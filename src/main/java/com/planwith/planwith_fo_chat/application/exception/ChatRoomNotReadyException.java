package com.planwith.planwith_fo_chat.application.exception;

import java.util.UUID;

public class ChatRoomNotReadyException extends RuntimeException {

	private final UUID meetingUuid;

	public ChatRoomNotReadyException(UUID meetingUuid) {
		super("Chat room is not ready for meeting " + meetingUuid);
		this.meetingUuid = meetingUuid;
	}

	public UUID getMeetingUuid() {
		return meetingUuid;
	}
}
