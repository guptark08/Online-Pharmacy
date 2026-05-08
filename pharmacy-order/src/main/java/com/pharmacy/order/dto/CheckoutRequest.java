package com.pharmacy.order.dto;

public class CheckoutRequest {

    private Long addressId;
    private Long prescriptionId;
    private String deliverySlot;

    public CheckoutRequest() {}

    public Long getAddressId() { return addressId; }
    public Long getPrescriptionId() { return prescriptionId; }
    public String getDeliverySlot() { return deliverySlot; }

    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public void setDeliverySlot(String deliverySlot) { this.deliverySlot = deliverySlot; }
}