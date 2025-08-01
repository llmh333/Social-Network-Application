package com.example.projectbase.repository;

import com.example.projectbase.domain.entity.Follow;
import com.example.projectbase.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowingAndFollower(User following, User follower);

    Follow findByFollowingAndFollower(User following, User follower);

    Page<Follow> findAllByFollower(User follower, Pageable pageable);

    Page<Follow> findAllByFollowing(User following, Pageable pageable);
}
