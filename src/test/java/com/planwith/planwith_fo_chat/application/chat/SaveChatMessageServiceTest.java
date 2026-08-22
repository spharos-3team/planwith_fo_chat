package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMessageRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class SaveChatMessageServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	@Mock
	private ChatMessageRepositoryPort chatMessageRepositoryPort;

	private SaveChatMessageService service;

	@BeforeEach
	void setUp() {
		service = new SaveChatMessageService(
				chatRoomRepositoryPort,
				chatMemberRepositoryPort,
				chatMessageRepositoryPort
		);
	}

	@Test
	void savesWhenRoomIsActiveAndSenderApproved() {
		UUID roomUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T16:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산 여행", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, senderUuid))
				.thenReturn(Optional.of(ChatMember.host(1L, senderUuid, now)));
		when(chatMessageRepositoryPort.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ChatMessage saved = service.save(new SaveChatMessageUseCase.Command(
				roomUuid,
				senderUuid,
				"TEXT",
				"내일 2시에 만나요",
				List.of(),
				now
		));

		assertThat(saved.getContent()).isEqualTo("내일 2시에 만나요");
		assertThat(saved.isDeleted()).isFalse();
		verify(chatMessageRepositoryPort).save(any(ChatMessage.class));
	}

	@Test
	void rejectsEndedRoom() {
		UUID roomUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산 여행", ChatRoomStatus.ENDED, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));

		assertThatThrownBy(() -> service.save(new SaveChatMessageUseCase.Command(
				roomUuid,
				senderUuid,
				"TEXT",
				"hi",
				List.of(),
				now
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_ENDED);
	}

	@Test
	void rejectsNonApprovedMember() {
		UUID roomUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();
		Instant now = Instant.now();
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산 여행", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, senderUuid))
				.thenReturn(Optional.of(ChatMember.of(1L, senderUuid, ChatMemberStatus.PENDING, now)));

		assertThatThrownBy(() -> service.save(new SaveChatMessageUseCase.Command(
				roomUuid,
				senderUuid,
				"TEXT",
				"hi",
				List.of(),
				now
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
	}
}
