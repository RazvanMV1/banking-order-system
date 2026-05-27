package com.example.order_processor.controller;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import com.example.order_processor.service.OrderService;
import com.example.order_processor.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final RetryService retryService;

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<Order> processOrder(
            @PathVariable Long id,
            @RequestBody ProcessOrderRequest request) {
        request.setOrderId(id);
        Order result = retryService.processWithRetry(request);
        return ResponseEntity.ok(result);
    }
}
