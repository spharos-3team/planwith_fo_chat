package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.GetChatRoomByMeetingUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class GetChatRoomByMeetingServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	private GetChatRoomByMeetingService service;

	@BeforeEach
	void setUp() {
		service = new GetChatRoomByMeetingService(chatRoomRepositoryPort, chatMemberRepositoryPort);
	}

	@Test
	void returnsRoomForApprovedMemberIncludingEnded() {
		UUID meetingUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		UUID roomUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, meetingUuid, "부산", ChatRoomStatus.ENDED, now, now);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, memberUuid))
				.thenReturn(Optional.of(ChatMember.host(1L, memberUuid, now)));

		GetChatRoomByMeetingUseCase.Result result = service.get(new GetChatRoomByMeetingUseCase.Command(
				meetingUuid,
				memberUuid
		));

		assertThat(result.chatRoomUuid()).isEqualTo(roomUuid);
		assertThat(result.meetingUuid()).isEqualTo(meetingUuid);
		assertThat(result.roomName()).isEqualTo("부산");
		assertThat(result.roomStatus()).isEqualTo(ChatRoomStatus.ENDED);
	}

	@Test
	void rejectsNonApprovedMember() {
		UUID meetingUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, UUID.randomUUID(), meetingUuid, "부산", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, memberUuid))
				.thenReturn(Optional.of(ChatMember.of(1L, memberUuid, ChatMemberStatus.PENDING, now)));

		assertThatThrownBy(() -> service.get(new GetChatRoomByMeetingUseCase.Command(meetingUuid, memberUuid)))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
	}

	@Test
	void rejectsDisbandedRoomAsNotFound() {
		UUID meetingUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, UUID.randomUUID(), meetingUuid, "부산", ChatRoomStatus.DISBANDED, now, now);
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.of(room));

		assertThatThrownBy(() -> service.get(new GetChatRoomByMeetingUseCase.Command(meetingUuid, UUID.randomUUID())))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
	}

	@Test
	void rejectsUnknownMeetingAsNotFound() {
		UUID meetingUuid = UUID.randomUUID();
		when(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(new GetChatRoomByMeetingUseCase.Command(meetingUuid, UUID.randomUUID())))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
	}
}
