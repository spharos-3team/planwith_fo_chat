package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Component
@Transactional
public class ChatRoomPersistenceAdapter implements ChatRoomRepositoryPort {

	private final ChatRoomJpaRepository chatRoomJpaRepository;

	public ChatRoomPersistenceAdapter(ChatRoomJpaRepository chatRoomJpaRepository) {
		this.chatRoomJpaRepository = chatRoomJpaRepository;
	}

	@Override
	public ChatRoom save(ChatRoom room) {
		ChatRoomJpaEntity entity = room.getChatRoomId() == null
				? new ChatRoomJpaEntity()
				: chatRoomJpaRepository.findById(room.getChatRoomId()).orElseGet(ChatRoomJpaEntity::new);
		entity.setChatRoomUuid(room.getChatRoomUuid().toString());
		entity.setMeetingUuid(room.getMeetingUuid().toString());
		entity.setRoomName(room.getRoomName());
		entity.setStatus(room.getStatus());
		entity.setCreatedAt(room.getCreatedAt());
		entity.setUpdatedAt(room.getUpdatedAt());
		return toDomain(chatRoomJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ChatRoom> findByMeetingUuid(UUID meetingUuid) {
		return chatRoomJpaRepository.findByMeetingUuid(meetingUuid.toString()).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ChatRoom> findByChatRoomId(Long chatRoomId) {
		return chatRoomJpaRepository.findById(chatRoomId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ChatRoom> findByChatRoomUuid(UUID chatRoomUuid) {
		return chatRoomJpaRepository.findByChatRoomUuid(chatRoomUuid.toString()).map(this::toDomain);
	}

	private ChatRoom toDomain(ChatRoomJpaEntity entity) {
		return new ChatRoom(
				entity.getChatRoomId(),
				UUID.fromString(entity.getChatRoomUuid()),
				UUID.fromString(entity.getMeetingUuid()),
				entity.getRoomName(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
