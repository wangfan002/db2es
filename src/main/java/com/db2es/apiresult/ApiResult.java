package com.db2es.apiresult;

import java.util.HashMap;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 响应结果
 */
public class ApiResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static ApiResult success() {
        ApiResult result = new ApiResult();
        result.setResultCode(ApiResultCode.SUCCESS);
        return result;
    }

    public static ApiResult success(Object resultData) {
        ApiResult result = new ApiResult();
        result.setResultCode(ApiResultCode.SUCCESS);
        result.setResult(resultData);
        return result;
    }

    public static ApiResult failure() {
        ApiResult result = new ApiResult();
        result.setResultCode(ApiResultCode.FAILURE);
        return result;
    }

    public static ApiResult failure(Object resultData) {
        ApiResult result = new ApiResult();
        result.setResultCode(ApiResultCode.FAILURE);
        result.setResult(resultData);
        return result;
    }

    public void setResultCode(ApiResultCode code) {
        this.put("code", code.code());
        this.put("msg", code.message());
    }

    public boolean isSuccess() {
        return this.get("code") == ApiResultCode.SUCCESS.code();
    }


    public String getMsg() {
        return this.get("msg").toString();
    }

    public void setMsg(String msg) {
        this.put("msg", msg);
    }

    public void setResult(Object result) {
        this.put("result", result);
    }
}
