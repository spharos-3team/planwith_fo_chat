package com.planwith.planwith_fo_chat.application.chat;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.application.port.in.ListChatRoomsUseCase;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomMemberReadRepositoryPort;
import com.planwith.planwith_fo_chat.application.port.out.ChatRoomRepositoryPort;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoom;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;

@Service
@Transactional(readOnly = true)
public class ListChatRoomsService implements ListChatRoomsUseCase {

	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 50;

	private final ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort;
	private final ChatRoomRepositoryPort chatRoomRepositoryPort;

	public ListChatRoomsService(
			ChatRoomMemberReadRepositoryPort chatRoomMemberReadRepositoryPort,
			ChatRoomRepositoryPort chatRoomRepositoryPort
	) {
		this.chatRoomMemberReadRepositoryPort = chatRoomMemberReadRepositoryPort;
		this.chatRoomRepositoryPort = chatRoomRepositoryPort;
	}

	@Override
	public Result list(Command command) {
		Objects.requireNonNull(command, "List chat rooms command is required.");
		if (command.memberUuid() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		int size = command.size() <= 0 ? DEFAULT_SIZE : Math.min(command.size(), MAX_SIZE);
		List<ChatRoomMemberRead> rows = chatRoomMemberReadRepositoryPort.findApprovedInbox(
				command.memberUuid(),
				command.cursorAt(),
				command.cursorChatRoomUuid(),
				size + 1
		);
		boolean hasNext = rows.size() > size;
		List<ChatRoomMemberRead> page = hasNext ? rows.subList(0, size) : rows;
		List<Item> items = page.stream()
				.map(read -> {
					ChatRoom room = chatRoomRepositoryPort.findByChatRoomUuid(read.getChatRoomUuid())
							.orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
					return new Item(read, room.getStatus(), room.getMeetingUuid());
				})
				.toList();
		Instant nextCursorAt = null;
		UUID nextCursorChatRoomUuid = null;
		if (hasNext && !page.isEmpty()) {
			ChatRoomMemberRead last = page.get(page.size() - 1);
			nextCursorAt = last.getLastMessageAt();
			nextCursorChatRoomUuid = last.getChatRoomUuid();
		}
		return new Result(items, nextCursorAt, nextCursorChatRoomUuid);
	}
}
