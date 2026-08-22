package com.planwith.planwith_fo_chat.adapter.out.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_chat.application.port.in.ApplyChatMessageCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageCreatedEventPort;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalChatMessageCreatedEventAdapter implements ChatMessageCreatedEventPort {

	private final ApplyChatMessageCreatedUseCase applyChatMessageCreatedUseCase;

	public LocalChatMessageCreatedEventAdapter(ApplyChatMessageCreatedUseCase applyChatMessageCreatedUseCase) {
		this.applyChatMessageCreatedUseCase = applyChatMessageCreatedUseCase;
	}

	@Override
	public void publish(Event event) {
		applyChatMessageCreatedUseCase.apply(new ApplyChatMessageCreatedUseCase.Command(
				event.eventId(),
				event.chatRoomUuid(),
				event.messageUuid(),
				event.senderUuid(),
				event.content(),
				event.occurredAt()
		));
	}
}
