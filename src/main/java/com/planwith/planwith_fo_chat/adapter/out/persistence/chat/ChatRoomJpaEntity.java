package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_rooms")
public class ChatRoomJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_id", columnDefinition = "bigint unsigned")
	private Long chatRoomId;

	@Column(name = "chat_room_uuid", nullable = false, unique = true, length = 36)
	private String chatRoomUuid;

	@Column(name = "meeting_uuid", nullable = false, unique = true, length = 36)
	private String meetingUuid;

	@Column(name = "room_name", length = 100)
	private String roomName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	private ChatRoomStatus status;

	@Column(name = "created_at", nullable = false, columnDefinition = "datetime")
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false, columnDefinition = "datetime")
	private Instant updatedAt;

	public Long getChatRoomId() {
		return chatRoomId;
	}

	public String getChatRoomUuid() {
		return chatRoomUuid;
	}

	public void setChatRoomUuid(String chatRoomUuid) {
		this.chatRoomUuid = chatRoomUuid;
	}

	public String getMeetingUuid() {
		return meetingUuid;
	}

	public void setMeetingUuid(String meetingUuid) {
		this.meetingUuid = meetingUuid;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public ChatRoomStatus getStatus() {
		return status;
	}

	public void setStatus(ChatRoomStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
