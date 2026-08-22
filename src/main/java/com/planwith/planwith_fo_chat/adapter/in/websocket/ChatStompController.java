package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.SendRealtimeChatMessageUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

import jakarta.validation.Valid;

@Controller
@Validated
public class ChatStompController {

	private final SendRealtimeChatMessageUseCase sendRealtimeChatMessageUseCase;

	public ChatStompController(SendRealtimeChatMessageUseCase sendRealtimeChatMessageUseCase) {
		this.sendRealtimeChatMessageUseCase = sendRealtimeChatMessageUseCase;
	}

	@MessageMapping(ChatStompDestinations.SEND_MAPPING)
	public void send(
			@DestinationVariable UUID chatRoomUuid,
			@Valid ChatMessageSendRequest request,
			Principal principal
	) {
		if (principal == null || !StringUtils.hasText(principal.getName())) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		sendRealtimeChatMessageUseCase.send(new SendRealtimeChatMessageUseCase.Command(
				chatRoomUuid,
				UUID.fromString(principal.getName()),
				request.messageType(),
				request.content(),
				toFiles(request.files()),
				null
		));
	}

	private List<ChatFile> toFiles(List<ChatMessageSendRequest.ChatFileSendRequest> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		return files.stream()
				.map(file -> new ChatFile(FileType.valueOf(file.fileType().trim().toUpperCase()), file.url(), file.name()))
				.toList();
	}
}
