package com.planwith.planwith_fo_chat.application.port.in;

import java.util.UUID;

public interface EnsureChatRoomSubscriberUseCase {

	void ensureCanSubscribe(UUID chatRoomUuid, UUID memberUuid);
}
