package com.pharmacy.order.service;

import com.pharmacy.order.dto.AddressDTO;
import com.pharmacy.order.entity.Address;
import com.pharmacy.order.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void saveAddressMapsRequestAndRepositoryResult() {
        AddressDTO request = new AddressDTO();
        request.setFullName("Jane Doe");
        request.setMobile("9999999999");
        request.setAddressLine1("Street 1");
        request.setCity("Kolkata");
        request.setState("WB");
        request.setPincode("700001");
        request.setDefault(true);

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        AddressDTO saved = addressService.saveAddress("jane@example.com", request);

        assertEquals(10L, saved.getId());
        assertEquals("Jane Doe", saved.getFullName());
        assertTrue(saved.isDefault());
    }

    @Test
    void getMyAddressesMapsEntitiesToDtos() {
        Address address = new Address();
        address.setId(1L);
        address.setUserEmail("jane@example.com");
        address.setFullName("Jane Doe");

        when(addressRepository.findByUserEmail("jane@example.com")).thenReturn(List.of(address));

        List<AddressDTO> addresses = addressService.getMyAddresses("jane@example.com");

        assertEquals(1, addresses.size());
        assertEquals("Jane Doe", addresses.get(0).getFullName());
    }
}
