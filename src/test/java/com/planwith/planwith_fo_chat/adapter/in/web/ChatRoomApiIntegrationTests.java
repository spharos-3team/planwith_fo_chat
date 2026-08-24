package com.planwith.planwith_fo_chat.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.port.in.ApplyChatMessageCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingDisbandedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.SaveChatMessageUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatRoomApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase;

	@Autowired
	private ApplyMeetingParticipationChangedUseCase applyMeetingParticipationChangedUseCase;

	@Autowired
	private SaveChatMessageUseCase saveChatMessageUseCase;

	@Autowired
	private ApplyChatMessageCreatedUseCase applyChatMessageCreatedUseCase;

	@Autowired
	private ApplyMeetingDisbandedUseCase applyMeetingDisbandedUseCase;

	@Test
	void listsCursorUnreadAndMarkRead() throws Exception {
		UUID hostUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T17:20:00Z");
		ChatRoom first = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"list-created-1",
				UUID.randomUUID(),
				hostUuid,
				"첫번째 방",
				now
		));
		ChatRoom second = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"list-created-2",
				UUID.randomUUID(),
				hostUuid,
				"두번째 방",
				now.plusSeconds(10)
		));
		applyMeetingParticipationChangedUseCase.apply(new ApplyMeetingParticipationChangedUseCase.Command(
				"list-join",
				first.getMeetingUuid(),
				memberUuid,
				"APPROVED",
				now
		));

		mockMvc.perform(get("/api/v1/chat-rooms")
						.header("X-Auth-User-Id", hostUuid)
						.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].chatRoomUuid").value(second.getChatRoomUuid().toString()))
				.andExpect(jsonPath("$.data.content[0].meetingUuid").value(second.getMeetingUuid().toString()))
				.andExpect(jsonPath("$.data.nextCursorChatRoomUuid").isNotEmpty());

		saveChatMessageUseCase.save(new SaveChatMessageUseCase.Command(
				first.getChatRoomUuid(),
				hostUuid,
				"TEXT",
				"목록 테스트",
				List.of(),
				now.plusSeconds(20)
		));

		mockMvc.perform(get("/api/v1/chat-rooms").header("X-Auth-User-Id", hostUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[?(@.chatRoomUuid=='" + first.getChatRoomUuid()
						+ "')].unreadCount").value(0))
				.andExpect(jsonPath("$.data.content[?(@.chatRoomUuid=='" + first.getChatRoomUuid()
						+ "')].lastMessage.content").value("목록 테스트"));

		mockMvc.perform(get("/api/v1/chat-rooms").header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].unreadCount").value(1));

		mockMvc.perform(post("/api/v1/chat-rooms/" + first.getChatRoomUuid() + "/read")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.unreadCount").value(0));

		String duplicateEventId = "dup-event";
		applyChatMessageCreatedUseCase.apply(new ApplyChatMessageCreatedUseCase.Command(
				duplicateEventId,
				first.getChatRoomUuid(),
				UUID.randomUUID(),
				hostUuid,
				"두번째",
				now.plusSeconds(30)
		));
		applyChatMessageCreatedUseCase.apply(new ApplyChatMessageCreatedUseCase.Command(
				duplicateEventId,
				first.getChatRoomUuid(),
				UUID.randomUUID(),
				hostUuid,
				"두번째",
				now.plusSeconds(30)
		));

		mockMvc.perform(get("/api/v1/chat-rooms").header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].unreadCount").value(1));

		mockMvc.perform(get("/api/v1/chat-rooms").header("X-Auth-User-Id", UUID.randomUUID()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(0));
	}

	@Test
	void hidesDisbandedRoomFromListAndRead() throws Exception {
		UUID hostUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T00:20:00Z");
		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"hide-created",
				UUID.randomUUID(),
				hostUuid,
				"해체될 방",
				now
		));
		applyMeetingDisbandedUseCase.apply(new ApplyMeetingDisbandedUseCase.Command(
				"hide-disband",
				room.getMeetingUuid(),
				now.plusSeconds(10)
		));

		mockMvc.perform(get("/api/v1/chat-rooms").header("X-Auth-User-Id", hostUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(0));

		mockMvc.perform(post("/api/v1/chat-rooms/" + room.getChatRoomUuid() + "/read")
						.header("X-Auth-User-Id", hostUuid)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/chat-rooms/by-meeting/" + room.getMeetingUuid())
						.header("X-Auth-User-Id", hostUuid))
				.andExpect(status().isNotFound());
	}

	@Test
	void findsApprovedRoomByMeetingUuid() throws Exception {
		UUID hostUuid = UUID.randomUUID();
		UUID pendingUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T07:30:00Z");
		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"by-meeting-created",
				UUID.randomUUID(),
				hostUuid,
				"부산 동행",
				now
		));
		applyMeetingParticipationChangedUseCase.apply(new ApplyMeetingParticipationChangedUseCase.Command(
				"by-meeting-pending",
				room.getMeetingUuid(),
				pendingUuid,
				"PENDING",
				now.plusSeconds(5)
		));

		mockMvc.perform(get("/api/v1/chat-rooms/by-meeting/" + room.getMeetingUuid())
						.header("X-Auth-User-Id", hostUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.chatRoomUuid").value(room.getChatRoomUuid().toString()))
				.andExpect(jsonPath("$.data.meetingUuid").value(room.getMeetingUuid().toString()))
				.andExpect(jsonPath("$.data.roomName").value("부산 동행"))
				.andExpect(jsonPath("$.data.roomStatus").value("ACTIVE"));

		mockMvc.perform(get("/api/v1/chat-rooms/by-meeting/" + room.getMeetingUuid())
						.header("X-Auth-User-Id", pendingUuid))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/chat-rooms/by-meeting/" + UUID.randomUUID())
						.header("X-Auth-User-Id", hostUuid))
				.andExpect(status().isNotFound());
	}
}
