package com.be9expensphie.notification.consumer;

import com.be9expensphie.common.event.WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            dltTopicSuffix = ".DLT",
            kafkaTemplate = "retryKafkaTemplate"
    )
    @KafkaListener(
            topics = "websocket-events",
            groupId = "${kafka.websocket.group-id}",
            containerFactory = "wsKafkaListenerContainerFactory"
    )
    public void consume(WebSocketEvent event) {
        try {
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);
            messagingTemplate.convertAndSend(event.getDestination(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize WebSocket payload for destination {}", event.getDestination(), e);
            throw new RuntimeException("WebSocket payload deserialization failed: " + event.getDestination(), e);
        }
    }

    @DltHandler
    public void handleDlt(WebSocketEvent event) {
        log.error("websocket-events exhausted retries — dropped event for destination={}", event.getDestination());
    }
}
