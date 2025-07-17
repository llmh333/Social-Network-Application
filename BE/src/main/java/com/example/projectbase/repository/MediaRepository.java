package com.example.projectbase.repository;

import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findAllByPublicIdIn(List<String> publicIds);

    void deleteAllByPublicIdIn(List<String> publicIds);

    Media findMediaByPublicId(String publicId);
}
