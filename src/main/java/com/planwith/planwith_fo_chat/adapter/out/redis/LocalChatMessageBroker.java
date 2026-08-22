package com.planwith.planwith_fo_chat.adapter.out.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageBrokerPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRealtimeFanoutPort;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalChatMessageBroker implements ChatMessageBrokerPort {

	private final ChatRealtimeFanoutPort chatRealtimeFanoutPort;

	public LocalChatMessageBroker(ChatRealtimeFanoutPort chatRealtimeFanoutPort) {
		this.chatRealtimeFanoutPort = chatRealtimeFanoutPort;
	}

	@Override
	public void publish(ChatRealtimePayload payload) {
		chatRealtimeFanoutPort.deliver(payload);
	}
}
