package com.planwith.planwith_fo_chat.application.port.out;

import com.planwith.planwith_fo_chat.application.chat.ChatRealtimePayload;

public interface ChatRealtimeFanoutPort {

	void deliver(ChatRealtimePayload payload);
}
