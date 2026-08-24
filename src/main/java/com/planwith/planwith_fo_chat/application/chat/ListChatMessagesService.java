package com.planwith.planwith_fo_chat.application.chat;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.ListChatMessagesUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
public class ListChatMessagesService implements ListChatMessagesUseCase {

	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 50;

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;
	private final ChatMessageRepositoryPort chatMessageRepositoryPort;

	public ListChatMessagesService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort,
			ChatMessageRepositoryPort chatMessageRepositoryPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
		this.chatMessageRepositoryPort = chatMessageRepositoryPort;
	}

	@Override
	public List<ChatMessage> list(Command command) {
		Objects.requireNonNull(command, "List messages command is required.");
		if (command.chatRoomUuid() == null) {
			throw new IllegalArgumentException("chatRoomUuid is required.");
		}
		if (command.requesterUuid() == null) {
			throw new IllegalArgumentException("requesterUuid is required.");
		}
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(command.chatRoomUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		if (room.isHidden()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
		}
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), command.requesterUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		int size = command.size() <= 0 ? DEFAULT_SIZE : Math.min(command.size(), MAX_SIZE);
		return chatMessageRepositoryPort.findByChatRoomUuid(command.chatRoomUuid(), command.before(), size);
	}
}
