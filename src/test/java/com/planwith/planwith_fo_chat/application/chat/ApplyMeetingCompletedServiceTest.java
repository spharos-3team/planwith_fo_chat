package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class ApplyMeetingCompletedServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ProcessedChatEventPort processedChatEventPort;

	private ApplyMeetingCompletedService service;

	@BeforeEach
	void setUp() {
		service = new ApplyMeetingCompletedService(chatRoomRepositoryPort, processedChatEventPort);
	}

	@Test
	void endsActiveRoom() {
		UUID meetingUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T14:00:00Z");
		ChatRoom active = new ChatRoom(
				1L,
				UUID.randomUUID(),
				meetingUuid,
				"부산 여행",
				ChatRoomStatus.ACTIVE,
				now,
				now
		);
		when(processedChatEventPort.existsByEventId("e2")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(active));
		when(chatRoomRepositoryPort.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ChatRoom ended = service.apply(new ApplyMeetingCompletedUseCase.Command("e2", meetingUuid, now));

		assertThat(ended.getStatus()).isEqualTo(ChatRoomStatus.ENDED);
		verify(processedChatEventPort).save("e2", "meeting.completed", meetingUuid, now);
	}

	@Test
	void retriesWhenRoomIsMissing() {
		UUID meetingUuid = UUID.randomUUID();
		when(processedChatEventPort.existsByEventId("e2")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.apply(new ApplyMeetingCompletedUseCase.Command(
				"e2",
				meetingUuid,
				Instant.now()
		))).isInstanceOf(ChatRoomNotReadyException.class);
	}
}
