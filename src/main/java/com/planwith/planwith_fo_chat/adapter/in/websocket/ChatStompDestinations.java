package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatStompDestinations {

	public static final String ENDPOINT = "/api/v1/chat/ws";
	public static final String APP_PREFIX = "/app";
	public static final String SUBSCRIBE_PREFIX = "/chat/room/";
	public static final String SEND_MAPPING = "/chat/{chatRoomUuid}/messages";
	public static final String USER_ID_HEADER = "X-Auth-User-Id";
	public static final String MEMBER_UUID_ATTRIBUTE = "memberUuid";

	private static final Pattern SUBSCRIBE_PATTERN = Pattern.compile("^/chat/room/([0-9a-fA-F-]{36})$");
	private static final Pattern SEND_PATTERN = Pattern.compile("^/app/chat/([0-9a-fA-F-]{36})/messages$");

	private ChatStompDestinations() {
	}

	public static String subscribe(UUID chatRoomUuid) {
		return SUBSCRIBE_PREFIX + chatRoomUuid;
	}

	public static UUID parseSubscribeRoom(String destination) {
		return matchUuid(SUBSCRIBE_PATTERN, destination);
	}

	public static UUID parseSendRoom(String destination) {
		return matchUuid(SEND_PATTERN, destination);
	}

	private static UUID matchUuid(Pattern pattern, String destination) {
		if (destination == null) {
			return null;
		}
		Matcher matcher = pattern.matcher(destination);
		if (!matcher.matches()) {
			return null;
		}
		return UUID.fromString(matcher.group(1));
	}
}
