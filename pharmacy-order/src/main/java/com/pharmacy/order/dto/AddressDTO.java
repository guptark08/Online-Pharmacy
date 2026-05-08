package com.pharmacy.order.dto;

public class AddressDTO {

    private Long id;
    private String fullName;
    private String mobile;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private boolean isDefault;

    public AddressDTO() {}

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getMobile() { return mobile; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public boolean isDefault() { return isDefault; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}