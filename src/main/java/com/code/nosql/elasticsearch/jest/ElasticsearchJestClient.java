package com.code.nosql.elasticsearch.jest;

import com.code.nosql.elasticsearch.IElasticsearchClient;
import org.elasticsearch.client.RestClientBuilder;

import java.util.List;

/**
 * @Description ES的REST 客户端
 * @Author liufei
 * @Date 2020/1/14 14:55
 */
public class ElasticsearchJestClient implements IElasticsearchClient {
    public ElasticsearchJestClient() {
        RestClientBuilder.HttpClientConfigCallback
        return client;
    }
    @Override
    public void bulk(List action) {

    }

    @Override
    public List search(String esql) {
        return null;
    }

    @Override
    public Object get(String id) {
        return null;
    }

    @Override
    public Object post(String cmd) {
        return null;
    }

    @Override
    public void delete(String esql) {

    }

    @Override
    public void update(String esql) {

    }
}
