package com.planwith.planwith_fo_chat.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.planwith.planwith_fo_chat.adapter.out.redis.RedisChatMessageSubscriber;

@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class ChatRedisConfig {

	@Bean
	LettuceConnectionFactory chatRedisConnectionFactory(ChatRedisProperties properties) {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
				properties.host(),
				properties.port()
		);
		return new LettuceConnectionFactory(configuration);
	}

	@Bean
	StringRedisTemplate stringRedisTemplate(RedisConnectionFactory chatRedisConnectionFactory) {
		return new StringRedisTemplate(chatRedisConnectionFactory);
	}

	@Bean
	RedisMessageListenerContainer chatRedisMessageListenerContainer(
			RedisConnectionFactory chatRedisConnectionFactory,
			RedisChatMessageSubscriber redisChatMessageSubscriber,
			ChatRedisProperties properties
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(chatRedisConnectionFactory);
		container.addMessageListener(redisChatMessageSubscriber, new PatternTopic(properties.channelPrefix() + "*"));
		return container;
	}
}
