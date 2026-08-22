package com.planwith.planwith_fo_chat.adapter.in.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;
import com.planwith.planwith_fo_chat.application.port.out.ChatRealtimeFanoutPort;

@Component
public class StompChatRealtimeFanoutAdapter implements ChatRealtimeFanoutPort {

	private final SimpMessagingTemplate simpMessagingTemplate;

	public StompChatRealtimeFanoutAdapter(SimpMessagingTemplate simpMessagingTemplate) {
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@Override
	public void deliver(ChatRealtimePayload payload) {
		simpMessagingTemplate.convertAndSend(ChatStompDestinations.subscribe(payload.chatRoomUuid()), payload);
	}
}
