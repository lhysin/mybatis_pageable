package com.example.demo.model;

import java.io.Serializable;
import java.util.List;

public class PageSerializable<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    protected long totalPages;

    protected List<T> list;

    public PageSerializable() {
    }

    public PageSerializable(List<T> list) {
        this.list = list;
        if(list instanceof Pages){
            //this.totalPages = ((Page<T>)list).getTotalPages();
        } else {
            this.totalPages = list.size();
        }
    }

    public static <T> PageSerializable<T> of(List<T> list){
        return new PageSerializable<T>(list);
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageSerializable{" +
                "totalPages=" + totalPages +
                ", list=" + list +
                '}';
    }
}