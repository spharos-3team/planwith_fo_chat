package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;

import com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "chat_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_chat_members_room_member",
				columnNames = {"chat_room_id", "member_uuid"}
		)
)
public class ChatMemberJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_member_id", columnDefinition = "bigint unsigned")
	private Long chatMemberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoomJpaEntity chatRoom;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "last_read_message_uuid", length = 36)
	private String lastReadMessageUuid;

	@Column(name = "notification_enabled", nullable = false)
	private boolean notificationEnabled = true;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 16)
	private ChatMemberStatus status;

	@Column(name = "joined_at", columnDefinition = "datetime")
	private Instant joinedAt;

	public Long getChatMemberId() {
		return chatMemberId;
	}

	public ChatRoomJpaEntity getChatRoom() {
		return chatRoom;
	}

	public void setChatRoom(ChatRoomJpaEntity chatRoom) {
		this.chatRoom = chatRoom;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public String getLastReadMessageUuid() {
		return lastReadMessageUuid;
	}

	public void setLastReadMessageUuid(String lastReadMessageUuid) {
		this.lastReadMessageUuid = lastReadMessageUuid;
	}

	public boolean isNotificationEnabled() {
		return notificationEnabled;
	}

	public void setNotificationEnabled(boolean notificationEnabled) {
		this.notificationEnabled = notificationEnabled;
	}

	public ChatMemberStatus getStatus() {
		return status;
	}

	public void setStatus(ChatMemberStatus status) {
		this.status = status;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
		this.joinedAt = joinedAt;
	}
}
