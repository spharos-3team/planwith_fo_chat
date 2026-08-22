package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.EnsureChatRoomSubscriberUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatPresencePort;

@Component
public class ChatStompChannelInterceptor implements ChannelInterceptor {

	private final EnsureChatRoomSubscriberUseCase ensureChatRoomSubscriberUseCase;
	private final ObjectProvider<ChatPresencePort> chatPresencePort;

	public ChatStompChannelInterceptor(
			EnsureChatRoomSubscriberUseCase ensureChatRoomSubscriberUseCase,
			ObjectProvider<ChatPresencePort> chatPresencePort
	) {
		this.ensureChatRoomSubscriberUseCase = ensureChatRoomSubscriberUseCase;
		this.chatPresencePort = chatPresencePort;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}
		try {
			if (accessor.getCommand() == StompCommand.CONNECT) {
				ChatMemberPrincipal principal = requirePrincipal(accessor);
				accessor.setUser(principal);
				chatPresencePort.ifAvailable(port -> port.markOnline(principal.memberUuid()));
			}
			else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
				ChatMemberPrincipal principal = requirePrincipal(accessor);
				UUID chatRoomUuid = ChatStompDestinations.parseSubscribeRoom(accessor.getDestination());
				if (chatRoomUuid == null) {
					throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
				}
				ensureChatRoomSubscriberUseCase.ensureCanSubscribe(chatRoomUuid, principal.memberUuid());
			}
			else if (accessor.getCommand() == StompCommand.SEND) {
				requirePrincipal(accessor);
				if (ChatStompDestinations.parseSendRoom(accessor.getDestination()) == null) {
					throw new BusinessException(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
				}
			}
			return message;
		}
		catch (BusinessException exception) {
			throw new MessageDeliveryException(message, exception);
		}
	}

	private ChatMemberPrincipal requirePrincipal(StompHeaderAccessor accessor) {
		if (accessor.getUser() instanceof ChatMemberPrincipal principal) {
			return principal;
		}
		UUID memberUuid = resolveMemberUuid(accessor);
		if (memberUuid == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		ChatMemberPrincipal principal = new ChatMemberPrincipal(memberUuid);
		accessor.setUser(principal);
		return principal;
	}

	private UUID resolveMemberUuid(StompHeaderAccessor accessor) {
		if (accessor.getSessionAttributes() != null) {
			Object attribute = accessor.getSessionAttributes().get(ChatStompDestinations.MEMBER_UUID_ATTRIBUTE);
			if (attribute instanceof UUID uuid) {
				return uuid;
			}
		}
		String header = accessor.getFirstNativeHeader(ChatStompDestinations.USER_ID_HEADER);
		if (!StringUtils.hasText(header)) {
			return null;
		}
		try {
			return UUID.fromString(header.trim());
		}
		catch (IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
	}
}
