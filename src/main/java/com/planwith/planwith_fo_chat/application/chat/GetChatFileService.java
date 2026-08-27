package com.planwith.planwith_fo_chat.application.chat;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.GetChatFileUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatFileStoragePort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;

@Service
public class GetChatFileService implements GetChatFileUseCase {

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatFileStoragePort chatFileStoragePort;

	public GetChatFileService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatFileStoragePort chatFileStoragePort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatFileStoragePort = chatFileStoragePort;
	}

	@Override
	public Result get(UUID chatRoomUuid, UUID fileUuid, UUID requesterUuid) {
		Objects.requireNonNull(chatRoomUuid, "chatRoomUuid is required.");
		Objects.requireNonNull(fileUuid, "fileUuid is required.");
		Objects.requireNonNull(requesterUuid, "requesterUuid is required.");
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(chatRoomUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		if (room.isHidden()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
		}
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), requesterUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		ChatStoredFile file = chatFileStoragePort.findByFileUuid(fileUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_FILE_NOT_FOUND));
		if (!file.getChatRoomUuid().equals(chatRoomUuid)) {
			throw new BusinessException(ErrorCode.CHAT_FILE_NOT_FOUND);
		}
		return new Result(file.getFileType(), file.getContentType(), file.getName(), file.getBytes());
	}
}
