package com.planwith.planwith_fo_chat.application.port.out;

import java.util.UUID;

public interface ChatPresencePort {

	void markOnline(UUID memberUuid);
}
