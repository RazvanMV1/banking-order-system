package com.example.order_processor.service;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import com.example.order_processor.model.OrderStatus;
import com.example.order_processor.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public Order processOrder(ProcessOrderRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Order not found: " + request.getOrderId()
                ));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Order {} already has status {}",
                    order.getId(), order.getStatus());
            return order;
        }

        if (request.getRequestedAmount().compareTo(order.getAmount()) == 0) {
            log.info("Correct amount for order {}", order.getId());
            order.setStatus(OrderStatus.PROCESSED);
            return orderRepository.save(order);
        } else {
            log.info("Wrong amount for order {} - reversing", order.getId());
            return reverseOrder(order, request.getRequestedAmount());
        }
    }

    private Order reverseOrder(Order original, BigDecimal requestedAmount) {
        original.setStatus(OrderStatus.REVERSED);
        orderRepository.save(original);

        var reversal = new Order();
        reversal.setAmount(original.getAmount().negate());
        reversal.setStatus(OrderStatus.REVERSED);
        reversal.setOriginalOrderId(original.getId());
        orderRepository.save(reversal);

        var newOrder = new Order();
        newOrder.setAmount(requestedAmount);
        newOrder.setStatus(OrderStatus.PROCESSED);
        newOrder.setOriginalOrderId(original.getId());
        return orderRepository.save(newOrder);
    }
}
