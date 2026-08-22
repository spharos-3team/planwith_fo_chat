package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

@Component
@Profile("test")
public class InMemoryChatMessageAdapter implements ChatMessageRepositoryPort {

	private final Map<UUID, ChatMessage> messages = new ConcurrentHashMap<>();

	@Override
	public ChatMessage save(ChatMessage message) {
		String id = message.getId() == null ? UUID.randomUUID().toString() : message.getId();
		ChatMessage stored = new ChatMessage(
				id,
				message.getMessageUuid(),
				message.getChatRoomUuid(),
				message.getSenderUuid(),
				message.getMessageType(),
				message.getContent(),
				message.getFiles(),
				message.isModified(),
				message.isDeleted(),
				message.getCreatedAt(),
				message.getUpdatedAt()
		);
		messages.put(stored.getMessageUuid(), stored);
		return stored;
	}

	@Override
	public Optional<ChatMessage> findByMessageUuid(UUID messageUuid) {
		return Optional.ofNullable(messages.get(messageUuid));
	}

	@Override
	public List<ChatMessage> findByChatRoomUuid(UUID chatRoomUuid, Instant before, int size) {
		return messages.values().stream()
				.filter(message -> message.getChatRoomUuid().equals(chatRoomUuid))
				.filter(message -> before == null || message.getCreatedAt().isBefore(before))
				.sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
				.limit(size)
				.toList();
	}
}
