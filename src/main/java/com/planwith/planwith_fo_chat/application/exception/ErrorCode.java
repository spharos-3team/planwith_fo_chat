package com.planwith.planwith_fo_chat.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
	CHAT_ROOM_NOT_READY(HttpStatus.CONFLICT, "CHAT_ROOM_NOT_READY", "채팅방이 아직 생성되지 않았습니다."),
	CHAT_ROOM_ENDED(HttpStatus.CONFLICT, "CHAT_ROOM_ENDED", "종료된 채팅방입니다."),
	CHAT_MEMBER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CHAT_MEMBER_NOT_ALLOWED", "채팅방에 참여한 회원만 메시지를 보낼 수 있습니다."),
	CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."),
	CHAT_MESSAGE_NOT_SENDER(HttpStatus.FORBIDDEN, "CHAT_MESSAGE_NOT_SENDER", "본인 메시지만 수정하거나 삭제할 수 있습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근이 거부되었습니다.");

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
