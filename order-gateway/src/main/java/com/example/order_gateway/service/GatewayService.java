package com.example.order_gateway.service;

import com.example.order_gateway.dto.OrderDTO;
import com.example.order_gateway.dto.ProcessOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@Slf4j
public class GatewayService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String processorBaseUrl;

    public static final MediaType JSON = MediaType.get("application/json");

    public GatewayService(
            @Value("${order.processor.base-url}") String processorBaseUrl,
            OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        this.processorBaseUrl = processorBaseUrl;
    }

    private OrderDTO getOrder(Long orderId) throws IOException {
        Request request = new Request.Builder()
                .url(processorBaseUrl + "/orders/" + orderId)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Error getting order: " + response.code());
            }
            return objectMapper.readValue(response.body().string(), OrderDTO.class);
        }
    }

    private OrderDTO processOrder(Long orderId, BigDecimal amount) throws IOException {
        ProcessOrderRequest requestBody = new ProcessOrderRequest();
        requestBody.setOrderId(orderId);
        requestBody.setRequestedAmount(amount);

        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody), JSON
        );

        Request request = new Request.Builder()
                .url(processorBaseUrl + "/orders/" + orderId + "/process")
                .put(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Error processing order: " + response.code());
            }
            return objectMapper.readValue(response.body().string(), OrderDTO.class);
        }
    }

    public OrderDTO processOrder(Long orderId) {
        try {
            log.info("Getting order: {}", orderId);
            OrderDTO order = getOrder(orderId);
            log.info("Processing order: {}", orderId);
            return processOrder(orderId, order.getAmount());
        } catch (IOException e) {
            log.error("Error processing order {}: {}", orderId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
