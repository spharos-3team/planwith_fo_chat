package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;

@Component
@Transactional
public class ProcessedChatEventPersistenceAdapter implements ProcessedChatEventPort {

	private final ProcessedChatEventJpaRepository processedChatEventJpaRepository;

	public ProcessedChatEventPersistenceAdapter(ProcessedChatEventJpaRepository processedChatEventJpaRepository) {
		this.processedChatEventJpaRepository = processedChatEventJpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEventId(String eventId) {
		return processedChatEventJpaRepository.existsByEventId(eventId);
	}

	@Override
	public void save(String eventId, String eventType, UUID meetingUuid, Instant processedAt) {
		ProcessedChatEventJpaEntity entity = new ProcessedChatEventJpaEntity();
		entity.setEventId(eventId);
		entity.setEventType(eventType);
		entity.setMeetingUuid(meetingUuid.toString());
		entity.setProcessedAt(processedAt);
		processedChatEventJpaRepository.save(entity);
	}
}
