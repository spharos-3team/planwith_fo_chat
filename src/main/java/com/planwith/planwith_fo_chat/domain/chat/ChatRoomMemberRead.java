package com.planwith.planwith_fo_chat.domain.chat;

import java.time.Instant;
import java.util.UUID;

public class ChatRoomMemberRead {

	private final Long readId;
	private final UUID memberUuid;
	private final UUID chatRoomUuid;
	private final String roomName;
	private final UUID lastMessageUuid;
	private final String lastMessageContent;
	private final UUID lastMessageSenderUuid;
	private final Instant lastMessageAt;
	private final int unreadCount;
	private final Instant updatedAt;

	public ChatRoomMemberRead(
			Long readId,
			UUID memberUuid,
			UUID chatRoomUuid,
			String roomName,
			UUID lastMessageUuid,
			String lastMessageContent,
			UUID lastMessageSenderUuid,
			Instant lastMessageAt,
			int unreadCount,
			Instant updatedAt
	) {
		this.readId = readId;
		this.memberUuid = memberUuid;
		this.chatRoomUuid = chatRoomUuid;
		this.roomName = roomName;
		this.lastMessageUuid = lastMessageUuid;
		this.lastMessageContent = lastMessageContent;
		this.lastMessageSenderUuid = lastMessageSenderUuid;
		this.lastMessageAt = lastMessageAt;
		this.unreadCount = unreadCount;
		this.updatedAt = updatedAt;
	}

	public static ChatRoomMemberRead empty(UUID memberUuid, UUID chatRoomUuid, String roomName, Instant now) {
		return new ChatRoomMemberRead(
				null,
				memberUuid,
				chatRoomUuid,
				roomName,
				null,
				null,
				null,
				now,
				0,
				now
		);
	}

	public boolean isOlderThan(Instant incomingAt) {
		return lastMessageAt != null && incomingAt != null && incomingAt.isBefore(lastMessageAt);
	}

	public ChatRoomMemberRead applyMessage(
			UUID messageUuid,
			String content,
			UUID senderUuid,
			Instant messageAt,
			boolean incrementUnread
	) {
		int nextUnread = incrementUnread ? unreadCount + 1 : unreadCount;
		return new ChatRoomMemberRead(
				readId,
				memberUuid,
				chatRoomUuid,
				roomName,
				messageUuid,
				content,
				senderUuid,
				messageAt,
				nextUnread,
				messageAt
		);
	}

	public ChatRoomMemberRead markRead(UUID lastReadMessageUuid, Instant now) {
		return new ChatRoomMemberRead(
				readId,
				memberUuid,
				chatRoomUuid,
				roomName,
				lastMessageUuid,
				lastMessageContent,
				lastMessageSenderUuid,
				lastMessageAt,
				0,
				now
		);
	}

	public ChatRoomMemberRead withRoomName(String nextRoomName, Instant now) {
		return new ChatRoomMemberRead(
				readId,
				memberUuid,
				chatRoomUuid,
				nextRoomName,
				lastMessageUuid,
				lastMessageContent,
				lastMessageSenderUuid,
				lastMessageAt,
				unreadCount,
				now
		);
	}

	public Long getReadId() {
		return readId;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public UUID getChatRoomUuid() {
		return chatRoomUuid;
	}

	public String getRoomName() {
		return roomName;
	}

	public UUID getLastMessageUuid() {
		return lastMessageUuid;
	}

	public String getLastMessageContent() {
		return lastMessageContent;
	}

	public UUID getLastMessageSenderUuid() {
		return lastMessageSenderUuid;
	}

	public Instant getLastMessageAt() {
		return lastMessageAt;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
