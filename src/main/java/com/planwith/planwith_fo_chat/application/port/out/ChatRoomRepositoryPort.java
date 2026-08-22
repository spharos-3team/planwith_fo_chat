package com.planwith.planwith_fo_chat.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

public interface ChatRoomRepositoryPort {

	ChatRoom save(ChatRoom room);

	Optional<ChatRoom> findByMeetingUuid(UUID meetingUuid);

	Optional<ChatRoom> findByChatRoomId(Long chatRoomId);
}
