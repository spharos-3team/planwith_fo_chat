package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "chat_room_member_reads",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_chat_room_member_reads_member_room",
				columnNames = {"member_uuid", "chat_room_uuid"}
		),
		indexes = @Index(
				name = "idx_chat_room_member_reads_member_last",
				columnList = "member_uuid, last_message_at, chat_room_uuid"
		)
)
public class ChatRoomMemberReadJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "read_id", columnDefinition = "bigint unsigned")
	private Long readId;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "chat_room_uuid", nullable = false, length = 36)
	private String chatRoomUuid;

	@Column(name = "room_name", length = 100)
	private String roomName;

	@Column(name = "last_message_uuid", length = 36)
	private String lastMessageUuid;

	@Column(name = "last_message_content", columnDefinition = "text")
	private String lastMessageContent;

	@Column(name = "last_message_sender_uuid", length = 36)
	private String lastMessageSenderUuid;

	@Column(name = "last_message_at", nullable = false, columnDefinition = "datetime")
	private Instant lastMessageAt;

	@Column(name = "unread_count", nullable = false)
	private int unreadCount;

	@Column(name = "updated_at", nullable = false, columnDefinition = "datetime")
	private Instant updatedAt;

	public Long getReadId() {
		return readId;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public String getChatRoomUuid() {
		return chatRoomUuid;
	}

	public void setChatRoomUuid(String chatRoomUuid) {
		this.chatRoomUuid = chatRoomUuid;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getLastMessageUuid() {
		return lastMessageUuid;
	}

	public void setLastMessageUuid(String lastMessageUuid) {
		this.lastMessageUuid = lastMessageUuid;
	}

	public String getLastMessageContent() {
		return lastMessageContent;
	}

	public void setLastMessageContent(String lastMessageContent) {
		this.lastMessageContent = lastMessageContent;
	}

	public String getLastMessageSenderUuid() {
		return lastMessageSenderUuid;
	}

	public void setLastMessageSenderUuid(String lastMessageSenderUuid) {
		this.lastMessageSenderUuid = lastMessageSenderUuid;
	}

	public Instant getLastMessageAt() {
		return lastMessageAt;
	}

	public void setLastMessageAt(Instant lastMessageAt) {
		this.lastMessageAt = lastMessageAt;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
