package com.planwith.planwith_fo_chat.domain.chat;

import java.time.Instant;
import java.util.UUID;

public class ChatStoredFile {

	private final String id;
	private final UUID fileUuid;
	private final UUID chatRoomUuid;
	private final UUID uploaderUuid;
	private final FileType fileType;
	private final String contentType;
	private final String name;
	private final byte[] bytes;
	private final Instant createdAt;

	public ChatStoredFile(
			String id,
			UUID fileUuid,
			UUID chatRoomUuid,
			UUID uploaderUuid,
			FileType fileType,
			String contentType,
			String name,
			byte[] bytes,
			Instant createdAt
	) {
		this.id = id;
		this.fileUuid = fileUuid;
		this.chatRoomUuid = chatRoomUuid;
		this.uploaderUuid = uploaderUuid;
		this.fileType = fileType;
		this.contentType = contentType;
		this.name = name;
		this.bytes = bytes == null ? new byte[0] : bytes.clone();
		this.createdAt = createdAt;
	}

	public static ChatStoredFile create(
			UUID chatRoomUuid,
			UUID uploaderUuid,
			FileType fileType,
			String contentType,
			String name,
			byte[] bytes,
			Instant createdAt
	) {
		return new ChatStoredFile(
				null,
				UUID.randomUUID(),
				chatRoomUuid,
				uploaderUuid,
				fileType,
				contentType,
				name,
				bytes,
				createdAt
		);
	}

	public ChatFile toChatFile() {
		return new ChatFile(fileType, publicUrl(), name);
	}

	public String publicUrl() {
		return "/api/v1/chat-rooms/" + chatRoomUuid + "/files/" + fileUuid;
	}

	public String getId() {
		return id;
	}

	public UUID getFileUuid() {
		return fileUuid;
	}

	public UUID getChatRoomUuid() {
		return chatRoomUuid;
	}

	public UUID getUploaderUuid() {
		return uploaderUuid;
	}

	public FileType getFileType() {
		return fileType;
	}

	public String getContentType() {
		return contentType;
	}

	public String getName() {
		return name;
	}

	public byte[] getBytes() {
		return bytes.clone();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
