-- Local/dev seed script.
-- Creates 70 mock accepted friends for member_id = 1 and adds arbitrary current-week schedules.
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
    DECLARE slot_status VARCHAR(30);

    IF NOT EXISTS (SELECT 1 FROM members WHERE member_id = 1) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'member_id = 1 does not exist. Create the base member first.';
    END IF;

    SET week_start = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);

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
            CONCAT('Mock Friend ', LPAD(friend_no, 3, '0')),
            CONCAT('https://api.dicebear.com/8.x/initials/svg?seed=Mock%20Friend%20', LPAD(friend_no, 3, '0')),
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

        SET day_offset = 0;
        WHILE day_offset < 7 DO
            SET slot_no = 0;
            WHILE slot_no < 4 DO
                SET slot_start = MAKETIME(9 + ((friend_no + day_offset + slot_no * 2) % 10), IF((friend_no + day_offset + slot_no) % 2 = 0, 0, 30), 0);
                SET slot_status = CASE (friend_no + day_offset + slot_no) % 3
                    WHEN 0 THEN 'UNAVAILABLE'
                    WHEN 1 THEN 'NEGOTIABLE'
                    ELSE 'AVAILABLE'
                END;

                IF slot_status <> 'AVAILABLE' THEN
                    INSERT IGNORE INTO schedule_slot (
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
                END IF;

                SET slot_no = slot_no + 1;
            END WHILE;

            SET day_offset = day_offset + 1;
        END WHILE;

        SET friend_no = friend_no + 1;
    END WHILE;
END $$

DELIMITER ;

CALL seed_mock_friends_for_member_1();

DROP PROCEDURE IF EXISTS seed_mock_friends_for_member_1;
