package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

@Component
@Profile("!test")
public class ChatMessageMongoAdapter implements ChatMessageRepositoryPort {

	private final ChatMessageMongoRepository chatMessageMongoRepository;

	public ChatMessageMongoAdapter(ChatMessageMongoRepository chatMessageMongoRepository) {
		this.chatMessageMongoRepository = chatMessageMongoRepository;
	}

	@Override
	public ChatMessage save(ChatMessage message) {
		ChatMessageDocument document = message.getId() == null
				? new ChatMessageDocument()
				: chatMessageMongoRepository.findById(message.getId()).orElseGet(ChatMessageDocument::new);
		document.setMessageUuid(message.getMessageUuid().toString());
		document.setChatRoomUuid(message.getChatRoomUuid().toString());
		document.setSenderUuid(message.getSenderUuid().toString());
		document.setMessageType(message.getMessageType());
		document.setContent(message.getContent());
		document.setFiles(message.getFiles().stream().map(this::toFileDocument).toList());
		document.setModified(message.isModified());
		document.setDeleted(message.isDeleted());
		document.setCreatedAt(message.getCreatedAt());
		document.setUpdatedAt(message.getUpdatedAt());
		return toDomain(chatMessageMongoRepository.save(document));
	}

	@Override
	public Optional<ChatMessage> findByMessageUuid(UUID messageUuid) {
		return chatMessageMongoRepository.findByMessageUuid(messageUuid.toString()).map(this::toDomain);
	}

	@Override
	public List<ChatMessage> findByChatRoomUuid(UUID chatRoomUuid, Instant before, int size) {
		PageRequest page = PageRequest.of(0, size);
		List<ChatMessageDocument> documents = before == null
				? chatMessageMongoRepository.findByChatRoomUuidOrderByCreatedAtDesc(chatRoomUuid.toString(), page)
				: chatMessageMongoRepository.findByChatRoomUuidAndCreatedAtLessThanOrderByCreatedAtDesc(
						chatRoomUuid.toString(),
						before,
						page
				);
		return documents.stream().map(this::toDomain).toList();
	}

	private ChatMessageDocument.ChatFileDocument toFileDocument(ChatFile file) {
		ChatMessageDocument.ChatFileDocument document = new ChatMessageDocument.ChatFileDocument();
		document.setFileType(file.getFileType().name());
		document.setUrl(file.getUrl());
		document.setName(file.getName());
		return document;
	}

	private ChatMessage toDomain(ChatMessageDocument document) {
		List<ChatFile> files = document.getFiles() == null
				? List.of()
				: document.getFiles().stream().map(this::toFile).toList();
		return new ChatMessage(
				document.getId(),
				UUID.fromString(document.getMessageUuid()),
				UUID.fromString(document.getChatRoomUuid()),
				UUID.fromString(document.getSenderUuid()),
				document.getMessageType(),
				document.getContent(),
				files,
				document.isModified(),
				document.isDeleted(),
				document.getCreatedAt(),
				document.getUpdatedAt()
		);
	}

	private ChatFile toFile(ChatMessageDocument.ChatFileDocument document) {
		return new ChatFile(FileType.valueOf(document.getFileType()), document.getUrl(), document.getName());
	}
}
