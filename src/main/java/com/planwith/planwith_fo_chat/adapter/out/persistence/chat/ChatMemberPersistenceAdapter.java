package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;

@Component
@Transactional
public class ChatMemberPersistenceAdapter implements ChatMemberRepositoryPort {

	private final ChatRoomJpaRepository chatRoomJpaRepository;
	private final ChatMemberJpaRepository chatMemberJpaRepository;

	public ChatMemberPersistenceAdapter(
			ChatRoomJpaRepository chatRoomJpaRepository,
			ChatMemberJpaRepository chatMemberJpaRepository
	) {
		this.chatRoomJpaRepository = chatRoomJpaRepository;
		this.chatMemberJpaRepository = chatMemberJpaRepository;
	}

	@Override
	public ChatMember save(ChatMember member) {
		ChatRoomJpaEntity room = chatRoomJpaRepository.findById(member.getChatRoomId())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		ChatMemberJpaEntity entity = chatMemberJpaRepository
				.findByChatRoom_ChatRoomIdAndMemberUuid(member.getChatRoomId(), member.getMemberUuid().toString())
				.orElseGet(ChatMemberJpaEntity::new);
		entity.setChatRoom(room);
		entity.setMemberUuid(member.getMemberUuid().toString());
		entity.setLastReadMessageUuid(
				member.getLastReadMessageUuid() == null ? null : member.getLastReadMessageUuid().toString()
		);
		entity.setNotificationEnabled(member.isNotificationEnabled());
		entity.setStatus(member.getStatus());
		entity.setJoinedAt(member.getJoinedAt());
		return toDomain(chatMemberJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ChatMember> findByChatRoomIdAndMemberUuid(Long chatRoomId, UUID memberUuid) {
		return chatMemberJpaRepository
				.findByChatRoom_ChatRoomIdAndMemberUuid(chatRoomId, memberUuid.toString())
				.map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChatMember> findByChatRoomIdAndStatus(Long chatRoomId, ChatMemberStatus status) {
		return chatMemberJpaRepository.findByChatRoom_ChatRoomIdAndStatus(chatRoomId, status)
				.stream()
				.map(this::toDomain)
				.toList();
	}

	private ChatMember toDomain(ChatMemberJpaEntity entity) {
		return new ChatMember(
				entity.getChatMemberId(),
				entity.getChatRoom().getChatRoomId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getLastReadMessageUuid() == null ? null : UUID.fromString(entity.getLastReadMessageUuid()),
				entity.isNotificationEnabled(),
				entity.getStatus(),
				entity.getJoinedAt()
		);
	}
}
