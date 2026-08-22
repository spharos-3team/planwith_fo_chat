package com.planwith.planwith_fo_chat.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

public interface ChatMessageRepositoryPort {

	ChatMessage save(ChatMessage message);

	Optional<ChatMessage> findByMessageUuid(UUID messageUuid);

	List<ChatMessage> findByChatRoomUuid(UUID chatRoomUuid, Instant before, int size);
}
