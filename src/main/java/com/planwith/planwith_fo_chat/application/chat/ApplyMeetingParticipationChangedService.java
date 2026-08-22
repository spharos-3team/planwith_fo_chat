package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
@Transactional
public class ApplyMeetingParticipationChangedService implements ApplyMeetingParticipationChangedUseCase {

	public static final String EVENT_TYPE = "meeting.participation.changed";

	private static final Logger log = LoggerFactory.getLogger(ApplyMeetingParticipationChangedService.class);

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ProcessedChatEventPort processedChatEventPort;

	public ApplyMeetingParticipationChangedService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ProcessedChatEventPort processedChatEventPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.processedChatEventPort = processedChatEventPort;
	}

	@Override
	public ChatMember apply(Command command) {
		Objects.requireNonNull(command, "Participation changed command is required.");
		if (!StringUtils.hasText(command.eventId())) {
			throw new IllegalArgumentException("eventId is required.");
		}
		if (command.meetingUuid() == null) {
			throw new IllegalArgumentException("meetingUuid is required.");
		}
		if (command.memberUuid() == null) {
			throw new IllegalArgumentException("memberUuid is required.");
		}
		ChatMemberStatus status = ChatMemberStatus.from(command.status());
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();

		if (processedChatEventPort.existsByEventId(command.eventId())) {
			log.warn("ApplyMeetingParticipationChangedService : apply : duplicate event ignored - eventId={}",
					command.eventId());
			return findExisting(command).orElse(null);
		}

		ChatRoom room = chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.orElseThrow(() -> new ChatRoomNotReadyException(command.meetingUuid()));

		Optional<ChatMember> existing = chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(
				room.getChatRoomId(),
				command.memberUuid()
		);
		if (room.isEnded() && status.isJoinAttempt() && existing.isEmpty()) {
			log.warn(
					"ApplyMeetingParticipationChangedService : apply : ignore join on ended room - meetingUuid={} memberUuid={}",
					command.meetingUuid(),
					command.memberUuid()
			);
			markProcessed(command, now);
			return null;
		}

		ChatMember saved = existing
				.map(member -> chatMemberRepositoryPort.save(member.withStatus(status, now)))
				.orElseGet(() -> chatMemberRepositoryPort.save(
						ChatMember.of(room.getChatRoomId(), command.memberUuid(), status, now)
				));
		markProcessed(command, now);
		log.info(
				"ApplyMeetingParticipationChangedService : apply : member synced - meetingUuid={} memberUuid={} status={}",
				command.meetingUuid(),
				command.memberUuid(),
				status
		);
		return saved;
	}

	private Optional<ChatMember> findExisting(Command command) {
		return chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.flatMap(room -> chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(
						room.getChatRoomId(),
						command.memberUuid()
				));
	}

	private void markProcessed(Command command, Instant now) {
		try {
			processedChatEventPort.save(command.eventId(), EVENT_TYPE, command.meetingUuid(), now);
		}
		catch (DataIntegrityViolationException exception) {
			log.warn(
					"ApplyMeetingParticipationChangedService : apply : concurrent duplicate event ignored - eventId={}",
					command.eventId()
			);
		}
	}
}
