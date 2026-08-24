# Chat API 진행 현황

> 이슈 완료마다 이 문서를 갱신한다.  
> Swagger (로컬 bootRun): `http://localhost:8088/swagger-ui/index.html`  
> Swagger (다른 PC / Docker): `http://{SERVER_IP}:8000/swagger-ui.html` → `planwith-fo-chat` (Gateway 라우트 붙인 뒤)  
> 공통 응답: `ApiResponse<T>`  
> 호출 경로: `Frontend → Gateway(:8000) → Chat(:8088)` (Access 검증은 Gateway. Chat은 JWT를 검증하지 않는다)

최종 갱신: 2026-08-24 (#13 채팅 메시지 히스토리 REST)

---

## 서비스 경계

`planwith-fo-chat` 은 **채팅방·멤버·메시지·읽음·실시간 전달** 만 담당한다.  
모임을 만들거나 참여를 승인하지 않는다. Meeting이 Kafka로 알려주면 자기 DB만 맞춘다.

| 요구사항 묶음 | 담당 |
| --- | --- |
| 모임 CRUD, 신청/승인/완료/해체 | **meeting** |
| 채팅방·멤버·읽음 (`chat_rooms`, `chat_members`, `chat_room_member_reads`) | **chat** |
| 메시지 (`chat_messages` MongoDB) | **chat** |
| 실시간 전송 | **chat** WebSocket STOMP. Redis Pub/Sub는 멀티 인스턴스용(기본 off) |
| 프로필 | member |

OpenFeign 금지. 신원은 Gateway `X-Auth-User-Id`. Refresh Token을 JSON/URL/로그에 넣지 않는다.

---

## 요약

| 상태 | 이슈 | 내용 |
| --- | --- | --- |
| ➡ Epic | #1 | 채팅 서비스 핵심 구조 |
| ✅ | #2 | 채팅방·멤버 MySQL + Meeting created/participation/completed |
| ✅ | #3 | MongoDB `chat_messages` |
| ✅ | #4 | WebSocket STOMP + Redis Pub/Sub |
| ✅ | #5 | Read Model + 목록·읽음 API |
| ✅ | #6 | 모임 해체 시 목록/입장 숨김, DB row 유지 |
| ✅ | #12 | 모임 UUID로 채팅방 조회 + 목록 `meetingUuid` |
| ✅ 구현 | #13 | 채팅 메시지 히스토리 REST |

---

## 모임 ↔ 채팅 생명주기

같은 HTTP 트랜잭션이 아니다. Meeting이 이벤트를 내고 Chat이 자기 저장소를 맞춘다.

```
모임 생성 성공  →  meeting.created
                 →  chat_rooms INSERT (ACTIVE)
                 + 호스트 chat_members APPROVED
                 + 호스트 chat_room_member_reads

참여 상태 변경  →  meeting.participation.changed
                 PENDING | APPROVED | REJECTED | LEFT | KICKED
                 →  chat_members.status 동기화
                 → APPROVED면 read model 행 생성

모임 완료        →  meeting.completed
                 →  chat_rooms.status = ENDED
                 → 목록/히스토리 유지, 입력만 차단

모임 해체        →  meeting.disbanded
                 →  chat_rooms.status = DISBANDED
                 → 목록·입장·읽음에서 안 보임
                 → chat_rooms / chat_members / chat_messages / read model 물리삭제 없음
```

Envelope: `eventId`, `eventType`, `occurredAt`, `aggregateId`, `version`, `payload`.  
Key = `meetingUuid` (메시지 이벤트는 `chatRoomUuid`).

| 토픽 | eventType | payload |
| --- | --- | --- |
| `planwith.meeting.created` | `meeting.created` | `meetingUuid`, `hostMemberUuid`, `title` |
| `planwith.meeting.completed` | `meeting.completed` | `meetingUuid` |
| `planwith.meeting.disbanded` | `meeting.disbanded` | `meetingUuid` |
| `planwith.meeting.participation.changed` | `meeting.participation.changed` | `meetingUuid`, `memberUuid`, `status` |
| `planwith.chat.message.created` | `chat.message.created` | 읽음 모델 갱신용. key=`chatRoomUuid` |

로컬 기본은 Kafka off (`CHAT_KAFKA_CONSUMER_ENABLED=false`, Meeting도 `MEETING_KAFKA_ENABLED=false`).  
꺼져 있으면 Meeting은 NoOp 로그만, 메시지 생성은 프로세스 안에서 Read Model을 갱신한다.

---

## 저장소

### MySQL `chat_db`

| 테이블 | 역할 |
| --- | --- |
| `chat_rooms` | `chat_room_uuid`, `meeting_uuid` UNIQUE, `room_name`, `status` |
| `chat_members` | 방 멤버, `status`, `last_read_message_uuid`, `joined_at` |
| `chat_room_member_reads` | 목록용 Read Model. 키 `(member_uuid, chat_room_uuid)` |
| `processed_chat_events` | 이벤트 멱등 (`event_id` UNIQUE) |

방 상태: `ACTIVE` 입력 가능 / `ENDED` 완료(목록 유지, 입력 불가) / `DISBANDED` 해체(사용자에게 숨김, row 유지)

멤버 상태: `PENDING` / `APPROVED` / `REJECTED` / `LEFT` / `KICKED`  
목록·입장·전송은 `APPROVED`만.

### MongoDB

컬렉션 `chat_messages`. 인덱스 `(chatRoomUuid, createdAt)`.  
필드: `messageUuid`, `chatRoomUuid`, `senderUuid`, `messageType`, `content`, `files[]` (`IMAGE|VIDEO|AUDIO|DOCUMENT|ETC`), `isModified`, `isDeleted`, `createdAt`, `updatedAt`.  
저장은 `ACTIVE` + `APPROVED`만. `ENDED`는 저장 거부·기존 조회 유지. `DISBANDED`는 조회/저장 모두 없는 방처럼 거절.

---

## 완료된 API

인증: Gateway `X-Auth-User-Id`. Chat은 헤더를 Context Resolver로만 읽는다. JWT 검증 없음.

| Issue | Method | Endpoint | 설명 |
| --- | --- | --- | --- |
| #5 | GET | `/api/v1/chat-rooms` | 내 채팅방 목록. cursor `cursorAt` + `cursorChatRoomUuid`, `size` 기본 20 최대 50 |
| #12 | GET | `/api/v1/chat-rooms/by-meeting/{meetingUuid}` | 모임 상세 입장용. APPROVED만. DISBANDED/없으면 `CHAT_ROOM_NOT_FOUND` |
| #13 | GET | `/api/v1/chat-rooms/{chatRoomUuid}/messages` | Mongo 히스토리. `before`+`size`(기본 20 최대 50). 역순. ENDED 조회 가능 |
| #5 | POST | `/api/v1/chat-rooms/{chatRoomUuid}/read` | 읽음. body `lastReadMessageUuid` 선택. `unreadCount=0` |
| #4 | WS | `/api/v1/chat/ws` | STOMP. 아래 WebSocket |

목록 응답 항목: `chatRoomUuid`, `meetingUuid`, `roomName`, `roomStatus`, `lastMessage`, `unreadCount`.  
`ENDED`는 목록에 남을 수 있다. `DISBANDED`는 목록·읽음에서 빠지고 `CHAT_ROOM_NOT_FOUND`.

메시지 목록 응답: `content[]` (`messageUuid`, `chatRoomUuid`, `senderUuid`, `messageType`, `content`, `files`, `modified`, `deleted`, `createdAt`, `updatedAt`) + `nextBefore`.

---

## WebSocket (STOMP)

브라우저에서 HTTP 헤더를 못 넣으므로 **CONNECT 프레임**에 `X-Auth-User-Id`를 넣는다.

| 단계 | 값 |
| --- | --- |
| 연결 | `ws://localhost:8088/api/v1/chat/ws` (SockJS 아님) |
| 구독 | `/chat/room/{chatRoomUuid}` |
| 전송 | `/app/chat/{chatRoomUuid}/messages` |

전송 body:

```json
{
  "messageType": "TEXT",
  "content": "안녕",
  "files": []
}
```

흐름: 멤버십 확인 → Mongo 저장 → `chat.message.created`(또는 로컬)로 unread 갱신 → Redis `chat:room:{uuid}` 또는 로컬 fanout → 구독자에게 STOMP 전달.  
`ENDED`·비 `APPROVED` 전송 거부. `ENDED` 구독은 허용. `DISBANDED` 구독/전송은 `CHAT_ROOM_NOT_FOUND`.  
`CHAT_REDIS_ENABLED=false`(기본)면 단일 프로세스 fanout.

---

## 로컬 실행 메모

- `.env`는 `LocalDotenvLoader`가 `main()`에서 읽는다. OS/IntelliJ env가 우선.
- MySQL: 인프라 `3307` / `chat_db` / `chat_user`. 볼륨이 오래됐으면 `planwith-infra/scripts/ensure-databases.ps1`.
- Mongo: 기본 `mongodb://127.0.0.1:27017/chat_db`. 이 포트가 인증 replica set이면 전용 Mongo를 띄우고 `MONGO_URI`를 바꾼다.
- Kafka/Redis는 기본 off. 모임→방 생성 확인은 Meeting·Chat Kafka를 둘 다 켜고 브로커(`localhost:9092`)가 있어야 한다.
- Gateway에 `/api/v1/chat-rooms/**`, `/api/v1/chat/ws` 라우트가 아직 없다. 지금은 Chat `:8088` 직접 호출.

환경 변수:

| 변수 | 기본 | 의미 |
| --- | --- | --- |
| `CHAT_KAFKA_ENABLED` | false | Kafka producer·health |
| `CHAT_KAFKA_CONSUMER_ENABLED` | false | Meeting/메시지 consumer |
| `CHAT_REDIS_ENABLED` | false | Redis Pub/Sub |
| `EUREKA_CLIENT_ENABLED` | (yaml true, 로컬 .env false) | 로컬 단독은 false |
| `GATEWAY_TRUST_CHECK_ENABLED` | false | 로컬은 헤더만으로 신원 |

---

## 하지 않음 / 다음

- Gateway `planwith-fo-chat` 라우트 (`gateway.route.snippet.yml` 경로가 템플릿 `/api/planwith-fo-chat/**` 이라 실제 `/api/v1/chat-rooms`, `/api/v1/chat/ws` 와 안 맞음)
- 프론트 채팅 UI
- 인프라 compose에 Mongo/Kafka/Redis (서버는 아직 MySQL만)
- 해체 물리삭제 (하지 않음. #6은 숨김)
