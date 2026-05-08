package com.pharmacy.order.service;

import com.pharmacy.order.dto.AddressDTO;
import com.pharmacy.order.entity.Address;
import com.pharmacy.order.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public AddressDTO saveAddress(String email, AddressDTO request) {
        Address address = new Address();
        address.setUserEmail(email);
        address.setFullName(request.getFullName());
        address.setMobile(request.getMobile());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setDefault(request.isDefault());
        return mapToDTO(addressRepository.save(address));
    }

    public List<AddressDTO> getMyAddresses(String email) {
        return addressRepository.findByUserEmail(email)
            .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AddressDTO mapToDTO(Address a) {
        AddressDTO dto = new AddressDTO();
        dto.setId(a.getId());
        dto.setFullName(a.getFullName());
        dto.setMobile(a.getMobile());
        dto.setAddressLine1(a.getAddressLine1());
        dto.setAddressLine2(a.getAddressLine2());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setPincode(a.getPincode());
        dto.setDefault(a.isDefault());
        return dto;
    }
}