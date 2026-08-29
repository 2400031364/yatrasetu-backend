package com.yatrasetu.tourism.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentRequest {
    @NotBlank(message = "Payment method is required")
    private String method; // UPI, CARD, NETBANKING, WALLET, PAY_AT_HOTEL

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
