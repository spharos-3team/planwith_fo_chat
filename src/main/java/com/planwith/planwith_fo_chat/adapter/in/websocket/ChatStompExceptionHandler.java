package com.planwith.planwith_fo_chat.adapter.in.websocket;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;

@ControllerAdvice
public class ChatStompExceptionHandler {

	@MessageExceptionHandler(BusinessException.class)
	@SendToUser("/queue/errors")
	public ChatStompError handleBusinessException(BusinessException exception) {
		return new ChatStompError(exception.getErrorCode().code(), exception.getErrorCode().message());
	}

	public record ChatStompError(String code, String message) {
	}
}
