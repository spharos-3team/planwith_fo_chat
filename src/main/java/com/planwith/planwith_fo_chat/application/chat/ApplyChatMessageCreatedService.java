package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.port.in.ApplyChatMessageCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

@Service
@Transactional
public class ApplyChatMessageCreatedService implements ApplyChatMessageCreatedUseCase {

	public static final String EVENT_TYPE = "chat.message.created";

	private static final Logger log = LoggerFactory.getLogger(ApplyChatMessageCreatedService.class);

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort;
	private final ProcessedChatEventPort processedChatEventPort;

	public ApplyChatMessageCreatedService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort,
			ProcessedChatEventPort processedChatEventPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatRoomMemberReadRepositoryPort = chatRoomMemberReadRepositoryPort;
		this.processedChatEventPort = processedChatEventPort;
	}

	@Override
	public void apply(Command command) {
		Objects.requireNonNull(command, "Chat message created command is required.");
		if (!StringUtils.hasText(command.eventId())) {
			throw new IllegalArgumentException("eventId is required.");
		}
		if (command.chatRoomUuid() == null) {
			throw new IllegalArgumentException("chatRoomUuid is required.");
		}
		if (command.messageUuid() == null) {
			throw new IllegalArgumentException("messageUuid is required.");
		}
		if (command.senderUuid() == null) {
			throw new IllegalArgumentException("senderUuid is required.");
		}
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();
		if (processedChatEventPort.existsByEventId(command.eventId())) {
			log.warn("ApplyChatMessageCreatedService : apply : duplicate event ignored - eventId={}", command.eventId());
			return;
		}
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(command.chatRoomUuid())
				.orElseThrow(() -> new IllegalArgumentException("chat room is required."));
		for (ChatMember member : chatMemberRepositoryPort.findByChatRoomIdAndStatus(
				room.getChatRoomId(),
				ChatMemberStatus.APPROVED
		)) {
			updateRead(room, member, command, now);
		}
		markProcessed(command.eventId(), command.chatRoomUuid(), now);
	}

	private void updateRead(ChatRoom room, ChatMember member, Command command, Instant now) {
		ChatRoomMemberRead current = chatRoomMemberReadRepositoryPort
				.findByMemberUuidAndChatRoomUuid(member.getMemberUuid(), room.getChatRoomUuid())
				.orElseGet(() -> ChatRoomMemberRead.empty(
						member.getMemberUuid(),
						room.getChatRoomUuid(),
						room.getRoomName(),
						room.getCreatedAt()
				));
		if (current.isOlderThan(now)) {
			return;
		}
		boolean incrementUnread = !member.getMemberUuid().equals(command.senderUuid());
		chatRoomMemberReadRepositoryPort.save(current.applyMessage(
				command.messageUuid(),
				command.content(),
				command.senderUuid(),
				now,
				incrementUnread
		));
	}

	private void markProcessed(String eventId, UUID chatRoomUuid, Instant now) {
		try {
			processedChatEventPort.save(eventId, EVENT_TYPE, chatRoomUuid, now);
		}
		catch (DataIntegrityViolationException exception) {
			log.warn("ApplyChatMessageCreatedService : apply : concurrent duplicate event ignored - eventId={}", eventId);
		}
	}
}
