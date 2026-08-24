package com.planwith.planwith_fo_chat.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

public interface GetChatRoomByMeetingUseCase {

	Result get(Command command);

	record Command(
			UUID meetingUuid,
			UUID memberUuid
	) {
	}

	record Result(
			UUID chatRoomUuid,
			UUID meetingUuid,
			String roomName,
			ChatRoomStatus roomStatus
	) {
	}
}
