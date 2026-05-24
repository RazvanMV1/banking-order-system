package com.example.order_gateway.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private BigDecimal amount;
    private String status;
    private Long version;
    private Long originalOrderId;
    private LocalDateTime createdAt;
}
