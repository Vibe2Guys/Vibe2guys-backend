package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamMember;
import com.vibe2guys.backend.team.domain.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamIdAndStatusOrderByIdAsc(Long teamId, TeamMemberStatus status);

    List<TeamMember> findByUserIdAndStatus(Long userId, TeamMemberStatus status);

    List<TeamMember> findByTeamCourseIdAndStatus(Long courseId, TeamMemberStatus status);

    Optional<TeamMember> findByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMemberStatus status);

    List<TeamMember> findByTeamCourseIdAndUserIdAndStatus(Long courseId, Long userId, TeamMemberStatus status);

    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMemberStatus status);

    @Query("""
            select tm.team.id as teamId, count(tm) as memberCount
            from TeamMember tm
            where tm.team.id in :teamIds
              and tm.status = :status
            group by tm.team.id
            """)
    List<TeamMemberCountView> countByTeamIdsAndStatus(List<Long> teamIds, TeamMemberStatus status);
}
