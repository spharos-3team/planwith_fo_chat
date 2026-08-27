package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

public record ChatFileUploadResponse(
		UUID fileUuid,
		FileType fileType,
		String url,
		String name
) {

	public static ChatFileUploadResponse from(ChatStoredFile file) {
		return new ChatFileUploadResponse(
				file.getFileUuid(),
				file.getFileType(),
				file.publicUrl(),
				file.getName()
		);
	}
}
