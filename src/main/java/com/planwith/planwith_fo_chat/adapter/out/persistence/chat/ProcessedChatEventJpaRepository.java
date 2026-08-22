package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedChatEventJpaRepository extends JpaRepository<ProcessedChatEventJpaEntity, Long> {

	boolean existsByEventId(String eventId);
}
