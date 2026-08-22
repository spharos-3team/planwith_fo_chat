package com.planwith.planwith_fo_chat.domain.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChatMessage {

	private final String id;
	private final UUID messageUuid;
	private final UUID chatRoomUuid;
	private final UUID senderUuid;
	private final String messageType;
	private final String content;
	private final List<ChatFile> files;
	private final boolean modified;
	private final boolean deleted;
	private final Instant createdAt;
	private final Instant updatedAt;

	public ChatMessage(
			String id,
			UUID messageUuid,
			UUID chatRoomUuid,
			UUID senderUuid,
			String messageType,
			String content,
			List<ChatFile> files,
			boolean modified,
			boolean deleted,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.messageUuid = messageUuid;
		this.chatRoomUuid = chatRoomUuid;
		this.senderUuid = senderUuid;
		this.messageType = messageType;
		this.content = content;
		this.files = files == null ? List.of() : List.copyOf(files);
		this.modified = modified;
		this.deleted = deleted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ChatMessage create(
			UUID chatRoomUuid,
			UUID senderUuid,
			String messageType,
			String content,
			List<ChatFile> files,
			Instant now
	) {
		return new ChatMessage(
				null,
				UUID.randomUUID(),
				chatRoomUuid,
				senderUuid,
				messageType,
				content,
				files,
				false,
				false,
				now,
				now
		);
	}

	public ChatMessage markModified(String nextContent, Instant now) {
		return new ChatMessage(
				id,
				messageUuid,
				chatRoomUuid,
				senderUuid,
				messageType,
				nextContent,
				files,
				true,
				deleted,
				createdAt,
				now
		);
	}

	public ChatMessage markDeleted(Instant now) {
		return new ChatMessage(
				id,
				messageUuid,
				chatRoomUuid,
				senderUuid,
				messageType,
				content,
				files,
				modified,
				true,
				createdAt,
				now
		);
	}

	public boolean isSender(UUID memberUuid) {
		return senderUuid.equals(memberUuid);
	}

	public String getId() {
		return id;
	}

	public UUID getMessageUuid() {
		return messageUuid;
	}

	public UUID getChatRoomUuid() {
		return chatRoomUuid;
	}

	public UUID getSenderUuid() {
		return senderUuid;
	}

	public String getMessageType() {
		return messageType;
	}

	public String getContent() {
		return content;
	}

	public List<ChatFile> getFiles() {
		return files;
	}

	public boolean isModified() {
		return modified;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
