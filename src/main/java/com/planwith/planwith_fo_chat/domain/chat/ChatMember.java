package com.planwith.planwith_fo_chat.domain.chat;

import java.time.Instant;
import java.util.UUID;

public class ChatMember {

	private final Long chatMemberId;
	private final Long chatRoomId;
	private final UUID memberUuid;
	private final UUID lastReadMessageUuid;
	private final boolean notificationEnabled;
	private final ChatMemberStatus status;
	private final Instant joinedAt;

	public ChatMember(
			Long chatMemberId,
			Long chatRoomId,
			UUID memberUuid,
			UUID lastReadMessageUuid,
			boolean notificationEnabled,
			ChatMemberStatus status,
			Instant joinedAt
	) {
		this.chatMemberId = chatMemberId;
		this.chatRoomId = chatRoomId;
		this.memberUuid = memberUuid;
		this.lastReadMessageUuid = lastReadMessageUuid;
		this.notificationEnabled = notificationEnabled;
		this.status = status;
		this.joinedAt = joinedAt;
	}

	public static ChatMember host(Long chatRoomId, UUID memberUuid, Instant now) {
		return new ChatMember(null, chatRoomId, memberUuid, null, true, ChatMemberStatus.APPROVED, now);
	}

	public static ChatMember of(Long chatRoomId, UUID memberUuid, ChatMemberStatus status, Instant now) {
		Instant joinedAt = status == ChatMemberStatus.APPROVED ? now : null;
		return new ChatMember(null, chatRoomId, memberUuid, null, true, status, joinedAt);
	}

	public ChatMember withStatus(ChatMemberStatus nextStatus, Instant now) {
		Instant nextJoinedAt = joinedAt;
		if (nextStatus == ChatMemberStatus.APPROVED && nextJoinedAt == null) {
			nextJoinedAt = now;
		}
		return new ChatMember(
				chatMemberId,
				chatRoomId,
				memberUuid,
				lastReadMessageUuid,
				notificationEnabled,
				nextStatus,
				nextJoinedAt
		);
	}

	public Long getChatMemberId() {
		return chatMemberId;
	}

	public Long getChatRoomId() {
		return chatRoomId;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public UUID getLastReadMessageUuid() {
		return lastReadMessageUuid;
	}

	public boolean isNotificationEnabled() {
		return notificationEnabled;
	}

	public ChatMemberStatus getStatus() {
		return status;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}
