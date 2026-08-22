package com.planwith.planwith_fo_chat.domain.chat;

public class ChatFile {

	private final FileType fileType;
	private final String url;
	private final String name;

	public ChatFile(FileType fileType, String url, String name) {
		this.fileType = fileType;
		this.url = url;
		this.name = name;
	}

	public FileType getFileType() {
		return fileType;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}
}
