package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {

	Optional<ChatMessageDocument> findByMessageUuid(String messageUuid);

	List<ChatMessageDocument> findByChatRoomUuidAndCreatedAtLessThanOrderByCreatedAtDesc(
			String chatRoomUuid,
			Instant before,
			Pageable pageable
	);

	List<ChatMessageDocument> findByChatRoomUuidOrderByCreatedAtDesc(String chatRoomUuid, Pageable pageable);
}
