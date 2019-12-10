package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtils {

    // @formatter:off
    public static PageCriteria pageable(Integer page, Integer size) {
        return PageCriteria.builder()
                .page(page)
                .size(size)
                .build();
    }

    public static <T> Pages<T> of(List<T> list, PageCriteria pageCriteria) {
        if(list.size() > pageCriteria.getTotalRows()) {
            throw new RuntimeException();
        } else {
            return Pages.<T>builder()
                    .page(pageCriteria.getPage())
                    .size(pageCriteria.getSize())
                    .totalRows(pageCriteria.getTotalRows())
                    .list(list)
                    .serverTime(LocalDateTime.now())
                    .build();
        }
    }
    // @formatter:on
}
