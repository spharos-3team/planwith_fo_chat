package com.planwith.planwith_fo_chat.adapter.in.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.planwith.planwith_fo_chat.application.port.in.EnsureChatRoomSubscriberUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatPresencePort;

@ExtendWith(MockitoExtension.class)
class ChatStompChannelInterceptorTest {

	@Mock
	private EnsureChatRoomSubscriberUseCase ensureChatRoomSubscriberUseCase;

	@Mock
	private ObjectProvider<ChatPresencePort> chatPresencePort;

	private ChatStompChannelInterceptor interceptor;

	@BeforeEach
	void setUp() {
		interceptor = new ChatStompChannelInterceptor(ensureChatRoomSubscriberUseCase, chatPresencePort);
	}

	@Test
	void connectSetsPrincipalFromHeader() {
		UUID memberUuid = UUID.randomUUID();
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setNativeHeader(ChatStompDestinations.USER_ID_HEADER, memberUuid.toString());
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		interceptor.preSend(message, (payload, timeout) -> true);

		assertThat(accessor.getUser()).isInstanceOf(ChatMemberPrincipal.class);
		assertThat(accessor.getUser().getName()).isEqualTo(memberUuid.toString());
	}

	@Test
	void subscribeChecksMembership() {
		UUID memberUuid = UUID.randomUUID();
		UUID roomUuid = UUID.randomUUID();
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
		accessor.setDestination(ChatStompDestinations.subscribe(roomUuid));
		accessor.setUser(new ChatMemberPrincipal(memberUuid));
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		interceptor.preSend(message, (payload, timeout) -> true);

		verify(ensureChatRoomSubscriberUseCase).ensureCanSubscribe(roomUuid, memberUuid);
	}

	@Test
	void connectWithoutIdentityIsRejected() {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setSessionAttributes(new HashMap<>());
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		assertThatThrownBy(() -> interceptor.preSend(message, (payload, timeout) -> true))
				.isInstanceOf(MessageDeliveryException.class);
		verifyNoInteractions(ensureChatRoomSubscriberUseCase);
	}

	@Test
	void subscribeUnknownDestinationIsRejected() {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
		accessor.setDestination("/topic/other");
		accessor.setUser(new ChatMemberPrincipal(UUID.randomUUID()));
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		assertThatThrownBy(() -> interceptor.preSend(message, (payload, timeout) -> true))
				.isInstanceOf(MessageDeliveryException.class);
	}

	@Test
	void connectReadsHandshakeAttribute() {
		UUID memberUuid = UUID.randomUUID();
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setSessionAttributes(Map.of(ChatStompDestinations.MEMBER_UUID_ATTRIBUTE, memberUuid));
		accessor.setLeaveMutable(true);
		Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		interceptor.preSend(message, (payload, timeout) -> true);

		assertThat(accessor.getUser().getName()).isEqualTo(memberUuid.toString());
	}
}
