package com.planwith.planwith_fo_chat.adapter.out.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageBrokerPort;

@Component
@Profile("test")
public class InMemoryChatMessageBroker implements ChatMessageBrokerPort {

	private final List<ChatRealtimePayload> published = new CopyOnWriteArrayList<>();

	@Override
	public void publish(ChatRealtimePayload payload) {
		published.add(payload);
	}

	public List<ChatRealtimePayload> published() {
		return List.copyOf(published);
	}

	public void clear() {
		published.clear();
	}

	public List<ChatRealtimePayload> drain() {
		List<ChatRealtimePayload> copy = new ArrayList<>(published);
		published.clear();
		return copy;
	}
}
