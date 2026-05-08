package com.be9expensphie.notification.consumer;
import com.be9expensphie.common.event.WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

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
        }
    }
}
