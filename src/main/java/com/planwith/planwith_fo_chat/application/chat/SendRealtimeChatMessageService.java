package com.planwith.planwith_fo_chat.application.chat;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.in.SendRealtimeChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageBrokerPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

@Service
public class SendRealtimeChatMessageService implements SendRealtimeChatMessageUseCase {

	private final SaveChatMessageUseCase saveChatMessageUseCase;
	private final ChatMessageBrokerPort chatMessageBrokerPort;

	public SendRealtimeChatMessageService(
			SaveChatMessageUseCase saveChatMessageUseCase,
			ChatMessageBrokerPort chatMessageBrokerPort
	) {
		this.saveChatMessageUseCase = saveChatMessageUseCase;
		this.chatMessageBrokerPort = chatMessageBrokerPort;
	}

	@Override
	public ChatMessage send(Command command) {
		Objects.requireNonNull(command, "Send realtime command is required.");
		ChatMessage saved = saveChatMessageUseCase.save(new SaveChatMessageUseCase.Command(
				command.chatRoomUuid(),
				command.senderUuid(),
				command.messageType(),
				command.content(),
				command.files(),
				command.occurredAt()
		));
		chatMessageBrokerPort.publish(ChatRealtimePayload.from(saved));
		return saved;
	}
}
