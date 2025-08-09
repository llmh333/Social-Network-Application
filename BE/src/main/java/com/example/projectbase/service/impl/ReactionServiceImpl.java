package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.ReactionTypeConstant;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.ReactionRequestDto;
import com.example.projectbase.domain.dto.response.ReactionResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.Reaction;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.ReactionMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.ReactionRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private static final Logger log = LogManager.getLogger(ReactionServiceImpl.class);
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final ReactionMapper reactionMapper;
    private final UserRepository userRepository;
    private final MailServiceImpl mailService;

    @PreAuthorize("isAuthenticated()")
    @Override
    public ReactionResponseDto reactionForPost(ReactionRequestDto request, Long postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Reaction reaction = reactionRepository.findByUser_IdAndPost_Id(userPrincipal.getId(), postId);
        boolean isNewReaction = false;

        if (reaction == null) {
            reaction = new Reaction();
            isNewReaction = true;
        }
        String reactionType = request.getReactionType();
        if (reactionType.equals(ReactionTypeConstant.LIKE.name())) {
            reaction.setReactionType(ReactionTypeConstant.LIKE);
        } else if (reactionType.equals(ReactionTypeConstant.LOVE.name())) {
            reaction.setReactionType(ReactionTypeConstant.LOVE);
        } else if (reactionType.equals(ReactionTypeConstant.HAHA.name())) {
            reaction.setReactionType(ReactionTypeConstant.HAHA);
        }
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userPrincipal.getId()})
        );
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)})
        );

        User userOfPost= userRepository.findById(post.getCreatedBy())
                .orElseThrow(() -> new  NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(post.getCreatedBy())}));

        reaction.setPost(post);
        reaction.setUser(user);
        ReactionResponseDto responseDto = reactionMapper.toReactionResponseDto(reactionRepository.save(reaction));
        if (isNewReaction) {
            post.setReactionCount(post.getReactionCount() + 1);
            post.getReactions().add(reaction);
            postRepository.save(post);
        }
        responseDto.setPostId(post.getId());
        responseDto.setUserId(user.getId());


//        String content = "Người dùng "+user.getFirstName()+" " + user.getLastName() + " đã thả cảm xúc vào một bài viết của bạn";
//        mailService.sendEmailWithObject(userOfPost.getEmail(),content,"Thông báo từ Chill And Chill");
        return responseDto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean cancelReaction(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        int response = reactionRepository.deleteByUserIdAndPostId(userPrincipal.getId(), post.getId());

        if (response == 0) {
            throw new NotFoundException(ErrorMessage.Reaction.ERR_NOT_FOUND);
        }
        post.setReactionCount(post.getReactionCount() - 1);
        postRepository.save(post);
        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto getReactionsOfPost(PaginationRequestDto paginationRequestDto, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)})
        );
        Integer pageNum = paginationRequestDto.getPageNum();
        Integer pageSize = paginationRequestDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Reaction> reactions = reactionRepository.findAllByPost(post, pageable);
        log.info("Reactions found: {}", reactions);
        List<ReactionResponseDto> reactionList = reactions.getContent().stream().map(reactionMapper::toReactionResponseDto).collect(Collectors.toList());
        PagingMeta metadata = new PagingMeta();
        metadata.setPageNum(pageNum);
        metadata.setPageSize(pageSize);
        metadata.setTotalElements(reactions.getTotalElements());
        metadata.setTotalPages(reactions.getTotalPages());
        return new PaginationResponseDto<>(metadata, reactionList);

    }
}
