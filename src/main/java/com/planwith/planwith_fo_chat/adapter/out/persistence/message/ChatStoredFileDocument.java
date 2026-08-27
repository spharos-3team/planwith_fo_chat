package com.planwith.planwith_fo_chat.adapter.out.persistence.message;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_files")
public class ChatStoredFileDocument {

	@Id
	private String id;
	@Indexed(unique = true)
	private String fileUuid;
	@Indexed
	private String chatRoomUuid;
	private String uploaderUuid;
	private String fileType;
	private String contentType;
	private String name;
	private byte[] bytes;
	private Instant createdAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileUuid() {
		return fileUuid;
	}

	public void setFileUuid(String fileUuid) {
		this.fileUuid = fileUuid;
	}

	public String getChatRoomUuid() {
		return chatRoomUuid;
	}

	public void setChatRoomUuid(String chatRoomUuid) {
		this.chatRoomUuid = chatRoomUuid;
	}

	public String getUploaderUuid() {
		return uploaderUuid;
	}

	public void setUploaderUuid(String uploaderUuid) {
		this.uploaderUuid = uploaderUuid;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
