package com.planwith.planwith_fo_chat.domain.chat;

public enum ChatMemberStatus {
	PENDING,
	APPROVED,
	REJECTED,
	LEFT,
	KICKED;

	public static ChatMemberStatus from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("status is required.");
		}
		try {
			return ChatMemberStatus.valueOf(value.trim().toUpperCase());
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unknown chat member status: " + value);
		}
	}

	public boolean isJoinAttempt() {
		return this == PENDING || this == APPROVED;
	}
}
