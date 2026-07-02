package com.very.relink.chat.infrastructure.persistence.jooq;

import com.very.relink.chat.application.query.ChatQueryPort;
import com.very.relink.chat.domain.ChatEnums.AttachmentType;
import com.very.relink.chat.domain.ChatEnums.MessageStatus;
import com.very.relink.chat.domain.ChatEnums.MessageType;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
@RequiredArgsConstructor
public class ChatJooqQueryAdapter implements ChatQueryPort {

    private final DSLContext dslContext;

    @Override
    public List<ChatRoomSummaryProjection> findRooms(Long memberId) {
        String sql = """
                select
                    cr.chat_room_id,
                    cr.room_type,
                    cr.title,
                    case
                        when cr.room_type in ('GROUP', 'APPOINTMENT') then cr.title
                        else coalesce(
                            (select m.name
                               from chat_participant cp2
                               join members m
                                 on m.member_id = cp2.member_id
                              where cp2.room_id = cr.chat_room_id
                                and cp2.member_id <> ?
                                and cp2.status = ?
                              order by cp2.member_id
                              limit 1),
                            concat('회원 ', coalesce(
                                (select cp2.member_id
                                   from chat_participant cp2
                                  where cp2.room_id = cr.chat_room_id
                                    and cp2.member_id <> ?
                                    and cp2.status = ?
                                  order by cp2.member_id
                                  limit 1),
                                ?
                            ))
                        )
                    end as display_name,
                    cr.cover_image_key,
                    lm.text_content as last_message,
                    lm.message_type as last_message_type,
                    lm.created_at as last_message_at,
                    (
                        select count(1)
                          from chat_message cm
                          left join chat_read_cursor crc
                            on crc.room_id = cm.room_id
                           and crc.member_id = ?
                         where cm.room_id = cr.chat_room_id
                           and cm.chat_message_id > coalesce(crc.last_read_message_id, 0)
                           and cm.sender_id <> ?
                    ) as unread_count
                  from chat_room cr
                  join chat_participant cp
                    on cp.room_id = cr.chat_room_id
                   and cp.member_id = ?
                   and cp.status = ?
                  left join chat_message lm
                    on lm.chat_message_id = (
                        select cm2.chat_message_id
                          from chat_message cm2
                         where cm2.room_id = cr.chat_room_id
                         order by cm2.chat_message_id desc
                         limit 1
                    )
                 order by coalesce(lm.created_at, cr.created_at) desc
                """;
        return dslContext.resultQuery(
                        sql,
                        memberId,
                        ParticipantStatus.ACTIVE.name(),
                        memberId,
                        ParticipantStatus.ACTIVE.name(),
                        memberId,
                        memberId,
                        memberId,
                        memberId,
                        ParticipantStatus.ACTIVE.name()
                )
                .fetch(record -> new ChatRoomSummaryProjection(
                        record.get("chat_room_id", Long.class),
                        RoomType.valueOf(record.get("room_type", String.class)),
                        record.get("title", String.class),
                        record.get("display_name", String.class),
                        record.get("cover_image_key", String.class),
                        record.get("last_message", String.class),
                        record.get("last_message_type", String.class) == null ? null : MessageType.valueOf(record.get("last_message_type", String.class)),
                        record.get("last_message_at", java.time.LocalDateTime.class),
                        record.get("unread_count", Long.class) == null ? 0 : record.get("unread_count", Long.class)
                ));
    }

    @Override
    public List<ChatMessageProjection> findMessages(Long roomId, Long cursor, int limit) {
        var condition = field("room_id", Long.class).eq(roomId);
        if (cursor != null) {
            condition = condition.and(field("chat_message_id", Long.class).lt(cursor));
        }
        return dslContext
                .select(
                        field("chat_message_id", Long.class),
                        field("room_id", Long.class),
                        field("sender_id", Long.class),
                        field("message_type", String.class),
                        field("text_content", String.class),
                        field("status", String.class),
                        field("created_at", java.time.LocalDateTime.class)
                )
                .from(table("chat_message"))
                .where(condition)
                .orderBy(field("chat_message_id").desc())
                .limit(limit)
                .fetch(record -> new ChatMessageProjection(
                        record.get(field("chat_message_id", Long.class)),
                        record.get(field("room_id", Long.class)),
                        record.get(field("sender_id", Long.class)),
                        MessageType.valueOf(record.get(field("message_type", String.class))),
                        record.get(field("text_content", String.class)),
                        MessageStatus.valueOf(record.get(field("status", String.class))),
                        record.get(field("created_at", java.time.LocalDateTime.class))
                ));
    }

    @Override
    public List<ChatMessageAttachmentProjection> findAttachments(List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return List.of();
        }
        return dslContext
                .select(
                        field("chat_message_attachment_id", Long.class),
                        field("message_id", Long.class),
                        field("attachment_type", String.class),
                        field("storage_key", String.class),
                        field("content_type", String.class),
                        field("file_size", Long.class),
                        field("width", Integer.class),
                        field("height", Integer.class),
                        field("sort_order", Integer.class)
                )
                .from(table("chat_message_attachment"))
                .where(field("message_id", Long.class).in(messageIds))
                .orderBy(field("message_id").asc(), field("sort_order").asc())
                .fetch(record -> new ChatMessageAttachmentProjection(
                        record.get(field("chat_message_attachment_id", Long.class)),
                        record.get(field("message_id", Long.class)),
                        AttachmentType.valueOf(record.get(field("attachment_type", String.class))),
                        record.get(field("storage_key", String.class)),
                        record.get(field("content_type", String.class)),
                        record.get(field("file_size", Long.class)),
                        record.get(field("width", Integer.class)),
                        record.get(field("height", Integer.class)),
                        record.get(field("sort_order", Integer.class))
                ));
    }
}
