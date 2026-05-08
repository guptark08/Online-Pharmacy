package com.pharmacy.order.service;

import com.pharmacy.order.client.CatalogClient;
import com.pharmacy.order.config.RabbitMQConfig;
import com.pharmacy.order.dto.*;
import com.pharmacy.order.entity.*;
import com.pharmacy.order.enums.OrderStatus;
import com.pharmacy.order.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public OrderResponse checkout(String email, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(request.getAddressId())
            .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean needsRx = cart.getItems().stream()
            .anyMatch(CartItem::isRequiresPrescription);
        boolean hasAttachedPrescription = request.getPrescriptionId() != null;

        if (needsRx && request.getPrescriptionId() == null) {
            throw new RuntimeException("Prescription ID is required for Rx items");
        }

        // Validate each item price and stock from catalog
        for (CartItem item : cart.getItems()) {
            var medicine = catalogClient.getMedicineById(item.getMedicineId());
            if (item.getQuantity() > medicine.getStock()) {
                throw new RuntimeException("Insufficient stock for " + medicine.getName());
            }
            if (medicine.isRequiresPrescription() && request.getPrescriptionId() == null) {
                throw new RuntimeException("Prescription is required for " + medicine.getName());
            }
            item.setPrice(medicine.getPrice());
        }

        Order order = new Order();
        order.setUserEmail(email);
        order.setDeliveryAddress(address);
        order.setPrescriptionId(request.getPrescriptionId());
        order.setDeliverySlot(request.getDeliverySlot());
        order.setStatus((needsRx || hasAttachedPrescription) ?
            OrderStatus.PRESCRIPTION_PENDING :
            OrderStatus.PAYMENT_PENDING);

        BigDecimal total = cart.getItems().stream()
            .map(i -> i.getPrice()
                .multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        if (request.getPrescriptionId() != null) {
            catalogClient.assignPrescriptionOrder(
                request.getPrescriptionId(), savedOrder.getId());
        }

        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> {
                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setMedicineId(cartItem.getMedicineId());
                oi.setMedicineName(cartItem.getMedicineName());
                oi.setPrice(cartItem.getPrice());
                oi.setQuantity(cartItem.getQuantity());
                oi.setSubtotal(cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(
                        cartItem.getQuantity())));
                return oi;
            }).collect(Collectors.toList());

        savedOrder.setItems(orderItems);
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDTO(orderRepository.save(savedOrder));
    }

    public List<OrderResponse> getMyOrders(String email) {
        return orderRepository
            .findByUserEmailOrderByCreatedAtDesc(email)
            .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        return mapToDTO(order);
    }

    public OrderResponse cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CUSTOMER_CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        order.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        return mapToDTO(orderRepository.save(order));
    }

    public PaymentResponse processPayment(PaymentRequest request, String email) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.PAID ||
            order.getStatus() == OrderStatus.PACKED ||
            order.getStatus() == OrderStatus.OUT_FOR_DELIVERY ||
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Payment already processed");
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING &&
            order.getStatus() != OrderStatus.PRESCRIPTION_APPROVED) {
            throw new RuntimeException("Order is not ready for payment");
        }

        boolean success = !"FAIL".equalsIgnoreCase(request.getPaymentMethod());
        if (success) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_STATUS_QUEUE,
            new OrderEvent(order.getId(), order.getUserEmail(), order.getStatus()));

        PaymentResponse response = new PaymentResponse();
        response.setOrderId(order.getId());
        response.setOrderStatus(order.getStatus());
        response.setStatus(success ? "SUCCESS" : "FAILED");
        response.setMessage(success ? "Payment processed" : "Payment failed");
        return response;
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (order.getStatus()) {
            case PRESCRIPTION_PENDING -> {
                if (newStatus != OrderStatus.PRESCRIPTION_APPROVED &&
                    newStatus != OrderStatus.PRESCRIPTION_REJECTED) {
                    throw new RuntimeException("Invalid transition from PRESCRIPTION_PENDING");
                }
            }
            case PRESCRIPTION_APPROVED -> {
                if (newStatus != OrderStatus.PAYMENT_PENDING) {
                    throw new RuntimeException("Invalid transition from PRESCRIPTION_APPROVED");
                }
            }
            case PAYMENT_PENDING -> {
                if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.PAYMENT_FAILED) {
                    throw new RuntimeException("Invalid transition from PAYMENT_PENDING");
                }
            }
            case PAID -> {
                if (newStatus != OrderStatus.PACKED) {
                    throw new RuntimeException("Invalid transition from PAID");
                }
            }
            case PACKED -> {
                if (newStatus != OrderStatus.OUT_FOR_DELIVERY) {
                    throw new RuntimeException("Invalid transition from PACKED");
                }
            }
            case OUT_FOR_DELIVERY -> {
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException("Invalid transition from OUT_FOR_DELIVERY");
                }
            }
            default -> throw new RuntimeException("Cannot change status from " + order.getStatus());
        }

        order.setStatus(newStatus);
        return mapToDTO(orderRepository.save(order));
    }

    private OrderResponse mapToDTO(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPrescriptionId(order.getPrescriptionId());
        dto.setDeliverySlot(order.getDeliverySlot());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getDeliveryAddress() != null) {
            AddressDTO addrDTO = new AddressDTO();
            addrDTO.setId(order.getDeliveryAddress().getId());
            addrDTO.setFullName(order.getDeliveryAddress().getFullName());
            addrDTO.setCity(order.getDeliveryAddress().getCity());
            addrDTO.setState(order.getDeliveryAddress().getState());
            addrDTO.setPincode(order.getDeliveryAddress().getPincode());
            dto.setDeliveryAddress(addrDTO);
        }

        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> {
                    OrderItemDTO oi = new OrderItemDTO();
                    oi.setId(i.getId());
                    oi.setMedicineId(i.getMedicineId());
                    oi.setMedicineName(i.getMedicineName());
                    oi.setPrice(i.getPrice());
                    oi.setQuantity(i.getQuantity());
                    oi.setSubtotal(i.getSubtotal());
                    return oi;
                }).collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}
