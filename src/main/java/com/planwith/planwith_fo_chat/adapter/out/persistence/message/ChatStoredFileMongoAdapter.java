package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.out.ChatFileStoragePort;
import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

@Component
@Profile("!test")
public class ChatStoredFileMongoAdapter implements ChatFileStoragePort {

	private final ChatStoredFileMongoRepository chatStoredFileMongoRepository;

	public ChatStoredFileMongoAdapter(ChatStoredFileMongoRepository chatStoredFileMongoRepository) {
		this.chatStoredFileMongoRepository = chatStoredFileMongoRepository;
	}

	@Override
	public ChatStoredFile save(ChatStoredFile file) {
		ChatStoredFileDocument document = file.getId() == null
				? new ChatStoredFileDocument()
				: chatStoredFileMongoRepository.findById(file.getId()).orElseGet(ChatStoredFileDocument::new);
		document.setFileUuid(file.getFileUuid().toString());
		document.setChatRoomUuid(file.getChatRoomUuid().toString());
		document.setUploaderUuid(file.getUploaderUuid().toString());
		document.setFileType(file.getFileType().name());
		document.setContentType(file.getContentType());
		document.setName(file.getName());
		document.setBytes(file.getBytes());
		document.setCreatedAt(file.getCreatedAt());
		return toDomain(chatStoredFileMongoRepository.save(document));
	}

	@Override
	public Optional<ChatStoredFile> findByFileUuid(UUID fileUuid) {
		return chatStoredFileMongoRepository.findByFileUuid(fileUuid.toString()).map(this::toDomain);
	}

	private ChatStoredFile toDomain(ChatStoredFileDocument document) {
		return new ChatStoredFile(
				document.getId(),
				UUID.fromString(document.getFileUuid()),
				UUID.fromString(document.getChatRoomUuid()),
				UUID.fromString(document.getUploaderUuid()),
				FileType.valueOf(document.getFileType()),
				document.getContentType(),
				document.getName(),
				document.getBytes(),
				document.getCreatedAt()
		);
	}
}
