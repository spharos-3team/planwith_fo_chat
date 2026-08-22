package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_chat_messages_room_created", def = "{'chatRoomUuid': 1, 'createdAt': -1}")
public class ChatMessageDocument {

	@Id
	private String id;
	private String messageUuid;
	private String chatRoomUuid;
	private String senderUuid;
	private String messageType;
	private String content;
	private List<ChatFileDocument> files;
	@Field("isModified")
	private boolean isModified;
	@Field("isDeleted")
	private boolean isDeleted;
	private Instant createdAt;
	private Instant updatedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessageUuid() {
		return messageUuid;
	}

	public void setMessageUuid(String messageUuid) {
		this.messageUuid = messageUuid;
	}

	public String getChatRoomUuid() {
		return chatRoomUuid;
	}

	public void setChatRoomUuid(String chatRoomUuid) {
		this.chatRoomUuid = chatRoomUuid;
	}

	public String getSenderUuid() {
		return senderUuid;
	}

	public void setSenderUuid(String senderUuid) {
		this.senderUuid = senderUuid;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<ChatFileDocument> getFiles() {
		return files;
	}

	public void setFiles(List<ChatFileDocument> files) {
		this.files = files;
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean modified) {
		isModified = modified;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public static class ChatFileDocument {

		private String fileType;
		private String url;
		private String name;

		public String getFileType() {
			return fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
