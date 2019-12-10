package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.SeqNameMapper;
import com.example.demo.model.PageCriteria;
import com.example.demo.model.PageUtils;
import com.example.demo.model.Pages;
import com.example.demo.model.SeqName;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TestController {
    
    @Autowired
    private SeqNameMapper seqNameMapper;

    @RequestMapping("/test")
    @ResponseBody
    public ResponseEntity<Object> test (@RequestParam("page") Integer page, @RequestParam("size") Integer size){
        
        
        SeqName seqName = seqNameMapper.findOne(page);
        
        PageCriteria pageCriteria = PageUtils.pageable(page, size);
        Pages<SeqName> seqNamePageList = PageUtils.of(seqNameMapper.findAll(pageCriteria), pageCriteria);
//        log.debug("seqNameList : {} ", seqNamePageList);
//
//        List<SeqName> seqNameList = seqNameMapper.findAll();
//        log.debug("seqNameList : {} ", seqNameList);

        return ResponseEntity.ok(seqNamePageList);
    }
}