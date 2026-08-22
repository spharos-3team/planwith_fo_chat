package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		String userId = request.getHeaders().getFirst(ChatStompDestinations.USER_ID_HEADER);
		if (!StringUtils.hasText(userId) && request instanceof ServletServerHttpRequest servletRequest) {
			userId = servletRequest.getServletRequest().getHeader(ChatStompDestinations.USER_ID_HEADER);
		}
		if (StringUtils.hasText(userId)) {
			try {
				attributes.put(ChatStompDestinations.MEMBER_UUID_ATTRIBUTE, UUID.fromString(userId.trim()));
			}
			catch (IllegalArgumentException ignored) {
				// CONNECT interceptor rejects invalid identity.
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception
	) {
		// no-op
	}
}
