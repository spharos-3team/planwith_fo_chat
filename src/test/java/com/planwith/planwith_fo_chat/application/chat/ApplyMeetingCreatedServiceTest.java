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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@ExtendWith(MockitoExtension.class)
class ApplyMeetingCreatedServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	@Mock
	private ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort;

	@Mock
	private ProcessedChatEventPort processedChatEventPort;

	private ApplyMeetingCreatedService service;

	@BeforeEach
	void setUp() {
		service = new ApplyMeetingCreatedService(
				chatRoomRepositoryPort,
				chatMemberRepositoryPort,
				chatRoomMemberReadRepositoryPort,
				processedChatEventPort
		);
	}

	@Test
	void createsRoomWithMeetingTitleAndHost() {
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T13:24:00Z");
		ChatRoom created = ChatRoom.create(meetingUuid, "부산 2박 3일 여행", now);
		ChatRoom persisted = new ChatRoom(
				10L,
				created.getChatRoomUuid(),
				meetingUuid,
				created.getRoomName(),
				created.getStatus(),
				now,
				now
		);
		when(processedChatEventPort.existsByEventId("e1")).thenReturn(false);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.empty());
		when(chatRoomRepositoryPort.save(any(ChatRoom.class))).thenReturn(persisted);
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(10L, hostUuid)).thenReturn(Optional.empty());
		when(chatMemberRepositoryPort.save(any(ChatMember.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(chatRoomMemberReadRepositoryPort.findByMemberUuidAndChatRoomUuid(hostUuid, persisted.getChatRoomUuid()))
				.thenReturn(Optional.empty());
		when(chatRoomMemberReadRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		ChatRoom result = service.apply(new ApplyMeetingCreatedUseCase.Command(
				"e1",
				meetingUuid,
				hostUuid,
				"부산 2박 3일 여행",
				now
		));

		assertThat(result.getRoomName()).isEqualTo("부산 2박 3일 여행");
		assertThat(result.getMeetingUuid()).isEqualTo(meetingUuid);
		ArgumentCaptor<ChatMember> memberCaptor = ArgumentCaptor.forClass(ChatMember.class);
		verify(chatMemberRepositoryPort).save(memberCaptor.capture());
		assertThat(memberCaptor.getValue().getMemberUuid()).isEqualTo(hostUuid);
		assertThat(memberCaptor.getValue().getStatus().name()).isEqualTo("APPROVED");
		verify(processedChatEventPort).save("e1", "meeting.created", meetingUuid, now);
	}

	@Test
	void ignoresDuplicateEventId() {
		UUID meetingUuid = UUID.randomUUID();
		when(processedChatEventPort.existsByEventId("e1")).thenReturn(true);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.empty());

		service.apply(new ApplyMeetingCreatedUseCase.Command(
				"e1",
				meetingUuid,
				UUID.randomUUID(),
				"제주 여행",
				Instant.now()
		));

		verify(chatRoomRepositoryPort, never()).save(any());
	}

	@Test
	void rejectsBlankTitle() {
		assertThatThrownBy(() -> service.apply(new ApplyMeetingCreatedUseCase.Command(
				"e1",
				UUID.randomUUID(),
				UUID.randomUUID(),
				"  ",
				Instant.now()
		))).isInstanceOf(IllegalArgumentException.class);
	}
}
