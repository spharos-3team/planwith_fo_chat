package com.planwith.planwith_fo_chat.adapter.in.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.EventEnvelope;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.MeetingCreatedPayload;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.MeetingParticipationChangedPayload;
import com.planwith.planwith_fo_chat.adapter.in.kafka.dto.MeetingUuidPayload;
import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingDisbandedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "consumer-enabled", havingValue = "true")
public class MeetingEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(MeetingEventConsumer.class);

	private final ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase;
	private final ApplyMeetingCompletedUseCase applyMeetingCompletedUseCase;
	private final ApplyMeetingDisbandedUseCase applyMeetingDisbandedUseCase;
	private final ApplyMeetingParticipationChangedUseCase applyMeetingParticipationChangedUseCase;
	private final ObjectMapper objectMapper;

	public MeetingEventConsumer(
			ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase,
			ApplyMeetingCompletedUseCase applyMeetingCompletedUseCase,
			ApplyMeetingDisbandedUseCase applyMeetingDisbandedUseCase,
			ApplyMeetingParticipationChangedUseCase applyMeetingParticipationChangedUseCase,
			ObjectMapper objectMapper
	) {
		this.applyMeetingCreatedUseCase = applyMeetingCreatedUseCase;
		this.applyMeetingCompletedUseCase = applyMeetingCompletedUseCase;
		this.applyMeetingDisbandedUseCase = applyMeetingDisbandedUseCase;
		this.applyMeetingParticipationChangedUseCase = applyMeetingParticipationChangedUseCase;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	void logConsumerReady() {
		log.info("MeetingEventConsumer : listening for meeting.created/completed/disbanded/participation");
	}

	@KafkaListener(topics = "${app.kafka.created-topic}")
	public void consumeCreated(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		EventEnvelope<MeetingCreatedPayload> envelope = parse(payload, new TypeReference<>() {
		});
		if (envelope == null || envelope.payload() == null) {
			log.error("MeetingEventConsumer : consumeCreated : skip invalid payload - topic={}", topic);
			return;
		}
		MeetingCreatedPayload body = envelope.payload();
		log.info(
				"MeetingEventConsumer : consumeCreated : eventId={} meetingUuid={}",
				envelope.eventId(),
				body.meetingUuid()
		);
		try {
			applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
					envelope.eventId(),
					body.meetingUuid(),
					body.hostMemberUuid(),
					body.title(),
					envelope.occurredAt()
			));
		}
		catch (IllegalArgumentException exception) {
			log.error("MeetingEventConsumer : consumeCreated : skip invalid event - eventId={}", envelope.eventId(), exception);
		}
		catch (RuntimeException exception) {
			log.error("MeetingEventConsumer : consumeCreated : retry later - eventId={}", envelope.eventId(), exception);
			throw exception;
		}
	}

	@KafkaListener(topics = "${app.kafka.completed-topic}")
	public void consumeCompleted(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		EventEnvelope<MeetingUuidPayload> envelope = parse(payload, new TypeReference<>() {
		});
		if (envelope == null || envelope.payload() == null) {
			log.error("MeetingEventConsumer : consumeCompleted : skip invalid payload - topic={}", topic);
			return;
		}
		try {
			applyMeetingCompletedUseCase.apply(new ApplyMeetingCompletedUseCase.Command(
					envelope.eventId(),
					envelope.payload().meetingUuid(),
					envelope.occurredAt()
			));
		}
		catch (ChatRoomNotReadyException exception) {
			log.warn("MeetingEventConsumer : consumeCompleted : room not ready, retry - meetingUuid={}",
					exception.getMeetingUuid());
			throw exception;
		}
		catch (IllegalArgumentException exception) {
			log.error("MeetingEventConsumer : consumeCompleted : skip invalid event - eventId={}", envelope.eventId());
		}
		catch (RuntimeException exception) {
			log.error("MeetingEventConsumer : consumeCompleted : retry later - eventId={}", envelope.eventId());
			throw exception;
		}
	}

	@KafkaListener(topics = "${app.kafka.disbanded-topic}")
	public void consumeDisbanded(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		EventEnvelope<MeetingUuidPayload> envelope = parse(payload, new TypeReference<>() {
		});
		if (envelope == null || envelope.payload() == null) {
			log.error("MeetingEventConsumer : consumeDisbanded : skip invalid payload - topic={}", topic);
			return;
		}
		try {
			applyMeetingDisbandedUseCase.apply(new ApplyMeetingDisbandedUseCase.Command(
					envelope.eventId(),
					envelope.payload().meetingUuid(),
					envelope.occurredAt()
			));
		}
		catch (ChatRoomNotReadyException exception) {
			log.warn("MeetingEventConsumer : consumeDisbanded : room not ready, retry - meetingUuid={}",
					exception.getMeetingUuid());
			throw exception;
		}
		catch (IllegalArgumentException exception) {
			log.error("MeetingEventConsumer : consumeDisbanded : skip invalid event - eventId={}", envelope.eventId());
		}
		catch (RuntimeException exception) {
			log.error("MeetingEventConsumer : consumeDisbanded : retry later - eventId={}", envelope.eventId());
			throw exception;
		}
	}

	@KafkaListener(topics = "${app.kafka.participation-changed-topic}")
	public void consumeParticipationChanged(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		EventEnvelope<MeetingParticipationChangedPayload> envelope = parse(payload, new TypeReference<>() {
		});
		if (envelope == null || envelope.payload() == null) {
			log.error("MeetingEventConsumer : consumeParticipationChanged : skip invalid payload - topic={}", topic);
			return;
		}
		MeetingParticipationChangedPayload body = envelope.payload();
		try {
			applyMeetingParticipationChangedUseCase.apply(new ApplyMeetingParticipationChangedUseCase.Command(
					envelope.eventId(),
					body.meetingUuid(),
					body.memberUuid(),
					body.status(),
					envelope.occurredAt()
			));
		}
		catch (ChatRoomNotReadyException exception) {
			log.warn("MeetingEventConsumer : consumeParticipationChanged : room not ready, retry - meetingUuid={}",
					exception.getMeetingUuid());
			throw exception;
		}
		catch (IllegalArgumentException exception) {
			log.error("MeetingEventConsumer : consumeParticipationChanged : skip invalid event - eventId={}",
					envelope.eventId());
		}
		catch (RuntimeException exception) {
			log.error("MeetingEventConsumer : consumeParticipationChanged : retry later - eventId={}",
					envelope.eventId());
			throw exception;
		}
	}

	private <T> EventEnvelope<T> parse(String payload, TypeReference<EventEnvelope<T>> type) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, type);
		}
		catch (JsonProcessingException exception) {
			log.error("MeetingEventConsumer : parse : invalid EventEnvelope JSON", exception);
			return null;
		}
	}
}
