package com.planwith.planwith_fo_chat.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_chat.application.port.in.GetChatRoomByMeetingUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

public record ChatRoomByMeetingResponse(
		UUID chatRoomUuid,
		UUID meetingUuid,
		String roomName,
		ChatRoomStatus roomStatus
) {

	public static ChatRoomByMeetingResponse from(GetChatRoomByMeetingUseCase.Result result) {
		return new ChatRoomByMeetingResponse(
				result.chatRoomUuid(),
				result.meetingUuid(),
				result.roomName(),
				result.roomStatus()
		);
	}
}
