package com.very.relink.chat.domain;

public final class ChatEnums {

    private ChatEnums() {
    }

    public enum RoomType {
        DIRECT,
        GROUP,
        APPOINTMENT
    }

    public enum RoomStatus {
        ACTIVE,
        CLOSED
    }

    public enum ParticipantRole {
        OWNER,
        MEMBER
    }

    public enum ParticipantStatus {
        ACTIVE,
        LEFT,
        BLOCKED
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        SYSTEM
    }

    public enum MessageStatus {
        SENT,
        DELETED
    }

    public enum AttachmentType {
        IMAGE
    }

    public enum OutboxStatus {
        PENDING,
        PUBLISHED,
        FAILED
    }

    public enum OutboxEventType {
        CHAT_MESSAGE_CREATED,
        CHAT_ROOM_READ
    }
}
