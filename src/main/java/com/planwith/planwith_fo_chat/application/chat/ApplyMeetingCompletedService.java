package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
@Transactional
public class ApplyMeetingCompletedService implements ApplyMeetingCompletedUseCase {

	public static final String EVENT_TYPE = "meeting.completed";

	private static final Logger log = LoggerFactory.getLogger(ApplyMeetingCompletedService.class);

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ProcessedChatEventPort processedChatEventPort;

	public ApplyMeetingCompletedService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ProcessedChatEventPort processedChatEventPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.processedChatEventPort = processedChatEventPort;
	}

	@Override
	public ChatRoom apply(Command command) {
		Objects.requireNonNull(command, "Meeting completed command is required.");
		if (!StringUtils.hasText(command.eventId())) {
			throw new IllegalArgumentException("eventId is required.");
		}
		if (command.meetingUuid() == null) {
			throw new IllegalArgumentException("meetingUuid is required.");
		}
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();

		if (processedChatEventPort.existsByEventId(command.eventId())) {
			log.warn("ApplyMeetingCompletedService : apply : duplicate event ignored - eventId={}", command.eventId());
			return chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid()).orElse(null);
		}

		ChatRoom room = chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.orElseThrow(() -> new ChatRoomNotReadyException(command.meetingUuid()));
		ChatRoom ended = chatRoomRepositoryPort.save(room.end(now));
		try {
			processedChatEventPort.save(command.eventId(), EVENT_TYPE, command.meetingUuid(), now);
		}
		catch (DataIntegrityViolationException exception) {
			log.warn("ApplyMeetingCompletedService : apply : concurrent duplicate event ignored - eventId={}",
					command.eventId());
		}
		log.info(
				"ApplyMeetingCompletedService : apply : chat room ended - meetingUuid={} chatRoomUuid={}",
				ended.getMeetingUuid(),
				ended.getChatRoomUuid()
		);
		return ended;
	}
}
