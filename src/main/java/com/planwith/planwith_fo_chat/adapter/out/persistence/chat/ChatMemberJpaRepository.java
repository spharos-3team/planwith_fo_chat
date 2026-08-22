package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;

public interface ChatMemberJpaRepository extends JpaRepository<ChatMemberJpaEntity, Long> {

	Optional<ChatMemberJpaEntity> findByChatRoom_ChatRoomIdAndMemberUuid(Long chatRoomId, String memberUuid);

	List<ChatMemberJpaEntity> findByChatRoom_ChatRoomIdAndStatus(Long chatRoomId, ChatMemberStatus status);
}
