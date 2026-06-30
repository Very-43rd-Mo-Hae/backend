package com.very.relink.friend.adapter.out.persistence;

import com.very.relink.friend.domain.FriendshipStatus;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipJpaEntity, Long> {

    @Query(
            value = """
                    select f
                    from FriendshipJpaEntity f
                    join fetch f.memberLow low
                    join fetch f.memberHigh high
                    where (low.id = :memberId or high.id = :memberId)
                      and f.status = :status
                      and (
                        :keyword is null
                        or lower(case when low.id = :memberId then high.name else low.name end)
                           like lower(concat('%', :keyword, '%'))
                      )
                    order by
                      case when low.id = :memberId then high.name else low.name end asc,
                      f.id asc
                    """,
            countQuery = """
                    select count(f)
                    from FriendshipJpaEntity f
                    join f.memberLow low
                    join f.memberHigh high
                    where (low.id = :memberId or high.id = :memberId)
                      and f.status = :status
                      and (
                        :keyword is null
                        or lower(case when low.id = :memberId then high.name else low.name end)
                           like lower(concat('%', :keyword, '%'))
                      )
                    """
    )
    List<FriendshipJpaEntity> findFriends(
            @Param("memberId") Long memberId,
            @Param("status") FriendshipStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select count(f)
            from FriendshipJpaEntity f
            join f.memberLow low
            join f.memberHigh high
            where (low.id = :memberId or high.id = :memberId)
              and f.status = :status
              and (
                :keyword is null
                or lower(case when low.id = :memberId then high.name else low.name end)
                   like lower(concat('%', :keyword, '%'))
              )
            """)
    long countFriends(
            @Param("memberId") Long memberId,
            @Param("status") FriendshipStatus status,
            @Param("keyword") String keyword
    );

    @Query("""
            select m
            from MemberJpaEntity m
            where m.id in (
                select high.id
                from FriendshipJpaEntity f
                join f.memberLow low
                join f.memberHigh high
                where low.id = :memberId
                  and f.status = com.very.relink.friend.domain.FriendshipStatus.ACCEPTED
            )
            or m.id in (
                select low.id
                from FriendshipJpaEntity f
                join f.memberLow low
                join f.memberHigh high
                where high.id = :memberId
                  and f.status = com.very.relink.friend.domain.FriendshipStatus.ACCEPTED
            )
            order by m.name asc, m.id asc
            """)
    List<MemberJpaEntity> findAcceptedFriendMembers(@Param("memberId") Long memberId);

    @Query("""
            select count(f) > 0
            from FriendshipJpaEntity f
            join f.memberLow low
            join f.memberHigh high
            where f.status = com.very.relink.friend.domain.FriendshipStatus.ACCEPTED
              and ((low.id = :memberId and high.id = :friendId)
                or (low.id = :friendId and high.id = :memberId))
            """)
    boolean existsAcceptedFriendship(
            @Param("memberId") Long memberId,
            @Param("friendId") Long friendId
    );
}
