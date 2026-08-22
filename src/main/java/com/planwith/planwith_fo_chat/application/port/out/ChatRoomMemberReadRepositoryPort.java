package com.planwith.planwith_fo_chat.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

public interface ChatRoomMemberReadRepositoryPort {

	ChatRoomMemberRead save(ChatRoomMemberRead read);

	Optional<ChatRoomMemberRead> findByMemberUuidAndChatRoomUuid(UUID memberUuid, UUID chatRoomUuid);

	List<ChatRoomMemberRead> findApprovedInbox(
			UUID memberUuid,
			Instant cursorAt,
			UUID cursorChatRoomUuid,
			int size
	);
}
