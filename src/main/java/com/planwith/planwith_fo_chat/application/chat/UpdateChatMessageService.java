package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.UpdateChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

@Service
public class UpdateChatMessageService implements UpdateChatMessageUseCase {

	private final ChatMessageRepositoryPort chatMessageRepositoryPort;

	public UpdateChatMessageService(ChatMessageRepositoryPort chatMessageRepositoryPort) {
		this.chatMessageRepositoryPort = chatMessageRepositoryPort;
	}

	@Override
	public ChatMessage modify(ModifyCommand command) {
		Objects.requireNonNull(command, "Modify command is required.");
		if (!StringUtils.hasText(command.content())) {
			throw new IllegalArgumentException("content is required.");
		}
		ChatMessage message = requireOwnMessage(command.messageUuid(), command.requesterUuid());
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();
		return chatMessageRepositoryPort.save(message.markModified(command.content().trim(), now));
	}

	@Override
	public ChatMessage delete(DeleteCommand command) {
		Objects.requireNonNull(command, "Delete command is required.");
		ChatMessage message = requireOwnMessage(command.messageUuid(), command.requesterUuid());
		Instant now = command.occurredAt() != null ? command.occurredAt() : Instant.now();
		return chatMessageRepositoryPort.save(message.markDeleted(now));
	}

	private ChatMessage requireOwnMessage(java.util.UUID messageUuid, java.util.UUID requesterUuid) {
		if (messageUuid == null) {
			throw new IllegalArgumentException("messageUuid is required.");
		}
		if (requesterUuid == null) {
			throw new IllegalArgumentException("requesterUuid is required.");
		}
		ChatMessage message = chatMessageRepositoryPort.findByMessageUuid(messageUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
		if (!message.isSender(requesterUuid)) {
			throw new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_SENDER);
		}
		return message;
	}
}
