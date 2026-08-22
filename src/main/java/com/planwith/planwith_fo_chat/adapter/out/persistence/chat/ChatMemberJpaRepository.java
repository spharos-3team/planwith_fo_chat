package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberJpaRepository extends JpaRepository<ChatMemberJpaEntity, Long> {

	Optional<ChatMemberJpaEntity> findByChatRoom_ChatRoomIdAndMemberUuid(Long chatRoomId, String memberUuid);
}
