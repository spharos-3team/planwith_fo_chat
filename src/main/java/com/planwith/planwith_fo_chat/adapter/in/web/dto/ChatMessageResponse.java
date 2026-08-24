package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

public record ChatMessageResponse(
		UUID messageUuid,
		UUID chatRoomUuid,
		UUID senderUuid,
		String messageType,
		String content,
		List<FileResponse> files,
		boolean modified,
		boolean deleted,
		Instant createdAt,
		Instant updatedAt
) {

	public static ChatMessageResponse from(ChatMessage message) {
		return new ChatMessageResponse(
				message.getMessageUuid(),
				message.getChatRoomUuid(),
				message.getSenderUuid(),
				message.getMessageType(),
				message.getContent(),
				message.getFiles().stream().map(FileResponse::from).toList(),
				message.isModified(),
				message.isDeleted(),
				message.getCreatedAt(),
				message.getUpdatedAt()
		);
	}

	public record FileResponse(
			FileType fileType,
			String url,
			String name
	) {

		public static FileResponse from(ChatFile file) {
			return new FileResponse(file.getFileType(), file.getUrl(), file.getName());
		}
	}
}
