package com.planwith.planwith_fo_chat.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

public interface ListChatRoomsUseCase {

	Result list(Command command);

	record Command(
			UUID memberUuid,
			Instant cursorAt,
			UUID cursorChatRoomUuid,
			int size
	) {
	}

	record Result(
			List<Item> items,
			Instant nextCursorAt,
			UUID nextCursorChatRoomUuid
	) {
	}

	record Item(
			ChatRoomMemberRead read,
			ChatRoomStatus roomStatus
	) {
	}
}
