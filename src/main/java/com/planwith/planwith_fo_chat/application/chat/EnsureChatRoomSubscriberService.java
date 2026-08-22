package com.planwith.planwith_fo_chat.application.chat;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.EnsureChatRoomSubscriberUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
public class EnsureChatRoomSubscriberService implements EnsureChatRoomSubscriberUseCase {

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;

	public EnsureChatRoomSubscriberService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
	}

	@Override
	public void ensureCanSubscribe(UUID chatRoomUuid, UUID memberUuid) {
		Objects.requireNonNull(chatRoomUuid, "chatRoomUuid is required.");
		Objects.requireNonNull(memberUuid, "memberUuid is required.");
		ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(chatRoomUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
	}
}
