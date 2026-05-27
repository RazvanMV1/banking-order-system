package com.example.order_gateway.controller;

import com.example.order_gateway.dto.OrderDTO;
import com.example.order_gateway.dto.ProcessOrderRequest;
import com.example.order_gateway.service.GatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    @PostMapping("/process")
    public ResponseEntity<OrderDTO> processOrder(
            @RequestBody ProcessOrderRequest request) {
        OrderDTO result = gatewayService.processOrder(request.getOrderId());
        return ResponseEntity.ok(result);
    }
}
