package com.example.order_processor.service;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import com.example.order_processor.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private RetryService retryService;

    @Test
    void processWithRetry_firstAttemptSucceeds_shouldReturnOrder() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        Order expected = new Order();
        expected.setId(1L);
        expected.setStatus(OrderStatus.PROCESSED);

        when(orderService.processOrder(request)).thenReturn(expected);

        Order result = retryService.processWithRetry(request);

        assertEquals(OrderStatus.PROCESSED, result.getStatus());
        verify(orderService, times(1)).processOrder(request);
    }

    @Test
    void processWithRetry_failsTwiceThenSucceeds_shouldRetryAndReturn() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        Order expected = new Order();
        expected.setId(1L);
        expected.setStatus(OrderStatus.PROCESSED);

        when(orderService.processOrder(request))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 1L))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 1L))
                .thenReturn(expected);

        Order result = retryService.processWithRetry(request);

        assertEquals(OrderStatus.PROCESSED, result.getStatus());
        verify(orderService, times(3)).processOrder(request);
    }

    @Test
    void processWithRetry_failsAllAttempts_shouldThrowException() {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        when(orderService.processOrder(request))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 1L));

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> retryService.processWithRetry(request));

        verify(orderService, times(3)).processOrder(request);
    }
}
