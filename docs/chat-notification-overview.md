# 알림/채팅 기능 구현 요약

## 알림 기능

- 위치: `com.very.relink.notification`
- Web Push 구독 정보를 `notification_target` 테이블에 저장합니다.
- `CurrentUserProvider`가 Spring Security 인증 정보에서 현재 회원 ID를 가져옵니다.
- `WebPushNotificationService`는 구독 등록, 비활성화, 테스트 발송을 담당합니다.
- 실제 발송은 `WebPushSenderPort`를 통해 분리했고, `WebPushSenderAdapter`가 VAPID 기반 Web Push 전송을 수행합니다.
- Redis는 `NotificationSendDeduplicationPort` 구현체에서 중복 발송 방지 용도로만 사용합니다.

## 채팅 기능

- 위치: `com.very.relink.chat`
- REST 기반 채팅 코어를 먼저 구현했고, WebSocket/Redis 실시간 전송은 Stub 어댑터 경계만 만들었습니다.
- 쓰기 모델은 JPA 엔티티와 Repository를 사용합니다.
- 읽기 모델은 `ChatQueryPort`와 `ChatJooqQueryAdapter`를 통해 jOOQ 조회로 분리했습니다.
- 메시지 저장과 `chat_outbox_event` 저장은 같은 트랜잭션에서 처리합니다.
- 이미지 파일은 DB에 public URL을 저장하지 않고 `storageKey`만 저장합니다.
- 응답에 필요한 이미지 URL은 `StorageUrlResolver`를 통해 조립합니다.

## 채팅 API

- `POST /api/chat/rooms/direct`: 1:1 채팅방 생성 또는 기존 방 반환
- `POST /api/chat/rooms/group`: 그룹 채팅방 생성
- `GET /api/chat/rooms`: 내 채팅방 목록 조회
- `GET /api/chat/rooms/{roomId}/messages?cursor=&size=`: 메시지 커서 페이지 조회
- `POST /api/chat/rooms/{roomId}/messages`: 텍스트/이미지 메시지 저장
- `PATCH /api/chat/rooms/{roomId}/read`: 읽음 커서 갱신
- `POST /api/chat/attachments/presigned-url`: 이미지 업로드용 URL과 storageKey 발급

## 주요 테이블

- `chat_room`: DIRECT/GROUP 공통 채팅방
- `direct_chat_room`: 1:1 채팅방 중복 방지용 member pair
- `chat_participant`: 채팅방 참여자와 상태
- `chat_message`: 메시지 본문
- `chat_message_attachment`: 이미지 첨부 메타데이터와 storageKey
- `chat_read_cursor`: 사용자별 마지막 읽은 메시지
- `chat_outbox_event`: 실시간 발행을 위한 Outbox 이벤트

## 이후 확장 포인트

- `WebSocketChatMessagePublisher`를 실제 WebSocket 또는 Redis Pub/Sub 발행으로 교체합니다.
- `ChatOutboxRelay`에 스케줄러/락/재시도 정책을 붙여 PENDING 이벤트를 발행합니다.
- `S3StorageAdapter`의 Stub URL 발급을 AWS S3 Presigner 또는 CloudFront signed URL로 교체합니다.
- DIRECT 채팅방 표시 이름은 현재 상대 회원 ID 기반 placeholder이며, 회원 프로필 조회 projection과 연결하면 됩니다.
