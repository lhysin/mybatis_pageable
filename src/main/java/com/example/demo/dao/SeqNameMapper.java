package com.example.demo.dao;

import java.util.List;

import com.example.demo.model.PageCriteria;
import com.example.demo.model.SeqName;

public interface SeqNameMapper {

    public List<SeqName> findAll();
    public SeqName findOne(Integer seq);
    public List<SeqName> findAll(PageCriteria pageCriteria);
}
