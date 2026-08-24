package com.planwith.planwith_fo_chat.application.chat;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.GetChatRoomByMeetingUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@Service
@Transactional(readOnly = true)
public class GetChatRoomByMeetingService implements GetChatRoomByMeetingUseCase {

	private final ChatRoomRepositoryPort chatRoomRepositoryPort;
	private final ChatMemberRepositoryPort chatMemberRepositoryPort;

	public GetChatRoomByMeetingService(
			ChatRoomRepositoryPort chatRoomRepositoryPort,
			ChatMemberRepositoryPort chatMemberRepositoryPort
	) {
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
		this.chatMemberRepositoryPort = chatMemberRepositoryPort;
	}

	@Override
	public Result get(Command command) {
		Objects.requireNonNull(command, "Get chat room by meeting command is required.");
		if (command.meetingUuid() == null) {
			throw new IllegalArgumentException("meetingUuid is required.");
		}
		if (command.memberUuid() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		ChatRoom room = chatRoomRepositoryPort.findByMeetingUuid(command.meetingUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
		if (room.isHidden()) {
			throw new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND);
		}
		ChatMember member = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), command.memberUuid())
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED));
		if (!member.getStatus().isApproved()) {
			throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		}
		return new Result(room.getChatRoomUuid(), room.getMeetingUuid(), room.getRoomName(), room.getStatus());
	}
}
