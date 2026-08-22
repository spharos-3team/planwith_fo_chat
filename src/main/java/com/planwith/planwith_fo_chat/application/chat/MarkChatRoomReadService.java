package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.MarkChatRoomReadUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

@Service
@Transactional
public class MarkChatRoomReadService implements MarkChatRoomReadUseCase {

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort;

	public MarkChatRoomReadService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatRoomMemberReadRepositoryPort = chatRoomMemberReadRepositoryPort;
	}

	@Override
	public ChatRoomMemberRead markRead(Command command) {
		Objects.requireNonNull(command, "Mark read command is required.");
		if (command.memberUuid() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		if (command.chatRoomUuid() == null) {
			throw new IllegalArgumentException("chatRoomUuid is required.");
		}
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(command.chatRoomUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), command.memberUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		ChatRoomMemberRead read = chatRoomMemberReadRepositoryPort
				.findByMemberUuidAndChatRoomUuid(command.memberUuid(), command.chatRoomUuid())
				.orElseGet(() -> ChatRoomMemberRead.empty(
						command.memberUuid(),
						command.chatRoomUuid(),
						room.getRoomName(),
						room.getCreatedAt()
				));
		UUID lastRead = command.lastReadMessageUuid() != null
				? command.lastReadMessageUuid()
				: read.getLastMessageUuid();
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();
		chatMemberRepositoryPort.save(member.withLastRead(lastRead));
		return chatRoomMemberReadRepositoryPort.save(read.markRead(lastRead, now));
	}
}
