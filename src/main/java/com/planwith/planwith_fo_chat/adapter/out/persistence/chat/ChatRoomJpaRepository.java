package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

	Optional<ChatRoomJpaEntity> findByMeetingUuid(String meetingUuid);
}
