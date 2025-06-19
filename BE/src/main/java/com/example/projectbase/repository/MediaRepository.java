package com.example.projectbase.repository;

import com.example.projectbase.domain.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("select u from Media u where LOWER(u.resourceType) like lower(concat('%', :keyword, '%'))")
    Page<Media> searchByKeyWord(@Param("keyword") String keyword, Pageable pageable);


    List<Media> findAllByPublicIdIn(List<String> publicIds);

    void deleteAllByPublicIdIn(List<String> publicIds);
}
