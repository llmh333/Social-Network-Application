package com.example.projectbase.service.impl;

import com.example.projectbase.constant.CommonConstant;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.PostStatusConstant;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PaginationSortRequestDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.AwsS3ResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.*;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.PostCategoryRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.ReactionRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Log4j2
@Service
//@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final ReactionRepository reactionRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final RedisServiceImpl redisService;
    private final PostCategoryServiceImpl postCategoryService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PostMapper postMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.request-topic}")
    private String requestTopic;

    @PreAuthorize("isAuthenticated()")
    @Override
    public PostResponseDto createPost(PostRequestDto requestDto, List<File> files, List<MultipartFile> multipartFiles, List<String> contentTypeFileList) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        if (!files.get(0).isFile()) {
            throw new BadRequestException(ErrorMessage.Post.ERR_FILES_NULL);
        }

        if (requestDto.getMediaType() != null) {
            String contentTypeMedia = contentTypeFileList.get(0).split("/")[0];
            log.info("Content type Media: {}", contentTypeMedia);
            if (!contentTypeMedia.equals(requestDto.getMediaType().toString().toLowerCase())) {
                throw new BadRequestException(ErrorMessage.Post.ERR_FILES_INVALID_FORMAT);
            }
        }

        AwsS3ResponseDto awsS3ResponseDto = awsS3Service.uploadMultiFile(files);
        awsS3ResponseDto.setType(requestDto.getMediaType().toString().toLowerCase());

        PostCategory postCategory = postCategoryService.createPostCategory(requestDto.getCategory());

        Post post = buildPostFromDto(requestDto, postCategory);
        post.setStatus(PostStatusConstant.PENDING_MODERATION);
        Post savedPost = postRepository.save(post);


        log.info("Đã tạo Post với trạng thái PENDING_MODERATION, postId: {}", savedPost.getId());

        Map<String, Object> moderationRequest = new HashMap<>();
        moderationRequest.put("postId", savedPost.getId().toString());
        moderationRequest.put("type", requestDto.getMediaType().toString().toUpperCase());
        moderationRequest.put("s3Urls", awsS3ResponseDto.getUrls());
        moderationRequest.put("contentType", contentTypeFileList);
        moderationRequest.put("singer_name", requestDto.getSingerName());

        String payload = objectMapper.writeValueAsString(moderationRequest);
        kafkaTemplate.send(requestTopic, payload);
        log.info("Đã gửi yêu cầu kiểm duyệt cho postId: {}", savedPost.getId());

        return postMapper.toPostResponseDto(savedPost);

    }

    @PreAuthorize("isAuthenticated() and @postServiceImpl.isOwner(#postId, authentication.name)")
    @Transactional
    @Override
    public void deletePost(Long postId) {
        Post post = findPostOrThrow(postId);
        postRepository.delete(post);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String keyword = request.getKeyword();

        Sort sort = Sort.by(request.getSortBy(SortByDataConstant.POST));
        sort = Boolean.FALSE.equals(request.getIsAscending()) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<Post> postPage = (keyword != null && !keyword.isBlank())
                ? postRepository.searchByTitleKeyword(keyword, pageable)
                : postRepository.findAll(pageable);
        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? CommonConstant.SORT_TYPE_ASC : CommonConstant.SORT_TYPE_DESC)
                .totalElements(postPage.getTotalElements())
                .build();

        return new PaginationResponseDto<>(meta, dtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto<PostResponseDto> getFavoritePosts(PaginationSortRequestDto request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();

        Sort sort = Sort.by(request.getSortBy(SortByDataConstant.POST));
        sort = Boolean.FALSE.equals(request.getIsAscending()) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<Post> postPage = postRepository.findFavoritePostsByUserId(userPrincipal.getId(), pageable);
        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? CommonConstant.SORT_TYPE_ASC : CommonConstant.SORT_TYPE_DESC)
                .totalElements(postPage.getTotalElements())
                .build();

        return new PaginationResponseDto<>(meta, dtoList);
    }

    @Override
    public PostResponseDto getPostById(Long postId) {
        Post post = findPostOrThrow(postId);
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reaction reaction = reactionRepository.findByUser_IdAndPost_Id(userPrincipal.getId(), postId);
        boolean reactedByCurrentUser;
        reactedByCurrentUser = false;
        if (reaction != null) {
            reactedByCurrentUser = true;
        }
        List<Media> mediaList = post.getMediaList();
        PostResponseDto postResponseDto = postMapper.toPostResponseDto(post);
        postResponseDto.setReactedByCurrentUser(reactedByCurrentUser);
        if (post.getOriginalPost() != null) {
            postResponseDto.setOriginalPostId(post.getOriginalPost().getId());
        }
        return postResponseDto;
    }

    @Override
    public PaginationResponseDto<PostResponseDto> getPostsTrendingForUser(PaginationFullRequestDto request) {
        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        List<String> categoryNames = getTrendingCategories();
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Post> postPage = postRepository.findByCategoryNameIn(categoryNames, pageable);

        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? CommonConstant.SORT_TYPE_ASC : CommonConstant.SORT_TYPE_DESC)
                .totalElements(postPage.getTotalElements())
                .build();

        return new  PaginationResponseDto(meta, dtoList);
    }

    private Post buildPostFromDto(PostRequestDto requestDto, PostCategory postCategory) {
        return Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .reactionCount(0L)
                .commentCount(0L)
                .shareCount(0L)
                .category(postCategory)
                .mediaType(requestDto.getMediaType())
                .mediaList(new ArrayList<>())
                .build();
    }


    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));
    }

    public boolean isOwner(Long postId, String username) {
        Post post = findPostOrThrow(postId);
        User user = userRepository.findById(post.getCreatedBy())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(post.getCreatedBy())}));
        return user.getUsername().equals(username);
    }

    private List<String> getTrendingCategories() {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String trendingCategory = redisService.get("username:"+userPrincipal.getUsername()+":trending");
            if (trendingCategory != null) {
                Map<String, Object> dataTrending = objectMapper.readValue(trendingCategory, new TypeReference<>() {});
                Map<String, Object> sortedTrending = dataTrending.entrySet()
                        .stream()
                        .sorted(Comparator.comparing(e -> (Integer)e.getValue(), Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));

                List<String> categoryNames = sortedTrending.entrySet()
                        .stream()
                        .limit(5)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                return categoryNames;
            }
            List<PostCategory> postCategoryList = postCategoryRepository.findTop5ByOrderByInteractionCountDesc();
            List<String> categoryNames = new ArrayList<>();
            postCategoryList.stream().forEach(postCategory -> {
                categoryNames.add(postCategory.getName());
            });
            return categoryNames;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(ErrorMessage.ERR_EXCEPTION_GENERAL);
        }

    }
}
