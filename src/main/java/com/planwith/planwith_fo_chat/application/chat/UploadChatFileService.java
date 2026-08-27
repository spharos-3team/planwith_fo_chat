package com.planwith.planwith_fo_chat.application.chat;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.UploadChatFileUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatFileStoragePort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

@Service
public class UploadChatFileService implements UploadChatFileUseCase {

	static final long MAX_BYTES = 10L * 1024 * 1024;

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatFileStoragePort chatFileStoragePort;

	public UploadChatFileService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatFileStoragePort chatFileStoragePort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatFileStoragePort = chatFileStoragePort;
	}

	@Override
	public ChatStoredFile upload(UUID chatRoomUuid, UUID uploaderUuid, MultipartFile file) {
		Objects.requireNonNull(chatRoomUuid, "chatRoomUuid is required.");
		Objects.requireNonNull(uploaderUuid, "uploaderUuid is required.");
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(chatRoomUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		if (room.isHidden()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
		}
		if (room.isEnded()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_ENDED);
		}
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), uploaderUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_CHAT_FILE, "파일이 필요합니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessException(ErrorCode.INVALID_CHAT_FILE, "파일 용량은 10MB 이하여야 합니다.");
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		}
		catch (IOException exception) {
			throw new BusinessException(ErrorCode.INVALID_CHAT_FILE);
		}
		if (bytes.length == 0) {
			throw new BusinessException(ErrorCode.INVALID_CHAT_FILE, "파일이 필요합니다.");
		}
		String name = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : "file";
		String contentType = StringUtils.hasText(file.getContentType())
				? file.getContentType().trim()
				: "application/octet-stream";
		return chatFileStoragePort.save(ChatStoredFile.create(
				chatRoomUuid,
				uploaderUuid,
				FileType.from(contentType, name),
				contentType,
				name,
				bytes,
				Instant.now()
		));
	}
}
