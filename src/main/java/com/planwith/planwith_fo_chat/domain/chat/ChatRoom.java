package com.planwith.planwith_fo_chat.domain.chat;

import java.time.Instant;
import java.util.UUID;

public class ChatRoom {

	private final Long chatRoomId;
	private final UUID chatRoomUuid;
	private final UUID meetingUuid;
	private final String roomName;
	private final ChatRoomStatus status;
	private final Instant createdAt;
	private final Instant updatedAt;

	public ChatRoom(
			Long chatRoomId,
			UUID chatRoomUuid,
			UUID meetingUuid,
			String roomName,
			ChatRoomStatus status,
			Instant createdAt,
			Instant updatedAt
	) {
		this.chatRoomId = chatRoomId;
		this.chatRoomUuid = chatRoomUuid;
		this.meetingUuid = meetingUuid;
		this.roomName = roomName;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ChatRoom create(UUID meetingUuid, String roomName, Instant now) {
		return new ChatRoom(
				null,
				UUID.randomUUID(),
				meetingUuid,
				roomName,
				ChatRoomStatus.ACTIVE,
				now,
				now
		);
	}

	public ChatRoom end(Instant now) {
		if (status == ChatRoomStatus.ENDED) {
			return this;
		}
		return new ChatRoom(
				chatRoomId,
				chatRoomUuid,
				meetingUuid,
				roomName,
				ChatRoomStatus.ENDED,
				createdAt,
				now
		);
	}

	public boolean isEnded() {
		return status == ChatRoomStatus.ENDED;
	}

	public Long getChatRoomId() {
		return chatRoomId;
	}

	public UUID getChatRoomUuid() {
		return chatRoomUuid;
	}

	public UUID getMeetingUuid() {
		return meetingUuid;
	}

	public String getRoomName() {
		return roomName;
	}

	public ChatRoomStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
