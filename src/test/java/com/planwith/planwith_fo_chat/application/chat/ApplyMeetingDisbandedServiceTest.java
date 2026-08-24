package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingDisbandedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class ApplyMeetingDisbandedServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ProcessedChatEventPort processedChatEventPort;

	private ApplyMeetingDisbandedService service;

	@BeforeEach
	void setUp() {
		service = new ApplyMeetingDisbandedService(chatRoomRepositoryPort, processedChatEventPort);
	}

	@Test
	void hidesActiveRoom() {
		UUID meetingUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T00:20:00Z");
		ChatRoom active = new ChatRoom(
				1L,
				UUID.randomUUID(),
				meetingUuid,
				"부산 여행",
				ChatRoomStatus.ACTIVE,
				now,
				now
		);
		when(processedChatEventPort.existsByEventId("d1")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(active));
		when(chatRoomRepositoryPort.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ChatRoom hidden = service.apply(new ApplyMeetingDisbandedUseCase.Command("d1", meetingUuid, now));

		assertThat(hidden.getStatus()).isEqualTo(ChatRoomStatus.DISBANDED);
		assertThat(hidden.getChatRoomId()).isEqualTo(1L);
		verify(processedChatEventPort).save("d1", "meeting.disbanded", meetingUuid, now);
	}

	@Test
	void hidesEndedRoomWithoutDeleting() {
		UUID meetingUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T00:20:00Z");
		ChatRoom ended = new ChatRoom(
				2L,
				UUID.randomUUID(),
				meetingUuid,
				"제주 여행",
				ChatRoomStatus.ENDED,
				now,
				now
		);
		when(processedChatEventPort.existsByEventId("d2")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(ended));
		when(chatRoomRepositoryPort.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ChatRoom hidden = service.apply(new ApplyMeetingDisbandedUseCase.Command("d2", meetingUuid, now));

		assertThat(hidden.getStatus()).isEqualTo(ChatRoomStatus.DISBANDED);
		assertThat(hidden.getChatRoomId()).isEqualTo(2L);
	}

	@Test
	void duplicateEventDoesNotSaveAgain() {
		UUID meetingUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T00:20:00Z");
		ChatRoom hidden = new ChatRoom(
				3L,
				UUID.randomUUID(),
				meetingUuid,
				"숨김 방",
				ChatRoomStatus.DISBANDED,
				now,
				now
		);
		when(processedChatEventPort.existsByEventId("d3")).thenReturn(true);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(hidden));

		ChatRoom result = service.apply(new ApplyMeetingDisbandedUseCase.Command("d3", meetingUuid, now));

		assertThat(result.getStatus()).isEqualTo(ChatRoomStatus.DISBANDED);
		verify(chatRoomRepositoryPort, never()).save(any());
		verify(processedChatEventPort, never()).save(any(), any(), any(), any());
	}

	@Test
	void retriesWhenRoomIsMissing() {
		UUID meetingUuid = UUID.randomUUID();
		when(processedChatEventPort.existsByEventId("d4")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.apply(new ApplyMeetingDisbandedUseCase.Command(
				"d4",
				meetingUuid,
				Instant.now()
		))).isInstanceOf(ChatRoomNotReadyException.class);
	}
}
