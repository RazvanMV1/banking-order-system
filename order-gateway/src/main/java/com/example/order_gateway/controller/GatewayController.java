package com.example.order_gateway.controller;

import com.example.order_gateway.dto.OrderDTO;
import com.example.order_gateway.service.GatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    @PostMapping("/process")
    public ResponseEntity<List<OrderDTO>> processOrders(
            @RequestBody List<Long> orderIds) {
        List<OrderDTO> results = gatewayService.processOrders(orderIds);
        return ResponseEntity.ok(results);
    }
}
