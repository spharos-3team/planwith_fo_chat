package com.planwith.planwith_fo_chat.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingDisbandedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatMemberRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MeetingChatSyncIntegrationTests {

	@Autowired
	private ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase;

	@Autowired
	private ApplyMeetingCompletedUseCase applyMeetingCompletedUseCase;

	@Autowired
	private ApplyMeetingDisbandedUseCase applyMeetingDisbandedUseCase;

	@Autowired
	private ApplyMeetingParticipationChangedUseCase applyMeetingParticipationChangedUseCase;

	@Autowired
	private ChatRoomRepositoryPort chatRoomRepositoryPort;

	@Autowired
	private ChatMemberRepositoryPort chatMemberRepositoryPort;

	@Test
	void createdThenDuplicateKeepsSingleRoom() {
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T13:24:00Z");

		ChatRoom first = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"created-1",
				meetingUuid,
				hostUuid,
				"부산 2박 3일 여행",
				now
		));
		ChatRoom second = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"created-1",
				meetingUuid,
				hostUuid,
				"다른 이름",
				now
		));

		assertThat(first.getChatRoomId()).isEqualTo(second.getChatRoomId());
		assertThat(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).isPresent();
		assertThat(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid).orElseThrow().getRoomName())
				.isEqualTo("부산 2박 3일 여행");
		ChatMember host = chatMemberRepositoryPort
				.findByChatRoomIdAndMemberUuid(first.getChatRoomId(), hostUuid)
				.orElseThrow();
		assertThat(host.getStatus()).isEqualTo(ChatMemberStatus.APPROVED);
	}

	@Test
	void participationAndCompletedFollowCreated() {
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T13:24:00Z");

		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"created-2",
				meetingUuid,
				hostUuid,
				"제주 여행",
				now
		));
		applyMeetingParticipationChangedUseCase.apply(new ApplyMeetingParticipationChangedUseCase.Command(
				"join-1",
				meetingUuid,
				memberUuid,
				"APPROVED",
				now
		));
		ChatRoom ended = applyMeetingCompletedUseCase.apply(new ApplyMeetingCompletedUseCase.Command(
				"done-1",
				meetingUuid,
				now.plusSeconds(60)
		));

		assertThat(ended.getStatus()).isEqualTo(ChatRoomStatus.ENDED);
		assertThat(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), memberUuid)
				.orElseThrow()
				.getStatus()).isEqualTo(ChatMemberStatus.APPROVED);
		ChatMember ignored = applyMeetingParticipationChangedUseCase.apply(
				new ApplyMeetingParticipationChangedUseCase.Command(
						"join-late",
						meetingUuid,
						UUID.randomUUID(),
						"APPROVED",
						now.plusSeconds(120)
				)
		);
		assertThat(ignored).isNull();
	}

	@Test
	void completedWithoutRoomRetries() {
		assertThatThrownBy(() -> applyMeetingCompletedUseCase.apply(
				new ApplyMeetingCompletedUseCase.Command("done-missing", UUID.randomUUID(), Instant.now())
		)).isInstanceOf(ChatRoomNotReadyException.class);
	}

	@Test
	void disbandedHidesRoomAndKeepsRows() {
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-24T00:20:00Z");
		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"created-disband",
				meetingUuid,
				hostUuid,
				"해체 여행",
				now
		));
		ChatRoom hidden = applyMeetingDisbandedUseCase.apply(new ApplyMeetingDisbandedUseCase.Command(
				"disband-1",
				meetingUuid,
				now.plusSeconds(30)
		));
		ChatRoom duplicate = applyMeetingDisbandedUseCase.apply(new ApplyMeetingDisbandedUseCase.Command(
				"disband-1",
				meetingUuid,
				now.plusSeconds(60)
		));

		assertThat(hidden.getStatus()).isEqualTo(ChatRoomStatus.DISBANDED);
		assertThat(duplicate.getChatRoomId()).isEqualTo(hidden.getChatRoomId());
		assertThat(chatRoomRepositoryPort.findByMeetingUuid(meetingUuid)).isPresent();
		assertThat(chatMemberRepositoryPort.findByChatRoomIdAndMemberUuid(room.getChatRoomId(), hostUuid)).isPresent();
	}
}
