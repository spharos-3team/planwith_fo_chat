package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

@Component
@Transactional
public class ChatRoomMemberReadPersistenceAdapter implements ChatRoomMemberReadRepositoryPort {

	private final ChatRoomMemberReadJpaRepository chatRoomMemberReadJpaRepository;

	public ChatRoomMemberReadPersistenceAdapter(ChatRoomMemberReadJpaRepository chatRoomMemberReadJpaRepository) {
		this.chatRoomMemberReadJpaRepository = chatRoomMemberReadJpaRepository;
	}

	@Override
	public ChatRoomMemberRead save(ChatRoomMemberRead read) {
		ChatRoomMemberReadJpaEntity entity = read.getReadId() == null
				? chatRoomMemberReadJpaRepository
						.findByMemberUuidAndChatRoomUuid(read.getMemberUuid().toString(), read.getChatRoomUuid().toString())
						.orElseGet(ChatRoomMemberReadJpaEntity::new)
				: chatRoomMemberReadJpaRepository.findById(read.getReadId()).orElseGet(ChatRoomMemberReadJpaEntity::new);
		entity.setMemberUuid(read.getMemberUuid().toString());
		entity.setChatRoomUuid(read.getChatRoomUuid().toString());
		entity.setRoomName(read.getRoomName());
		entity.setLastMessageUuid(read.getLastMessageUuid() == null ? null : read.getLastMessageUuid().toString());
		entity.setLastMessageContent(read.getLastMessageContent());
		entity.setLastMessageSenderUuid(
				read.getLastMessageSenderUuid() == null ? null : read.getLastMessageSenderUuid().toString()
		);
		entity.setLastMessageAt(read.getLastMessageAt());
		entity.setUnreadCount(read.getUnreadCount());
		entity.setUpdatedAt(read.getUpdatedAt());
		return toDomain(chatRoomMemberReadJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ChatRoomMemberRead> findByMemberUuidAndChatRoomUuid(UUID memberUuid, UUID chatRoomUuid) {
		return chatRoomMemberReadJpaRepository
				.findByMemberUuidAndChatRoomUuid(memberUuid.toString(), chatRoomUuid.toString())
				.map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChatRoomMemberRead> findApprovedInbox(
			UUID memberUuid,
			Instant cursorAt,
			UUID cursorChatRoomUuid,
			int size
	) {
		return chatRoomMemberReadJpaRepository.findApprovedInbox(
						memberUuid.toString(),
						cursorAt,
						cursorChatRoomUuid == null ? null : cursorChatRoomUuid.toString(),
						PageRequest.of(0, size)
				)
				.stream()
				.map(this::toDomain)
				.toList();
	}

	private ChatRoomMemberRead toDomain(ChatRoomMemberReadJpaEntity entity) {
		return new ChatRoomMemberRead(
				entity.getReadId(),
				UUID.fromString(entity.getMemberUuid()),
				UUID.fromString(entity.getChatRoomUuid()),
				entity.getRoomName(),
				entity.getLastMessageUuid() == null ? null : UUID.fromString(entity.getLastMessageUuid()),
				entity.getLastMessageContent(),
				entity.getLastMessageSenderUuid() == null ? null : UUID.fromString(entity.getLastMessageSenderUuid()),
				entity.getLastMessageAt(),
				entity.getUnreadCount(),
				entity.getUpdatedAt()
		);
	}
}
