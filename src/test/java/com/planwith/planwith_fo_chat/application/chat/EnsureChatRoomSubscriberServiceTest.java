package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThatCode;
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
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class EnsureChatRoomSubscriberServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	private EnsureChatRoomSubscriberService service;

	@BeforeEach
	void setUp() {
		service = new EnsureChatRoomSubscriberService(chatRoomRepositoryPort, chatMemberRepositoryPort);
	}

	@Test
	void allowsApprovedMemberIncludingEndedRoom() {
		UUID roomUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산", ChatRoomStatus.ENDED, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, memberUuid))
				.thenReturn(Optional.of(ChatMember.host(1L, memberUuid, now)));

		assertThatCode(() -> service.ensureCanSubscribe(roomUuid, memberUuid)).doesNotThrowAnyException();
	}

	@Test
	void rejectsNonApprovedMember() {
		UUID roomUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, memberUuid))
				.thenReturn(Optional.of(ChatMember.of(1L, memberUuid, ChatMemberStatus.PENDING, now)));

		assertThatThrownBy(() -> service.ensureCanSubscribe(roomUuid, memberUuid))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
	}

	@Test
	void rejectsDisbandedRoomAsNotFound() {
		UUID roomUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산", ChatRoomStatus.DISBANDED, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));

		assertThatThrownBy(() -> service.ensureCanSubscribe(roomUuid, memberUuid))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
	}
}
