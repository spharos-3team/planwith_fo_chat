package com.planwith.planwith_fo_chat.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;

public interface ChatFileStoragePort {

	ChatStoredFile save(ChatStoredFile file);

	Optional<ChatStoredFile> findByFileUuid(UUID fileUuid);
}
