package com.planwith.planwith_fo_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.planwith.planwith_fo_chat.adapter.in.websocket.ChatHandshakeInterceptor;
import com.planwith.planwith_fo_chat.adapter.in.websocket.ChatStompChannelInterceptor;
import com.planwith.planwith_fo_chat.adapter.in.websocket.ChatStompDestinations;

@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final ChatHandshakeInterceptor chatHandshakeInterceptor;
	private final ChatStompChannelInterceptor chatStompChannelInterceptor;

	public ChatWebSocketConfig(
			ChatHandshakeInterceptor chatHandshakeInterceptor,
			ChatStompChannelInterceptor chatStompChannelInterceptor
	) {
		this.chatHandshakeInterceptor = chatHandshakeInterceptor;
		this.chatStompChannelInterceptor = chatStompChannelInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint(ChatStompDestinations.ENDPOINT)
				.addInterceptors(chatHandshakeInterceptor)
				.setAllowedOriginPatterns("*");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes(ChatStompDestinations.APP_PREFIX);
		registry.enableSimpleBroker("/chat", "/queue");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(chatStompChannelInterceptor);
	}
}
