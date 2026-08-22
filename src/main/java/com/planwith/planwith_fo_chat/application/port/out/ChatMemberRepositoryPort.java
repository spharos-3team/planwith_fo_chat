package com.planwith.planwith_fo_chat.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;

public interface ChatMemberRepositoryPort {

	ChatMember save(ChatMember member);

	Optional<ChatMember> findByChatRoomIdAndMemberUuid(Long chatRoomId, UUID memberUuid);

	List<ChatMember> findByChatRoomIdAndStatus(Long chatRoomId, ChatMemberStatus status);
}
