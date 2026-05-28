package com.example.order_processor.controller;

import com.example.order_processor.dto.ProcessOrderRequest;
import com.example.order_processor.model.Order;
import com.example.order_processor.model.OrderStatus;
import com.example.order_processor.service.OrderService;
import com.example.order_processor.service.RetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private RetryService retryService;

    @Test
    void getOrder_existingId_shouldReturn200() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PENDING);

        when(orderService.getOrder(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void getOrder_notFound_shouldReturn500() throws Exception {
        when(orderService.getOrder(99L))
                .thenThrow(new RuntimeException("Order not found: 99"));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Order not found: 99"));
    }

    @Test
    void processOrder_validRequest_shouldReturn200() throws Exception {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        Order result = new Order();
        result.setId(1L);
        result.setAmount(new BigDecimal("100.00"));
        result.setStatus(OrderStatus.PROCESSED);

        when(retryService.processWithRetry(any(ProcessOrderRequest.class))).thenReturn(result);

        mockMvc.perform(put("/orders/1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSED"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void processOrder_conflict_shouldReturn409() throws Exception {
        ProcessOrderRequest request = new ProcessOrderRequest();
        request.setOrderId(1L);
        request.setRequestedAmount(new BigDecimal("100.00"));

        when(retryService.processWithRetry(any(ProcessOrderRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 1L));

        mockMvc.perform(put("/orders/1/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
