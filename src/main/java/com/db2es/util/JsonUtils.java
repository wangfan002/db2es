package com.db2es.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Charsets;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des json工具类
 */
public class JsonUtils {


    /**
     * 根据文件名获取json字符串
     *
     * @param fileName json文件名
     * @return String
     */
    public static String getJsonStringSource(String fileName) {
        try {
            return StrUtil.toString(JSONUtil.parseObj(ResourceUtil.readStr("json/" + fileName + ".json", Charsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

}
