package com.planwith.planwith_fo_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.planwith.planwith_fo_chat.adapter.out.persistence.message.ChatMessageMongoRepository;

@Configuration
@Profile("!test")
@EnableMongoRepositories(basePackageClasses = ChatMessageMongoRepository.class)
public class ChatMongoConfig {
}
