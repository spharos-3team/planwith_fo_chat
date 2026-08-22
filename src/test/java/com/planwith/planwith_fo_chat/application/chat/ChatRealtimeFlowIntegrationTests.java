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

import com.planwith.planwith_fo_chat.adapter.out.redis.InMemoryChatMessageBroker;
import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.EnsureChatRoomSubscriberUseCase;
import com.planwith.planwith_fo_chat.application.port.in.SendRealtimeChatMessageUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatRealtimeFlowIntegrationTests {

	@Autowired
	private ApplyMeetingCreatedUseCase applyMeetingCreatedUseCase;

	@Autowired
	private ApplyMeetingCompletedUseCase applyMeetingCompletedUseCase;

	@Autowired
	private ApplyMeetingParticipationChangedUseCase applyMeetingParticipationChangedUseCase;

	@Autowired
	private SendRealtimeChatMessageUseCase sendRealtimeChatMessageUseCase;

	@Autowired
	private EnsureChatRoomSubscriberUseCase ensureChatRoomSubscriberUseCase;

	@Autowired
	private InMemoryChatMessageBroker inMemoryChatMessageBroker;

	@Test
	void publishesToBrokerAndRejectsEndedOrNonMember() {
		inMemoryChatMessageBroker.clear();
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();
		UUID outsiderUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T16:45:00Z");
		ChatRoom room = applyMeetingCreatedUseCase.apply(new ApplyMeetingCreatedUseCase.Command(
				"rt-created",
				meetingUuid,
				hostUuid,
				"실시간 방",
				now
		));
		applyMeetingParticipationChangedUseCase.apply(new ApplyMeetingParticipationChangedUseCase.Command(
				"rt-pending",
				meetingUuid,
				outsiderUuid,
				"PENDING",
				now
		));

		ensureChatRoomSubscriberUseCase.ensureCanSubscribe(room.getChatRoomUuid(), hostUuid);
		assertThatThrownBy(() -> ensureChatRoomSubscriberUseCase.ensureCanSubscribe(
				room.getChatRoomUuid(),
				outsiderUuid
		)).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);

		ChatMessage saved = sendRealtimeChatMessageUseCase.send(new SendRealtimeChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				"TEXT",
				"실시간으로 전달",
				List.of(),
				now
		));
		assertThat(inMemoryChatMessageBroker.published()).hasSize(1);
		assertThat(inMemoryChatMessageBroker.published().get(0).messageUuid()).isEqualTo(saved.getMessageUuid());
		assertThat(inMemoryChatMessageBroker.published().get(0).content()).isEqualTo("실시간으로 전달");

		assertThatThrownBy(() -> sendRealtimeChatMessageUseCase.send(new SendRealtimeChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				outsiderUuid,
				"TEXT",
				"비멤버",
				List.of(),
				now.plusSeconds(10)
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_MEMBER_NOT_ALLOWED);
		assertThat(inMemoryChatMessageBroker.published()).hasSize(1);

		applyMeetingCompletedUseCase.apply(new ApplyMeetingCompletedUseCase.Command(
				"rt-completed",
				meetingUuid,
				now.plusSeconds(20)
		));
		ensureChatRoomSubscriberUseCase.ensureCanSubscribe(room.getChatRoomUuid(), hostUuid);
		assertThatThrownBy(() -> sendRealtimeChatMessageUseCase.send(new SendRealtimeChatMessageUseCase.Command(
				room.getChatRoomUuid(),
				hostUuid,
				"TEXT",
				"종료 후",
				List.of(),
				now.plusSeconds(30)
		))).isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.CHAT_ROOM_ENDED);
		assertThat(inMemoryChatMessageBroker.published()).hasSize(1);
	}
}
