package com.planwith.planwith_fo_chat.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.planwith.planwith_fo_chat.application.exception.ChatRoomNotReadyException;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCompletedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingCreatedUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ApplyMeetingParticipationChangedUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatMember;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;

class MeetingEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Test
	void createdEnvelopeMapsToUseCase() {
		CapturingCreated created = new CapturingCreated();
		MeetingEventConsumer consumer = new MeetingEventConsumer(
				created,
				command -> null,
				command -> null,
				objectMapper
		);
		UUID meetingUuid = UUID.randomUUID();
		UUID hostUuid = UUID.randomUUID();

		consumer.consumeCreated("planwith.meeting.created", """
				{
				  "eventId":"e1",
				  "eventType":"meeting.created",
				  "occurredAt":"2026-08-22T13:24:00Z",
				  "aggregateId":"%s",
				  "version":1,
				  "payload":{
				    "meetingUuid":"%s",
				    "hostMemberUuid":"%s",
				    "title":"부산 여행"
				  }
				}
				""".formatted(meetingUuid, meetingUuid, hostUuid));

		assertThat(created.commands).hasSize(1);
		assertThat(created.commands.get(0).title()).isEqualTo("부산 여행");
		assertThat(created.commands.get(0).meetingUuid()).isEqualTo(meetingUuid);
		assertThat(created.commands.get(0).hostMemberUuid()).isEqualTo(hostUuid);
	}

	@Test
	void ignoresInvalidJson() {
		CapturingCreated created = new CapturingCreated();
		MeetingEventConsumer consumer = new MeetingEventConsumer(
				created,
				command -> null,
				command -> null,
				objectMapper
		);

		consumer.consumeCreated("planwith.meeting.created", "{not-json");

		assertThat(created.commands).isEmpty();
	}

	@Test
	void retriesWhenRoomNotReady() {
		MeetingEventConsumer consumer = new MeetingEventConsumer(
				command -> null,
				command -> {
					throw new ChatRoomNotReadyException(command.meetingUuid());
				},
				command -> null,
				objectMapper
		);
		UUID meetingUuid = UUID.randomUUID();

		assertThatThrownBy(() -> consumer.consumeCompleted("planwith.meeting.completed", """
				{
				  "eventId":"e2",
				  "eventType":"meeting.completed",
				  "occurredAt":"2026-08-22T14:00:00Z",
				  "aggregateId":"%s",
				  "version":1,
				  "payload":{"meetingUuid":"%s"}
				}
				""".formatted(meetingUuid, meetingUuid)))
				.isInstanceOf(ChatRoomNotReadyException.class);
	}

	private static final class CapturingCreated implements ApplyMeetingCreatedUseCase {

		private final List<Command> commands = new ArrayList<>();

		@Override
		public ChatRoom apply(Command command) {
			commands.add(command);
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static final class UnusedCompleted implements ApplyMeetingCompletedUseCase {

		@Override
		public ChatRoom apply(Command command) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static final class UnusedParticipation implements ApplyMeetingParticipationChangedUseCase {

		@Override
		public ChatMember apply(Command command) {
			return null;
		}
	}
}
