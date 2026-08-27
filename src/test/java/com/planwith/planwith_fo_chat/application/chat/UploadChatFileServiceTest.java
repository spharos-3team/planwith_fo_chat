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
import org.springframework.mock.web.MockMultipartFile;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.out.ChatFileStoragePort;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatStoredFile;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

@ExtendWith(MockitoExtension.class)
class UploadChatFileServiceTest {

	@Mock
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Mock
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	@Mock
	private ChatFileStoragePort chatFileStoragePort;

	private UploadChatFileService service;

	@BeforeEach
	void setUp() {
		service = new UploadChatFileService(
				chatRoomRepositoryPort,
				chatMemberRepositoryPort,
				chatFileStoragePort
		);
	}

	@Test
	void storesApprovedMemberFile() {
		UUID roomUuid = UUID.randomUUID();
		UUID uploaderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-27T04:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산 여행", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, uploaderUuid))
				.thenReturn(Optional.of(ChatMember.host(1L, uploaderUuid, now)));
		when(chatFileStoragePort.save(any(ChatStoredFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ChatStoredFile stored = service.upload(
				roomUuid,
				uploaderUuid,
				new MockMultipartFile("file", "map.png", "image/png", new byte[] {1, 2, 3})
		);

		assertThat(stored.getFileType()).isEqualTo(FileType.IMAGE);
		assertThat(stored.getName()).isEqualTo("map.png");
		assertThat(stored.publicUrl()).isEqualTo("/api/v1/chat-rooms/" + roomUuid + "/files/" + stored.getFileUuid());
		verify(chatFileStoragePort).save(any(ChatStoredFile.class));
	}

	@Test
	void rejectsEndedRoom() {
		UUID roomUuid = UUID.randomUUID();
		UUID uploaderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-27T04:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "끝난 방", ChatRoomStatus.ENDED, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));

		assertThatThrownBy(() -> service.upload(
				roomUuid,
				uploaderUuid,
				new MockMultipartFile("file", "a.txt", "text/plain", "hi".getBytes())
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_ENDED);
	}

	@Test
	void rejectsOversizedFile() {
		UUID roomUuid = UUID.randomUUID();
		UUID uploaderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-27T04:00:00Z");
		ChatRoom room = new ChatRoom(1L, roomUuid, UUID.randomUUID(), "부산 여행", ChatRoomStatus.ACTIVE, now, now);
		when(chatRoomRepositoryPort.findByChatRoomUuid(roomUuid)).thenReturn(Optional.of(room));
		when(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(1L, uploaderUuid))
				.thenReturn(Optional.of(ChatMember.host(1L, uploaderUuid, now)));

		assertThatThrownBy(() -> service.upload(
				roomUuid,
				uploaderUuid,
				new MockMultipartFile("file", "big.bin", "application/octet-stream", new byte[(int) UploadChatFileService.MAX_BYTES + 1])
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_CHAT_FILE);
	}
}
