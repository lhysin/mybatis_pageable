package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pages<T> {

    private List<T> list;
    private Integer page;
    private Integer size;
    private Integer totalRows;
    private LocalDateTime serverTime;

}
