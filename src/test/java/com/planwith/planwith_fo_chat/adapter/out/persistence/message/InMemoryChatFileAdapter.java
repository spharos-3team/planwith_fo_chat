package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.out.ChatFileStoragePort;
import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;

@Component
@Profile("test")
public class InMemoryChatFileAdapter implements ChatFileStoragePort {

	private final Map<UUID, ChatStoredFile> files = new ConcurrentHashMap<>();

	@Override
	public ChatStoredFile save(ChatStoredFile file) {
		String id = file.getId() == null ? UUID.randomUUID().toString() : file.getId();
		ChatStoredFile stored = new ChatStoredFile(
				id,
				file.getFileUuid(),
				file.getChatRoomUuid(),
				file.getUploaderUuid(),
				file.getFileType(),
				file.getContentType(),
				file.getName(),
				file.getBytes(),
				file.getCreatedAt()
		);
		files.put(stored.getFileUuid(), stored);
		return stored;
	}

	@Override
	public Optional<ChatStoredFile> findByFileUuid(UUID fileUuid) {
		return Optional.ofNullable(files.get(fileUuid));
	}
}
