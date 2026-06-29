package com.very.relink.friend.adapter.out.persistence;

import com.very.relink.friend.domain.FriendshipStatus;
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
}
