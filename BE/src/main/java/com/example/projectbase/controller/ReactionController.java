package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.RestData;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.ReactionRequestDto;
import com.example.projectbase.domain.dto.response.ReactionResponseDto;
import com.example.projectbase.domain.entity.Reaction;
import com.example.projectbase.repository.ReactionRepository;
import com.example.projectbase.service.ReactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestApiV1
@Log4j2
@RequiredArgsConstructor
@Tag(name = "Reaction")
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping(value = UrlConstant.Post.GET_REACTIONS)
    public ResponseEntity<RestData<?>> getReactions(@PathVariable Long postId,
                                                    @Valid @ParameterObject PaginationRequestDto paginationRequestDto) {
        log.info("post id: {}", postId);
        PaginationResponseDto responseDto = reactionService.getReactionsOfPost(paginationRequestDto, postId);
        return VsResponseUtil.success(responseDto);
    }

    @PostMapping(value = UrlConstant.Post.REACTION_FOR_POST)
    public ResponseEntity<RestData<?>> createReaction(@PathVariable Long postId,
                                                      @RequestBody @Valid ReactionRequestDto request) {
        ReactionResponseDto responseDto = reactionService.reactionForPost(request, postId);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @DeleteMapping(value = UrlConstant.Post.CANCEL_REACTION_OF_POST)
    public ResponseEntity<RestData<?>> deleteReaction(@PathVariable Long postId) {
        boolean responseDto = reactionService.cancelReaction(postId);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

}
