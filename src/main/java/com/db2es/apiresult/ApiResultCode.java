package com.db2es.apiresult;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 响应码
 */
public enum ApiResultCode {
    /**
     * 通用提示语
     */
    FAILURE(0, "请求失败"),
    SUCCESS(1, "请求成功");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;

    ApiResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.name();
    }

}
