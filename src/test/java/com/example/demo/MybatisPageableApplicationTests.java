package com.example.demo;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.dao.SeqNameMapper;
import com.example.demo.model.Pages;
import com.example.demo.model.PageCriteria;
import com.example.demo.model.PageUtils;
import com.example.demo.model.SeqName;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MybatisPageableApplicationTests {

    @Autowired
    private SeqNameMapper seqNameMapper;

    @Test
    public void contextLoads() {

        PageCriteria pageCriteria = PageUtils.pageable(1, 10);
        Pages<SeqName> seqNamePageList = PageUtils.of(seqNameMapper.findAll(pageCriteria), pageCriteria);

        List<SeqName> seqNameList = seqNameMapper.findAll();
        log.debug("seqNameList : {} ", seqNameList);
    }

}
