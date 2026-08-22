package com.planwith.planwith_fo_chat.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMember;

public interface ChatMemberRepositoryPort {

	ChatMember save(ChatMember member);

	Optional<ChatMember> findByChatRoomIdAndMemberUuid(Long chatRoomId, UUID memberUuid);
}
