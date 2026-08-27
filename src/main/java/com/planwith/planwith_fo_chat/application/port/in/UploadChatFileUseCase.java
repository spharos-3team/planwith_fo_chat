package com.planwith.planwith_fo_chat.application.port.in;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;

public interface UploadChatFileUseCase {

	ChatStoredFile upload(UUID chatRoomUuid, UUID uploaderUuid, MultipartFile file);
}
