package com.code.nosql.elasticsearch.rest;

import com.code.nosql.elasticsearch.IElasticsearchClient;

import java.util.List;

/**
 * @Description ES的REST 客户端
 * @Author liufei
 * @Date 2020/1/14 14:55
 */
public class ElasticsearchRestClient implements IElasticsearchClient {
    @Override
    public void bulk(List action) {

    }

    @Override
    public List search(String esql) {
        return null;
    }

    @Override
    public void delete(String esql) {

    }

    @Override
    public void update(String esql) {

    }
}
