package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.in.SendRealtimeChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageBrokerPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;

@ExtendWith(MockitoExtension.class)
class SendRealtimeChatMessageServiceTest {

	@Mock
	private SaveChatMessageUseCase saveChatMessageUseCase;

	@Mock
	private ChatMessageBrokerPort chatMessageBrokerPort;

	private SendRealtimeChatMessageService service;

	@BeforeEach
	void setUp() {
		service = new SendRealtimeChatMessageService(saveChatMessageUseCase, chatMessageBrokerPort);
	}

	@Test
	void savesThenPublishes() {
		UUID roomUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T16:40:00Z");
		ChatMessage saved = ChatMessage.create(roomUuid, senderUuid, "TEXT", "안녕", List.of(), now);
		when(saveChatMessageUseCase.save(any())).thenReturn(saved);

		ChatMessage result = service.send(new SendRealtimeChatMessageUseCase.Command(
				roomUuid,
				senderUuid,
				"TEXT",
				"안녕",
				List.of(),
				now
		));

		assertThat(result.getContent()).isEqualTo("안녕");
		ArgumentCaptor<ChatRealtimePayload> captor = ArgumentCaptor.forClass(ChatRealtimePayload.class);
		verify(chatMessageBrokerPort).publish(captor.capture());
		assertThat(captor.getValue().chatRoomUuid()).isEqualTo(roomUuid);
		assertThat(captor.getValue().content()).isEqualTo("안녕");
	}

	@Test
	void doesNotPublishWhenSaveRejected() {
		when(saveChatMessageUseCase.save(any())).thenThrow(new BusinessException(ErrorCode.CHAT_ROOM_ENDED));

		assertThatThrownBy(() -> service.send(new SendRealtimeChatMessageUseCase.Command(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"TEXT",
				"끝",
				List.of(),
				Instant.now()
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_ENDED);
		verify(chatMessageBrokerPort, never()).publish(any());
	}
}
