package com.example.order_gateway.config;

import org.apache.coyote.ProtocolHandler;

import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadTomcatCustomizer() {
        return (ProtocolHandler protocolHandler) ->
                protocolHandler.setExecutor(
                        Executors.newVirtualThreadPerTaskExecutor()
                );
    }
}
