package com.example.order_processor.service;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;

    private final OrderService orderService;

    public Order processWithRetry(ProcessOrderRequest request) {
        int attempts = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while (attempts < MAX_RETRIES) {
            try {
                return orderService.processOrder(request);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    log.error("Order {} failed after {} attempts", request.getOrderId(), MAX_RETRIES);
                    throw e;
                }
                log.warn("Optimistic lock conflict for order {}, attempt {}/{}, retrying in {}ms",
                        request.getOrderId(), attempts, MAX_RETRIES, backoff);
                sleep(backoff);
                backoff *= 2;
            }
        }

        throw new RuntimeException("Unexpected retry loop exit for order: " + request.getOrderId());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", e);
        }
    }
}
