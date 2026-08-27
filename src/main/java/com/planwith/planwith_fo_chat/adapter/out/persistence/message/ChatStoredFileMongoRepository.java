package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatStoredFileMongoRepository extends MongoRepository<ChatStoredFileDocument, String> {

	Optional<ChatStoredFileDocument> findByFileUuid(String fileUuid);
}
