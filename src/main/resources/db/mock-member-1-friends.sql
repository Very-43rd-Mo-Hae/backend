-- Local/dev seed script.
-- Creates 70 mock accepted friends for member_id = 1 and adds varied current-week schedules.
-- Run against the application schema, for example:
-- mysql -uroot -p mohae < src/main/resources/db/mock-member-1-friends.sql

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_mock_friends_for_member_1 $$

CREATE PROCEDURE seed_mock_friends_for_member_1()
BEGIN
    DECLARE friend_no INT DEFAULT 1;
    DECLARE friend_member_id BIGINT;
    DECLARE week_start DATE;
    DECLARE v_weekly_schedule_id BIGINT;
    DECLARE day_offset INT;
    DECLARE slot_no INT;
    DECLARE slot_start TIME;
    DECLARE current_slot_start TIME;
    DECLARE slot_status VARCHAR(30);
    DECLARE pattern_no INT;
    DECLARE group_no INT;
    DECLARE group_member_no INT;
    DECLARE group_member_friend_no INT;
    DECLARE v_member_group_id BIGINT;
    DECLARE participant_member_id BIGINT;
    DECLARE appointment_no INT;
    DECLARE appointment_participant_no INT;
    DECLARE appointment_friend_no INT;
    DECLARE appointment_slot_no INT;
    DECLARE appointment_participant_count INT;
    DECLARE v_appointment_id BIGINT;
    DECLARE appointment_start_at DATETIME;
    DECLARE appointment_end_at DATETIME;
    DECLARE appointment_week_start DATE;

    IF NOT EXISTS (SELECT 1 FROM members WHERE member_id = 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'member_id = 1 does not exist. Create the base member first.';
    END IF;

    SET week_start = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);

    DELETE FROM schedule_slot
    WHERE appointment_id IN (
        SELECT appointment_id
        FROM appointment
        WHERE owner_member_id = 1
          AND title LIKE '테스트 약속 %'
    );

    DELETE FROM appointment_reminder
    WHERE appointment_id IN (
        SELECT appointment_id
        FROM appointment
        WHERE owner_member_id = 1
          AND title LIKE '테스트 약속 %'
    );

    DELETE FROM appointment_participant
    WHERE appointment_id IN (
        SELECT appointment_id
        FROM appointment
        WHERE owner_member_id = 1
          AND title LIKE '테스트 약속 %'
    );

    DELETE FROM appointment
    WHERE owner_member_id = 1
      AND title LIKE '테스트 약속 %';

    WHILE friend_no <= 70 DO
        INSERT INTO members (
            email,
            name,
            image_url,
            provider_id,
            provider,
            created_at,
            updated_at
        )
        VALUES (
            CONCAT('mock.friend.', LPAD(friend_no, 3, '0'), '@mohae.local'),
            CONCAT('테스트 유저 ', LPAD(friend_no, 3, '0')),
            CONCAT('https://api.dicebear.com/8.x/initials/svg?seed=', '%ED%85%8C%EC%8A%A4%ED%8A%B8%20%EC%9C%A0%EC%A0%80%20', LPAD(friend_no, 3, '0')),
            CONCAT('mock-friend-', LPAD(friend_no, 3, '0')),
            'GOOGLE',
            NOW(),
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            email = VALUES(email),
            name = VALUES(name),
            image_url = VALUES(image_url),
            updated_at = NOW();

        SELECT member_id
        INTO friend_member_id
        FROM members
        WHERE provider = 'GOOGLE'
          AND provider_id = CONCAT('mock-friend-', LPAD(friend_no, 3, '0'));

        INSERT INTO friendship (
            requester_member_id,
            member_low_id,
            member_high_id,
            status,
            created_at,
            updated_at
        )
        VALUES (
            1,
            LEAST(1, friend_member_id),
            GREATEST(1, friend_member_id),
            'ACCEPTED',
            NOW(),
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            requester_member_id = VALUES(requester_member_id),
            status = 'ACCEPTED',
            updated_at = NOW();

        INSERT INTO weekly_schedule (
            member_id,
            week_start_date,
            created_at,
            updated_at
        )
        VALUES (
            friend_member_id,
            week_start,
            NOW(),
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            updated_at = NOW();

        SELECT ws.weekly_schedule_id
        INTO v_weekly_schedule_id
        FROM weekly_schedule ws
        WHERE ws.member_id = friend_member_id
          AND ws.week_start_date = week_start;

        DELETE FROM schedule_slot
        WHERE weekly_schedule_id = v_weekly_schedule_id;

        SET pattern_no = (friend_no - 1) % 8;
        SET current_slot_start = MAKETIME(HOUR(NOW()), IF(MINUTE(NOW()) < 30, 0, 30), 0);

        SET day_offset = 0;
        WHILE day_offset < 7 DO
            CASE pattern_no
                WHEN 0 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 2 DO
                        SET slot_start = MAKETIME(12 + slot_no, IF((friend_no + day_offset) % 2 = 0, 0, 30), 0);
                        SET slot_status = 'NEGOTIABLE';
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 1 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 4 DO
                        SET slot_start = MAKETIME(9 + slot_no, IF(day_offset % 2 = 0, 0, 30), 0);
                        SET slot_status = IF(slot_no % 2 = 0, 'UNAVAILABLE', 'NEGOTIABLE');
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 2 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 5 DO
                        SET slot_start = MAKETIME(14 + slot_no, IF((friend_no + slot_no) % 2 = 0, 0, 30), 0);
                        SET slot_status = 'UNAVAILABLE';
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 3 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 6 DO
                        SET slot_start = MAKETIME(8 + slot_no * 2, IF(day_offset % 3 = 0, 30, 0), 0);
                        SET slot_status = CASE slot_no % 3
                            WHEN 0 THEN 'NEGOTIABLE'
                            ELSE 'UNAVAILABLE'
                        END;
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 4 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 8 DO
                        SET slot_start = MAKETIME(10 + slot_no, 0, 0);
                        SET slot_status = 'UNAVAILABLE';
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 5 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 3 DO
                        SET slot_start = MAKETIME(18 + slot_no, IF(slot_no = 1, 30, 0), 0);
                        SET slot_status = 'NEGOTIABLE';
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                WHEN 6 THEN
                    SET slot_no = 0;
                    WHILE slot_no < 7 DO
                        SET slot_start = MAKETIME(7 + slot_no * 2, IF(slot_no % 2 = 0, 0, 30), 0);
                        SET slot_status = IF(slot_no IN (1, 4), 'NEGOTIABLE', 'UNAVAILABLE');
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
                ELSE
                    SET slot_no = 0;
                    WHILE slot_no < 10 DO
                        SET slot_start = MAKETIME(8 + slot_no, IF((slot_no + day_offset) % 2 = 0, 0, 30), 0);
                        SET slot_status = 'UNAVAILABLE';
                        INSERT INTO schedule_slot (
                            weekly_schedule_id,
                            schedule_date,
                            start_time,
                            end_time,
                            status,
                            appointment_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            v_weekly_schedule_id,
                            DATE_ADD(week_start, INTERVAL day_offset DAY),
                            slot_start,
                            ADDTIME(slot_start, '00:30:00'),
                            slot_status,
                            NULL,
                            NOW(),
                            NOW()
                        );
                        SET slot_no = slot_no + 1;
                    END WHILE;
            END CASE;

            SET day_offset = day_offset + 1;
        END WHILE;

        SET slot_no = 0;
        WHILE slot_no < 8 DO
            SET slot_status = CASE pattern_no
                WHEN 0 THEN 'AVAILABLE'
                WHEN 1 THEN IF(slot_no IN (0, 4), 'NEGOTIABLE', 'AVAILABLE')
                WHEN 2 THEN IF(slot_no IN (1, 5), 'UNAVAILABLE', 'AVAILABLE')
                WHEN 3 THEN IF(slot_no < 2, 'UNAVAILABLE', 'AVAILABLE')
                WHEN 4 THEN IF(slot_no < 4, 'UNAVAILABLE', 'AVAILABLE')
                WHEN 5 THEN IF(slot_no IN (2, 3), 'NEGOTIABLE', IF(slot_no = 6, 'UNAVAILABLE', 'AVAILABLE'))
                WHEN 6 THEN IF(slot_no MOD 2 = 0, 'UNAVAILABLE', 'NEGOTIABLE')
                ELSE 'UNAVAILABLE'
            END;

            IF slot_status <> 'AVAILABLE' THEN
                INSERT INTO schedule_slot (
                    weekly_schedule_id,
                    schedule_date,
                    start_time,
                    end_time,
                    status,
                    appointment_id,
                    created_at,
                    updated_at
                )
                VALUES (
                    v_weekly_schedule_id,
                    DATE_ADD(
                        CURDATE(),
                        INTERVAL FLOOR((TIME_TO_SEC(current_slot_start) + slot_no * 1800) / 86400) DAY
                    ),
                    SEC_TO_TIME(MOD(TIME_TO_SEC(current_slot_start) + slot_no * 1800, 86400)),
                    SEC_TO_TIME(MOD(TIME_TO_SEC(current_slot_start) + (slot_no + 1) * 1800, 86400)),
                    slot_status,
                    NULL,
                    NOW(),
                    NOW()
                )
                ON DUPLICATE KEY UPDATE
                    status = VALUES(status),
                    appointment_id = NULL,
                    updated_at = NOW();
            END IF;

            SET slot_no = slot_no + 1;
        END WHILE;

        SET friend_no = friend_no + 1;
    END WHILE;

    SET group_no = 1;
    WHILE group_no <= 4 DO
        SET v_member_group_id = NULL;

        SELECT member_group_id
        INTO v_member_group_id
        FROM member_group
        WHERE owner_member_id = 1
          AND name = CONCAT('테스트 그룹 ', LPAD(group_no, 3, '0'))
        LIMIT 1;

        IF v_member_group_id IS NULL THEN
            INSERT INTO member_group (
                owner_member_id,
                name,
                created_at,
                updated_at
            )
            VALUES (
                1,
                CONCAT('테스트 그룹 ', LPAD(group_no, 3, '0')),
                NOW(),
                NOW()
            );

            SET v_member_group_id = LAST_INSERT_ID();
        ELSE
            UPDATE member_group
            SET updated_at = NOW()
            WHERE member_group_id = v_member_group_id;
        END IF;

        INSERT INTO member_group_participant (
            member_group_id,
            member_id,
            role,
            status,
            created_at,
            updated_at
        )
        VALUES (
            v_member_group_id,
            1,
            'OWNER',
            'ACTIVE',
            NOW(),
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            role = 'OWNER',
            status = 'ACTIVE',
            updated_at = NOW();

        SET group_member_no = 1;
        WHILE group_member_no <= 3 + group_no * 2 DO
            SET group_member_friend_no = ((group_no - 1) * 9 + group_member_no - 1) % 70 + 1;

            SELECT member_id
            INTO participant_member_id
            FROM members
            WHERE provider = 'GOOGLE'
              AND provider_id = CONCAT('mock-friend-', LPAD(group_member_friend_no, 3, '0'));

            INSERT INTO member_group_participant (
                member_group_id,
                member_id,
                role,
                status,
                created_at,
                updated_at
            )
            VALUES (
                v_member_group_id,
                participant_member_id,
                'MEMBER',
                'ACTIVE',
                NOW(),
                NOW()
            )
            ON DUPLICATE KEY UPDATE
                role = 'MEMBER',
                status = 'ACTIVE',
                updated_at = NOW();

            SET group_member_no = group_member_no + 1;
        END WHILE;

        SET group_no = group_no + 1;
    END WHILE;

    SET appointment_no = 1;
    WHILE appointment_no <= 5 DO
        SET v_member_group_id = NULL;
        SET appointment_start_at = TIMESTAMP(
            DATE_ADD(CURDATE(), INTERVAL appointment_no DAY),
            MAKETIME(10 + appointment_no, IF(appointment_no % 2 = 0, 30, 0), 0)
        );
        SET appointment_end_at = DATE_ADD(appointment_start_at, INTERVAL IF(appointment_no % 2 = 0, 60, 90) MINUTE);

        IF appointment_no <= 3 THEN
            SELECT member_group_id
            INTO v_member_group_id
            FROM member_group
            WHERE owner_member_id = 1
              AND name = CONCAT('테스트 그룹 ', LPAD(appointment_no, 3, '0'))
            LIMIT 1;
        END IF;

        INSERT INTO appointment (
            owner_member_id,
            member_group_id,
            title,
            start_at,
            end_at,
            memo,
            created_at,
            updated_at
        )
        VALUES (
            1,
            v_member_group_id,
            CONCAT('테스트 약속 ', LPAD(appointment_no, 3, '0')),
            appointment_start_at,
            appointment_end_at,
            CASE appointment_no
                WHEN 1 THEN '연세대 정문'
                WHEN 2 THEN '신촌역 2번 출구'
                WHEN 3 THEN '학생회관'
                WHEN 4 THEN '온라인'
                ELSE '도서관 로비'
            END,
            NOW(),
            NOW()
        );

        SET v_appointment_id = LAST_INSERT_ID();
        SET appointment_participant_count = 2 + appointment_no;
        SET appointment_participant_no = 0;

        WHILE appointment_participant_no <= appointment_participant_count DO
            IF appointment_participant_no = 0 THEN
                SET participant_member_id = 1;
            ELSE
                SET appointment_friend_no = ((appointment_no - 1) * 6 + appointment_participant_no - 1) % 70 + 1;

                SELECT member_id
                INTO participant_member_id
                FROM members
                WHERE provider = 'GOOGLE'
                  AND provider_id = CONCAT('mock-friend-', LPAD(appointment_friend_no, 3, '0'));
            END IF;

            INSERT INTO appointment_participant (
                appointment_id,
                member_id,
                status,
                created_at,
                updated_at
            )
            VALUES (
                v_appointment_id,
                participant_member_id,
                'ACCEPTED',
                NOW(),
                NOW()
            )
            ON DUPLICATE KEY UPDATE
                status = 'ACCEPTED',
                updated_at = NOW();

            SET appointment_week_start = DATE_SUB(DATE(appointment_start_at), INTERVAL WEEKDAY(DATE(appointment_start_at)) DAY);

            INSERT INTO weekly_schedule (
                member_id,
                week_start_date,
                created_at,
                updated_at
            )
            VALUES (
                participant_member_id,
                appointment_week_start,
                NOW(),
                NOW()
            )
            ON DUPLICATE KEY UPDATE
                updated_at = NOW();

            SELECT ws.weekly_schedule_id
            INTO v_weekly_schedule_id
            FROM weekly_schedule ws
            WHERE ws.member_id = participant_member_id
              AND ws.week_start_date = appointment_week_start;

            SET appointment_slot_no = 0;
            WHILE appointment_slot_no < TIMESTAMPDIFF(MINUTE, appointment_start_at, appointment_end_at) / 30 DO
                SET slot_start = ADDTIME(TIME(appointment_start_at), SEC_TO_TIME(appointment_slot_no * 1800));

                INSERT INTO schedule_slot (
                    weekly_schedule_id,
                    schedule_date,
                    start_time,
                    end_time,
                    status,
                    appointment_id,
                    created_at,
                    updated_at
                )
                VALUES (
                    v_weekly_schedule_id,
                    DATE(appointment_start_at),
                    slot_start,
                    ADDTIME(slot_start, '00:30:00'),
                    'APPOINTMENT',
                    v_appointment_id,
                    NOW(),
                    NOW()
                )
                ON DUPLICATE KEY UPDATE
                    status = 'APPOINTMENT',
                    appointment_id = v_appointment_id,
                    updated_at = NOW();

                SET appointment_slot_no = appointment_slot_no + 1;
            END WHILE;

            SET appointment_participant_no = appointment_participant_no + 1;
        END WHILE;

        SET appointment_no = appointment_no + 1;
    END WHILE;
END $$

DELIMITER ;

CALL seed_mock_friends_for_member_1();

DROP PROCEDURE IF EXISTS seed_mock_friends_for_member_1;
