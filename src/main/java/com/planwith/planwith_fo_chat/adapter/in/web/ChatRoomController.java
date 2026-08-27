package com.planwith.planwith_fo_chat.adapter.in.web;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_chat.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatFileUploadResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatMessageListResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatMessageResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatRoomByMeetingResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatRoomListItemResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatRoomListResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.ChatRoomReadResponse;
import com.planwith.planwith_fo_chat.adapter.in.web.dto.MarkChatRoomReadRequest;
import com.planwith.planwith_fo_chat.application.port.in.GetChatFileUseCase;
import com.planwith.planwith_fo_chat.application.port.in.GetChatRoomByMeetingUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ListChatMessagesUseCase;
import com.planwith.planwith_fo_chat.application.port.in.ListChatRoomsUseCase;
import com.planwith.planwith_fo_chat.application.port.in.MarkChatRoomReadUseCase;
import com.planwith.planwith_fo_chat.application.port.in.UploadChatFileUseCase;
import com.planwith.planwith_fo_chat.domain.chat.ChatMessage;
import com.planwith.planwith_fo_chat.domain.chat.ChatRoomMemberRead;
import com.planwith.planwith_fo_chat.domain.chat.FileType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping("/api/v1/chat-rooms")
@Tag(name = "chat-rooms", description = "채팅방 목록·읽음·모임 조회·메시지")
public class ChatRoomController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final ListChatRoomsUseCase listChatRoomsUseCase;
	private final GetChatRoomByMeetingUseCase getChatRoomByMeetingUseCase;
	private final ListChatMessagesUseCase listChatMessagesUseCase;
	private final MarkChatRoomReadUseCase markChatRoomReadUseCase;
	private final UploadChatFileUseCase uploadChatFileUseCase;
	private final GetChatFileUseCase getChatFileUseCase;

	public ChatRoomController(
			GatewayAuthenticationContextResolver authContextResolver,
			ListChatRoomsUseCase listChatRoomsUseCase,
			GetChatRoomByMeetingUseCase getChatRoomByMeetingUseCase,
			ListChatMessagesUseCase listChatMessagesUseCase,
			MarkChatRoomReadUseCase markChatRoomReadUseCase,
			UploadChatFileUseCase uploadChatFileUseCase,
			GetChatFileUseCase getChatFileUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.listChatRoomsUseCase = listChatRoomsUseCase;
		this.getChatRoomByMeetingUseCase = getChatRoomByMeetingUseCase;
		this.listChatMessagesUseCase = listChatMessagesUseCase;
		this.markChatRoomReadUseCase = markChatRoomReadUseCase;
		this.uploadChatFileUseCase = uploadChatFileUseCase;
		this.getChatFileUseCase = getChatFileUseCase;
	}

	@GetMapping
	@Operation(summary = "채팅방 목록", description = "last_message_at + chat_room_uuid 커서로 내 채팅방을 조회한다.")
	public ResponseEntity<ApiResponse<ChatRoomListResponse>> list(
			@RequestParam(required = false) Instant cursorAt,
			@RequestParam(required = false) UUID cursorChatRoomUuid,
			@RequestParam(defaultValue = "20") int size
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		ListChatRoomsUseCase.Result result = listChatRoomsUseCase.list(new ListChatRoomsUseCase.Command(
				memberUuid,
				cursorAt,
				cursorChatRoomUuid,
				size
		));
		return ResponseEntity.ok(ApiResponse.success(new ChatRoomListResponse(
				result.items().stream().map(this::toItem).toList(),
				result.nextCursorAt(),
				result.nextCursorChatRoomUuid()
		)));
	}

	@GetMapping("/by-meeting/{meetingUuid}")
	@Operation(summary = "모임으로 채팅방 조회", description = "APPROVED 멤버만. DISBANDED는 없는 방처럼 404.")
	public ResponseEntity<ApiResponse<ChatRoomByMeetingResponse>> getByMeeting(@PathVariable UUID meetingUuid) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(ChatRoomByMeetingResponse.from(
				getChatRoomByMeetingUseCase.get(new GetChatRoomByMeetingUseCase.Command(meetingUuid, memberUuid))
		)));
	}

	@GetMapping("/{chatRoomUuid}/messages")
	@Operation(summary = "채팅 메시지 목록", description = "createdAt 역순. before로 이전 페이지. ENDED는 조회 가능, DISBANDED는 404.")
	public ResponseEntity<ApiResponse<ChatMessageListResponse>> listMessages(
			@PathVariable UUID chatRoomUuid,
			@RequestParam(required = false) Instant before,
			@RequestParam(defaultValue = "20") int size
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		int requestedSize = size <= 0 ? 20 : Math.min(size, 50);
		List<ChatMessage> messages = listChatMessagesUseCase.list(new ListChatMessagesUseCase.Command(
				chatRoomUuid,
				memberUuid,
				before,
				requestedSize
		));
		Instant nextBefore = messages.size() == requestedSize
				? messages.get(messages.size() - 1).getCreatedAt()
				: null;
		return ResponseEntity.ok(ApiResponse.success(new ChatMessageListResponse(
				messages.stream().map(ChatMessageResponse::from).toList(),
				nextBefore
		)));
	}

	@PostMapping("/{chatRoomUuid}/read")
	@Operation(summary = "채팅방 읽음", description = "last_read_message_uuid를 갱신하고 unreadCount를 0으로 만든다.")
	public ResponseEntity<ApiResponse<ChatRoomReadResponse>> markRead(
			@PathVariable UUID chatRoomUuid,
			@RequestBody(required = false) MarkChatRoomReadRequest request
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		UUID lastReadMessageUuid = request == null ? null : request.lastReadMessageUuid();
		ChatRoomMemberRead read = markChatRoomReadUseCase.markRead(new MarkChatRoomReadUseCase.Command(
				memberUuid,
				chatRoomUuid,
				lastReadMessageUuid,
				null
		));
		return ResponseEntity.ok(ApiResponse.success(new ChatRoomReadResponse(
				read.getChatRoomUuid(),
				read.getLastMessageUuid(),
				read.getUnreadCount(),
				read.getUpdatedAt()
		)));
	}

	@PostMapping(path = "/{chatRoomUuid}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "채팅 파일 업로드", description = "APPROVED 멤버만. ACTIVE 방에서만 업로드. 최대 10MB. 응답 url을 STOMP files에 넣는다.")
	public ResponseEntity<ApiResponse<ChatFileUploadResponse>> uploadFile(
			@PathVariable UUID chatRoomUuid,
			@RequestPart("file") MultipartFile file
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(ChatFileUploadResponse.from(
				uploadChatFileUseCase.upload(chatRoomUuid, memberUuid, file)
		)));
	}

	@GetMapping("/{chatRoomUuid}/files/{fileUuid}")
	@Operation(summary = "채팅 파일 조회", description = "APPROVED 멤버만. ENDED는 조회 가능, DISBANDED는 404.")
	public ResponseEntity<byte[]> getFile(
			@PathVariable UUID chatRoomUuid,
			@PathVariable UUID fileUuid
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		GetChatFileUseCase.Result file = getChatFileUseCase.get(chatRoomUuid, fileUuid, memberUuid);
		boolean inline = file.fileType() == FileType.IMAGE
				|| file.fileType() == FileType.VIDEO
				|| file.fileType() == FileType.AUDIO;
		ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
				.filename(file.name() == null ? "file" : file.name(), StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(file.bytes());
	}

	private ChatRoomListItemResponse toItem(ListChatRoomsUseCase.Item item) {
		ChatRoomMemberRead read = item.read();
		ChatRoomListItemResponse.LastMessageResponse lastMessage = read.getLastMessageUuid() == null
				? null
				: new ChatRoomListItemResponse.LastMessageResponse(
						read.getLastMessageUuid(),
						read.getLastMessageContent(),
						read.getLastMessageSenderUuid(),
						read.getLastMessageAt()
				);
		return new ChatRoomListItemResponse(
				read.getChatRoomUuid(),
				item.meetingUuid(),
				read.getRoomName(),
				item.roomStatus(),
				lastMessage,
				read.getUnreadCount()
		);
	}
}
