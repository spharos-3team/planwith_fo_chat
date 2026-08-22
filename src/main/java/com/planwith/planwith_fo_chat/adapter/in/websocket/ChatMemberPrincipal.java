package com.planwith.planwith_fo_chat.adapter.in.websocket;

import java.security.Principal;
import java.util.UUID;

public record ChatMemberPrincipal(UUID memberUuid) implements Principal {

	@Override
	public String getName() {
		return memberUuid.toString();
	}
}
