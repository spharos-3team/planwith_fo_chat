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

import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
@Transactional
public class ApplyMeetingCreatedService implements ApplyMeetingCreatedUseCase {

	public static final String EVENT_TYPE = "meeting.created";

	private static final Logger log = LoggerFactory.getLogger(ApplyMeetingCreatedService.class);

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ProcessedChatEventPort processedChatEventPort;

	public ApplyMeetingCreatedService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ProcessedChatEventPort processedChatEventPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.processedChatEventPort = processedChatEventPort;
	}

	@Override
	public ChatRoom apply(Command command) {
		Objects.requireNonNull(command, "Meeting created command is required.");
		requireEventId(command.eventId());
		if (command.meetingUuid() == null) {
			throw new IllegalArgumentException("meetingUuid is required.");
		}
		if (command.hostMemberUuid() == null) {
			throw new IllegalArgumentException("hostMemberUuid is required.");
		}
		if (!StringUtils.hasText(command.title())) {
			throw new IllegalArgumentException("title is required.");
		}
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();

		if (processedChatEventPort.existsByEventId(command.eventId())) {
			log.warn("ApplyMeetingCreatedService : apply : duplicate event ignored - eventId={}", command.eventId());
			return chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid()).orElse(null);
		}

		ChatRoom room = chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.orElseGet(() -> createRoom(command, now));
		ensureHost(room, command.hostMemberUuid(), now);
		markProcessed(command.eventId(), command.meetingUuid(), now);
		return room;
	}

	private ChatRoom createRoom(Command command, Instant now) {
		try {
			ChatRoom saved = chatRoomRepositoryPort.save(
					ChatRoom.create(command.meetingUuid(), command.title().trim(), now)
			);
			log.info(
					"ApplyMeetingCreatedService : apply : chat room created - meetingUuid={} chatRoomUuid={}",
					saved.getMeetingUuid(),
					saved.getChatRoomUuid()
			);
			return saved;
		}
		catch (DataIntegrityViolationException exception) {
			return chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
					.orElseThrow(() -> exception);
		}
	}

	private void ensureHost(ChatRoom room, UUID hostMemberUuid, Instant now) {
		chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), hostMemberUuid)
				.orElseGet(() -> chatMemberRepositoryPort.save(ChatMember.host(room.getChatRoomId(), hostMemberUuid, now)));
	}

	private void markProcessed(String eventId, UUID meetingUuid, Instant now) {
		try {
			processedChatEventPort.save(eventId, EVENT_TYPE, meetingUuid, now);
		}
		catch (DataIntegrityViolationException exception) {
			log.warn("ApplyMeetingCreatedService : apply : concurrent duplicate event ignored - eventId={}", eventId);
		}
	}

	private void requireEventId(String eventId) {
		if (!StringUtils.hasText(eventId)) {
			throw new IllegalArgumentException("eventId is required.");
		}
	}
}
