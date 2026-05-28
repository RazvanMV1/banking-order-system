package com.example.order_processor.service;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import com.example.order_processor.model.OrderStatus;
import com.example.order_processor.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setAmount(new BigDecimal("100.00"));
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setVersion(0L);
    }

    @Test
    void processOrder_correctAmount_shouldReturnProcessed() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.processOrder(request);

        assertEquals(OrderStatus.PROCESSED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void processOrder_wrongAmount_shouldReturnReversed() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("200.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.processOrder(request);

        assertEquals(OrderStatus.PROCESSED, result.getStatus());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
        verify(orderRepository, times(3)).save(any(Order.class));
    }

    @Test
    void processOrder_orderNotFound_shouldThrowException() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(99L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.processOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processOrder_alreadyProcessed_shouldReturnAsIs() {
        pendingOrder.setStatus(OrderStatus.PROCESSED);

        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        Order result = orderService.processOrder(request);

        assertEquals(OrderStatus.PROCESSED, result.getStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
