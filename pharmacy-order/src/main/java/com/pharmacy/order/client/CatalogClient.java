package com.pharmacy.order.client;

import com.pharmacy.order.dto.MedicineInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "pharmacy-catalog")
public interface CatalogClient {

    @GetMapping("/api/catalog/medicines/{id}")
    MedicineInfoDTO getMedicineById(@PathVariable("id") Long id);

    @PutMapping("/api/catalog/prescriptions/{prescriptionId}/assign-order/{orderId}")
    void assignPrescriptionOrder(@PathVariable("prescriptionId") Long prescriptionId,
                                 @PathVariable("orderId") Long orderId);
}
