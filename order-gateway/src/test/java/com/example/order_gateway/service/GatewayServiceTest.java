package com.example.order_gateway.service;

import com.example.order_gateway.dto.OrderDTO;
import com.example.order_gateway.dto.ProcessOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GatewayServiceTest {

    private MockWebServer mockWebServer;
    private GatewayService gatewayService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(2000);
        dispatcher.setMaxRequestsPerHost(2000);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(10, 30, TimeUnit.SECONDS))
                .build();

        String baseUrl = mockWebServer.url("").toString();
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        gatewayService = new GatewayService(baseUrl, httpClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void processOrder_successfulFlow_shouldReturnProcessedOrder() throws Exception {
        OrderDTO getResponse = new OrderDTO();
        getResponse.setId(1L);
        getResponse.setAmount(new BigDecimal("100.00"));
        getResponse.setStatus("PENDING");
        getResponse.setVersion(0L);

        OrderDTO putResponse = new OrderDTO();
        putResponse.setId(1L);
        putResponse.setAmount(new BigDecimal("100.00"));
        putResponse.setStatus("PROCESSED");
        putResponse.setVersion(1L);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(getResponse)));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(putResponse)));

        OrderDTO result = gatewayService.processOrder(1L);

        assertEquals("PROCESSED", result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(1L, result.getId());
    }

    @Test
    void processOrder_processorReturns500_shouldThrowException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\": \"Internal Server Error\"}"));

        assertThrows(RuntimeException.class, () -> gatewayService.processOrder(1L));
    }

    @Test
    void processOrder_processorReturns404_shouldThrowException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\": \"Order not found\"}"));

        assertThrows(RuntimeException.class, () -> gatewayService.processOrder(1L));
    }
}
