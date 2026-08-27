package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageCreatedEventPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
public class SaveChatMessageService implements SaveChatMessageUseCase {

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatMessageRepositoryPort chatMessageRepositoryPort;
	private final ChatMessageCreatedEventPort chatMessageCreatedEventPort;

	public SaveChatMessageService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatMessageRepositoryPort chatMessageRepositoryPort,
			ChatMessageCreatedEventPort chatMessageCreatedEventPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatMessageRepositoryPort = chatMessageRepositoryPort;
		this.chatMessageCreatedEventPort = chatMessageCreatedEventPort;
	}

	@Override
	public ChatMessage save(Command command) {
		Objects.requireNonNull(command, "Save message command is required.");
		if (command.chatRoomUuid() == null) {
			throw new IllegalArgumentException("chatRoomUuid is required.");
		}
		if (command.senderUuid() == null) {
			throw new IllegalArgumentException("senderUuid is required.");
		}
		if (!StringUtils.hasText(command.messageType())) {
			throw new IllegalArgumentException("messageType is required.");
		}
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(command.chatRoomUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		if (room.isHidden()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
		}
		if (room.isEnded()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_ENDED);
		}
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), command.senderUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();
		ChatMessage saved = chatMessageRepositoryPort.save(ChatMessage.create(
				command.chatRoomUuid(),
				command.senderUuid(),
				command.messageType().trim(),
				command.content(),
				command.files(),
				now
		));
		chatMessageCreatedEventPort.publish(new ChatMessageCreatedEventPort.Event(
				UUID.randomUUID().toString(),
				saved.getChatRoomUuid(),
				saved.getMessageUuid(),
				saved.getSenderUuid(),
				listPreview(saved),
				saved.getCreatedAt()
		));
		return saved;
	}

	private String listPreview(ChatMessage saved) {
		if (StringUtils.hasText(saved.getContent())) {
			return saved.getContent();
		}
		if (saved.getFiles().isEmpty()) {
			return saved.getContent();
		}
		return switch (saved.getFiles().get(0).getFileType()) {
			case IMAGE -> "사진";
			case VIDEO -> "동영상";
			case AUDIO -> "음성";
			case DOCUMENT, ETC -> "파일";
		};
	}
}
