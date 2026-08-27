package com.planwith.planwith_fo_chat.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.FileType;

public interface GetChatFileUseCase {

	Result get(UUID chatRoomUuid, UUID fileUuid, UUID requesterUuid);

	record Result(
			FileType fileType,
			String contentType,
			String name,
			byte[] bytes
	) {
	}
}
