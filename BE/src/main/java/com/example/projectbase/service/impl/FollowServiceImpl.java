package com.example.projectbase.service.impl;


import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.FollowRequestDto;
import com.example.projectbase.domain.dto.response.FollowResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.domain.entity.Follow;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.FollowMapper;
import com.example.projectbase.domain.mapper.UserMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.ConflictException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.FollowRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @PreAuthorize("isAuthenticated()")
    @Override
    public FollowResponseDto follow(FollowRequestDto requestDto) {
        String followingId = requestDto.getFollowingId();
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String followerId = userPrincipal.getId();

        if (followerId.equals(followingId)) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_FOLLOW_YOURSELF);
        }

        Map<String, User> relatedUsers = getFollowerAndFollowing(followerId, followingId);
        User following = relatedUsers.get("following");
        User follower = relatedUsers.get("follower");

        boolean follow = followRepository.existsByFollowingAndFollower(following, follower);
        if (follow) {
            throw new ConflictException(ErrorMessage.Follow.ERR_DUPLICATE);
        }

        Follow newFollow = new Follow();
        newFollow.setFollowing(following);
        newFollow.setFollower(follower);
        FollowResponseDto responseDto = followMapper.toFollowResponseDto(followRepository.save(newFollow));
        responseDto.setFollowerId(followerId);
        responseDto.setFollowingId(following.getId());

        return responseDto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean unfollow(String followingId) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = userPrincipal.getId();

        Map<String, User> relatedUsers = getFollowerAndFollowing(currentUserId, followingId);
        User following = relatedUsers.get("following");
        User follower = relatedUsers.get("follower");

        if (!isFollowing(following, follower)) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_UNFOLLOW_USER, new String[]{followingId});
        }
        return true;
    }

    @Override
    public boolean removeFollower(String followerId) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = userPrincipal.getId();

        Map<String, User> relatedUsers = getFollowerAndFollowing(followerId, currentUserId);
        User following = relatedUsers.get("following");
        User follower = relatedUsers.get("follower");

        if (!isFollowing(following, follower)) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_REMOVE_FOLLOWER, new String[]{followerId});
        }
        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto<UserSummaryDto> getFollowers(PaginationRequestDto requestDto) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = userPrincipal.getId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{currentUserId})
        );

        int pageSize = requestDto.getPageSize();
        int pageNum = requestDto.getPageNum();

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Follow> followers = followRepository.findAllByFollowing(currentUser, pageable);

        List<UserSummaryDto> userSummaries = new ArrayList<>();
        followers.forEach(follow -> {
            userSummaries.add(userMapper.toUserSummaryDto(follow.getFollower()));
        });
        PagingMeta metadata = PagingMeta.builder()
                .totalPages(followers.getTotalPages())
                .totalElements(followers.getTotalElements())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
        return new PaginationResponseDto<>(metadata, userSummaries);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto<UserSummaryDto> getFollowings(PaginationRequestDto requestDto) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = userPrincipal.getId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{currentUserId})
        );

        int pageSize = requestDto.getPageSize();
        int pageNum = requestDto.getPageNum();

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Follow> followers = followRepository.findAllByFollower(currentUser, pageable);

        List<UserSummaryDto> userSummaries = new ArrayList<>();
        followers.forEach(follow -> {
            userSummaries.add(userMapper.toUserSummaryDto(follow.getFollowing()));
        });
        PagingMeta metadata = PagingMeta.builder()
                .totalPages(followers.getTotalPages())
                .totalElements(followers.getTotalElements())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
        return new PaginationResponseDto<>(metadata, userSummaries);
    }

    @PreAuthorize("isAuthenticated()")
    private Map<String, User> getFollowerAndFollowing(String followerId, String followingId) {
        User following = userRepository.findById(followingId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{followingId})
        );

        User follower = userRepository.findById(followerId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{followerId})
        );
        Map<String, User> resultUser = new HashMap<>();
        resultUser.put("follower", follower);
        resultUser.put("following", following);
        return resultUser;
    }

    private boolean isFollowing(User following, User follower) {
        Follow follow = followRepository.findByFollowingAndFollower(following, follower);
        if (follow == null) {
            return false;
        }
        followRepository.delete(follow);
        return true;
    }
}
