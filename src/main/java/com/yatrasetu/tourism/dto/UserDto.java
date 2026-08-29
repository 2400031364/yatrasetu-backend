package com.yatrasetu.tourism.dto;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String address;
    private String role;
    private Long hotelId;
    private String hotelName;

    public UserDto() {}
    public UserDto(Long id, String name, String email, String mobile, String address) {
        this.id = id; this.name = name; this.email = email; this.mobile = mobile; this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
}
