package com.planwith.planwith_fo_chat.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
	CHAT_ROOM_NOT_READY(HttpStatus.CONFLICT, "CHAT_ROOM_NOT_READY", "채팅방이 아직 생성되지 않았습니다."),
	CHAT_ROOM_ENDED(HttpStatus.CONFLICT, "CHAT_ROOM_ENDED", "종료된 채팅방입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}
}
