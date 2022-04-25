package com.db2es.core.sync.exception;

/**
 * @author Fan.Wang
 * @date 2021/1/27 14:58
 * @des 索引失败异常
 */
public class IndexSyncException extends RuntimeException {

    public IndexSyncException(String message) {
        super(message);
    }
}