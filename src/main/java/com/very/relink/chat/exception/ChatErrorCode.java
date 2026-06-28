package com.very.relink.chat.exception;

import com.very.relink.core.exception.DomainException;
import com.very.relink.core.exception.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements BaseErrorCode<DomainException> {

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방 참여자를 찾을 수 없습니다."),
    NOT_ACTIVE_CHAT_PARTICIPANT(HttpStatus.FORBIDDEN, "활성 상태의 채팅방 참여자가 아닙니다."),
    CANNOT_CREATE_DIRECT_ROOM_WITH_SELF(HttpStatus.BAD_REQUEST, "자기 자신과 1:1 채팅방을 만들 수 없습니다."),
    INVALID_GROUP_PARTICIPANTS(HttpStatus.BAD_REQUEST, "그룹 채팅 참여자 구성이 올바르지 않습니다."),
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 메시지 타입입니다."),
    EMPTY_TEXT_MESSAGE(HttpStatus.BAD_REQUEST, "텍스트 메시지는 내용을 포함해야 합니다."),
    IMAGE_MESSAGE_REQUIRES_ATTACHMENT(HttpStatus.BAD_REQUEST, "이미지 메시지는 첨부 파일이 필요합니다."),
    TEXT_MESSAGE_CANNOT_HAVE_ATTACHMENT(HttpStatus.BAD_REQUEST, "텍스트 메시지는 첨부 파일을 포함할 수 없습니다."),
    UNSUPPORTED_ATTACHMENT_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다."),
    ATTACHMENT_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "첨부 파일 크기가 허용 범위를 초과했습니다."),
    DUPLICATED_CLIENT_MESSAGE_ID(HttpStatus.CONFLICT, "이미 처리된 클라이언트 메시지 ID입니다."),
    INVALID_READ_CURSOR(HttpStatus.BAD_REQUEST, "읽음 커서가 올바르지 않습니다."),
    STORAGE_PRESIGNED_URL_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "첨부 파일 업로드 URL 발급에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ChatErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public DomainException toException() {
        return new DomainException(httpStatus, this);
    }
}
