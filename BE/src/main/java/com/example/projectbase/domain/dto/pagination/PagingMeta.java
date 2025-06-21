package com.example.projectbase.domain.dto.pagination;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class PagingMeta {

  private Long totalElements;

  private Integer totalPages;

  private Integer pageNum;

  private Integer pageSize;

  private String sortBy;

  private String sortType;

}