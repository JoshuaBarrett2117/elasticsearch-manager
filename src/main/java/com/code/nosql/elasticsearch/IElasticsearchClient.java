package com.code.nosql.elasticsearch;

import java.util.List;

/**
 * @Description ES的客户端接口
 * @Author liufei
 * @Date 2020/1/14 14:42
 */
public interface IElasticsearchClient {
    /**
     * 批量写入
     *
     * @param action 单个请求体
     * @return
     */
    void bulk(List action);
    /**
     * 查询
     *
     * @param esql 查询语句
     * @return
     */
    List search(String esql);
    /**
     * 发送get命令
     *
     * @param cmd
     * @return
     */
    Object get(String cmd);

    Object post(String cmd);

    void delete(String esql);

    void update(String esql);
}
