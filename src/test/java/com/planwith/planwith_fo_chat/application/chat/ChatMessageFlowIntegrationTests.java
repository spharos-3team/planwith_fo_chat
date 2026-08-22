package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ListChatMessagesUseCase;
import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.application.port.in.UpdateChatMessageUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatFile;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatMessageFlowIntegrationTests {

	@Autowired
	private ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase;

	@Autowired
	private ApplyMeetingCompletedUseCase applyMeetingCompletedUseCase;

	@Autowired
	private SaveChatMessageUseCase saveChatMessageUseCase;

	@Autowired
	private ListChatMessagesUseCase listChatMessagesUseCase;

	@Autowired
	private UpdateChatMessageUseCase updateChatMessageUseCase;

	@Test
	void savesListsSoftDeletesAndRejectsWriteAfterEnded() {
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T16:10:00Z");
		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"msg-created",
				meetingUuid,
				hostUuid,
				"부산 여행",
				now
		));

		ChatMessage first = saveChatMessageUseCase.save(new SaveChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				"TEXT",
				"안녕하세요",
				List.of(),
				now
		));
		saveChatMessageUseCase.save(new SaveChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				"FILE",
				null,
				List.of(new ChatFile(FileType.IMAGE, "https://example.com/map.png", "map.png")),
				now.plusSeconds(30)
		));
		ChatMessage deleted = updateChatMessageUseCase.delete(new UpdateChatMessageUseCase.DeleteCommand(
				first.getMessageUuid(),
				hostUuid,
				now.plusSeconds(40)
		));
		ChatMessage modified = updateChatMessageUseCase.modify(new UpdateChatMessageUseCase.ModifyCommand(
				deleted.getMessageUuid(),
				hostUuid,
				"안녕하세요!",
				now.plusSeconds(50)
		));

		List<ChatMessage> listed = listChatMessagesUseCase.list(new ListChatMessagesUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				null,
				20
		));
		assertThat(listed).hasSize(2);
		assertThat(listed.get(0).getFiles()).hasSize(1);
		assertThat(listed.get(0).getFiles().get(0).getFileType()).isEqualTo(FileType.IMAGE);
		assertThat(modified.isDeleted()).isTrue();
		assertThat(modified.isModified()).isTrue();

		applyMeetingCompletedUseCase.apply(new ApplyMeetingCompletedUseCase.Command(
				"msg-completed",
				meetingUuid,
				now.plusSeconds(60)
		));
		assertThatThrownBy(() -> saveChatMessageUseCase.save(new SaveChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				"TEXT",
				"끝나면 못 씀",
				List.of(),
				now.plusSeconds(70)
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_ENDED);

		assertThat(listChatMessagesUseCase.list(new ListChatMessagesUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				null,
				20
		))).hasSize(2);
	}
}
