package com.example.demo.model;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@ToString
public class PageCriteria {

    @NotNull
    @Positive
    private Integer page;

    @NotNull
    @Positive
    @Max(value = 20000L)
    private Integer size;

    @Nullable
    private Integer totalRows;

    @Nullable
    private String orderColId;

    public String getOrderColId() {
        if(StringUtils.isNotBlank(this.orderColId)) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this.orderColId);
        } else {
            return null;
        }
    }

    private OrderType orderType;

    public Integer getStartRow() {
        return ((this.page - 1) * this.size) + 1;
    }

    public Integer getEndRow() {
        return ((this.page - 1) * this.size) + this.size;
    }

    public Integer getOffset() {
        return (this.page - 1) * this.size;
    }

    public Integer getLimit() {
        return this.size;
    }

}