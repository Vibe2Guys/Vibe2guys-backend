package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamMember;
import com.vibe2guys.backend.team.domain.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamIdAndStatusOrderByIdAsc(Long teamId, TeamMemberStatus status);

    List<TeamMember> findByUserIdAndStatus(Long userId, TeamMemberStatus status);

    List<TeamMember> findByTeamCourseIdAndStatus(Long courseId, TeamMemberStatus status);

    Optional<TeamMember> findByTeamIdAndUserIdAndStatus(Long teamId, Long userId, TeamMemberStatus status);

    List<TeamMember> findByTeamCourseIdAndUserIdAndStatus(Long courseId, Long userId, TeamMemberStatus status);
}
