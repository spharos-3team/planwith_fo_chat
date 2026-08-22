package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "processed_chat_events",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_processed_chat_events_event_id",
				columnNames = {"event_id"}
		)
)
public class ProcessedChatEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "processed_id")
	private Long processedId;

	@Column(name = "event_id", nullable = false, length = 36)
	private String eventId;

	@Column(name = "event_type", nullable = false, length = 64)
	private String eventType;

	@Column(name = "meeting_uuid", nullable = false, length = 36)
	private String meetingUuid;

	@Column(name = "processed_at", nullable = false, columnDefinition = "datetime")
	private Instant processedAt;

	public Long getProcessedId() {
		return processedId;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getMeetingUuid() {
		return meetingUuid;
	}

	public void setMeetingUuid(String meetingUuid) {
		this.meetingUuid = meetingUuid;
	}

	public Instant getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(Instant processedAt) {
		this.processedAt = processedAt;
	}
}
