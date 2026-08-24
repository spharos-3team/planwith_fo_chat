package com.planwith.planwith_fo_chat.adapter.out.persistence.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberReadJpaRepository extends JpaRepository<ChatRoomMemberReadJpaEntity, Long> {

	Optional<ChatRoomMemberReadJpaEntity> findByMemberUuidAndChatRoomUuid(String memberUuid, String chatRoomUuid);

	@Query("""
			select r from ChatRoomMemberReadJpaEntity r, ChatMemberJpaEntity m
			where r.memberUuid = :memberUuid
			  and m.memberUuid = r.memberUuid
			  and m.chatRoom.chatRoomUuid = r.chatRoomUuid
			  and m.status = com.planwith.planwith_fo_chat.domain.chat.ChatMemberStatus.APPROVED
			  and m.chatRoom.status <> com.planwith.planwith_fo_chat.domain.chat.ChatRoomStatus.DISBANDED
			  and (
			    :cursorAt is null
			    or r.lastMessageAt < :cursorAt
			    or (r.lastMessageAt = :cursorAt and r.chatRoomUuid < :cursorUuid)
			  )
			order by r.lastMessageAt desc, r.chatRoomUuid desc
			""")
	List<ChatRoomMemberReadJpaEntity> findApprovedInbox(
			@Param("memberUuid") String memberUuid,
			@Param("cursorAt") Instant cursorAt,
			@Param("cursorUuid") String cursorUuid,
			Pageable pageable
	);
}
