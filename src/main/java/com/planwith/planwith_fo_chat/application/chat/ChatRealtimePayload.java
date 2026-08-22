package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

public record ChatRealtimePayload(
		UUID messageUuid,
		UUID chatRoomUuid,
		UUID senderUuid,
		String messageType,
		String content,
		List<FilePayload> files,
		boolean isModified,
		boolean isDeleted,
		Instant createdAt,
		Instant updatedAt
) {

	public static ChatRealtimePayload from(ChatMessage message) {
		List<FilePayload> files = message.getFiles().stream()
				.map(file -> new FilePayload(file.getFileType(), file.getUrl(), file.getName()))
				.toList();
		return new ChatRealtimePayload(
				message.getMessageUuid(),
				message.getChatRoomUuid(),
				message.getSenderUuid(),
				message.getMessageType(),
				message.getContent(),
				files,
				message.isModified(),
				message.isDeleted(),
				message.getCreatedAt(),
				message.getUpdatedAt()
		);
	}

	public record FilePayload(
			FileType fileType,
			String url,
			String name
	) {
		public ChatFile toDomain() {
			return new ChatFile(fileType, url, name);
		}
	}
}
