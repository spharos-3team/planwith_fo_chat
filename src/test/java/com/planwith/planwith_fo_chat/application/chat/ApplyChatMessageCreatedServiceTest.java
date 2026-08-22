package com.planwith.planwith_fo_chat.application.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.planwith.planwith_fo_chat.application.port.in.ApplyChatMessageCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ProcessedChatEventPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@ExtendWith(MockitoExtension.class)
class ApplyChatMessageCreatedServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	@Mock
	private ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort;

	@Mock
	private ProcessedChatEventPort processedChatEventPort;

	private ApplyChatMessageCreatedService service;

	@BeforeEach
	void setUp() {
		service = new ApplyChatMessageCreatedService(
				chatRoomRepositoryPort,
				chatMemberRepositoryPort,
				chatRoomMemberReadRepositoryPort,
				processedChatEventPort
		);
	}

	@Test
	void incrementsUnreadForApprovedOthersOnly() {
		UUID roomUuid = UUID.randomUUID();
		UUID senderUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T17:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산", ChatRoomStatus.ACTIVE, now, now);
		when(processedChatEventPort.existsByEventId("msg-1")).thenReturn(false);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndStatus(1L, ChatMemberStatus.APPROVED)).thenReturn(List.of(
				ChatMember.host(1L, senderUuid, now),
				ChatMember.of(1L, otherUuid, ChatMemberStatus.APPROVED, now)
		));
		when(chatRoomMemberReadRepositoryPort.findByMemberUuidAndChatRoomUuid(any(), eq(roomUuid)))
				.thenReturn(Optional.empty());
		when(chatRoomMemberReadRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.apply(new ApplyChatMessageCreatedUseCase.Command(
				"msg-1",
				roomUuid,
				UUID.randomUUID(),
				senderUuid,
				"안녕",
				now
		));

		verify(chatRoomMemberReadRepositoryPort, times(2)).save(any());
		verify(processedChatEventPort).save("msg-1", "chat.message.created", roomUuid, now);
	}

	@Test
	void ignoresDuplicateEventId() {
		when(processedChatEventPort.existsByEventId("msg-1")).thenReturn(true);

		service.apply(new ApplyChatMessageCreatedUseCase.Command(
				"msg-1",
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"안녕",
				Instant.now()
		));

		verify(chatRoomRepositoryPort, never()).findByChatRoomUuid(any());
		verify(chatRoomMemberReadRepositoryPort, never()).save(any());
	}

	@Test
	void doesNotRewindLastMessageAt() {
		UUID roomUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant newer = Instant.parse("2026-08-22T17:10:00Z");
		Instant older = Instant.parse("2026-08-22T17:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산", ChatRoomStatus.ACTIVE, older, older);
		ChatRoomMemberRead current = ChatRoomMemberRead.empty(memberUuid, roomUuid, "부산", older)
				.applyMessage(UUID.randomUUID(), "새 메시지", memberUuid, newer, false);
		when(processedChatEventPort.existsByEventId("msg-old")).thenReturn(false);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndStatus(1L, ChatMemberStatus.APPROVED))
				.thenReturn(List.of(ChatMember.of(1L, memberUuid, ChatMemberStatus.APPROVED, older)));
		when(chatRoomMemberReadRepositoryPort.findByMemberUuidAndChatRoomUuid(memberUuid, roomUuid))
				.thenReturn(Optional.of(current));

		service.apply(new ApplyChatMessageCreatedUseCase.Command(
				"msg-old",
				roomUuid,
				UUID.randomUUID(),
				UUID.randomUUID(),
				"과거",
				older
		));

		verify(chatRoomMemberReadRepositoryPort, never()).save(any());
		verify(processedChatEventPort).save("msg-old", "chat.message.created", roomUuid, older);
	}
}
