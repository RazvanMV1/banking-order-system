package com.example.order_processor.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessOrderRequest {
    private Long orderId;
    private BigDecimal requestedAmount;
}
