package com.db2es.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Fan.Wang
 * @date 2021/11/23 11:43
 * @des 同步数据的公共清洗方法
 */
@SuppressWarnings("unused")
@Component
public class DataCleanUtils {
    private static final String CH = "1";
    private static final String ENG = "0";


    /**
     * 数据按标识符进行分割为数组
     *
     * @param map       数据map
     * @param key       待处理字段
     * @param separator 标识符
     * @dateTime 2021/11/23 11:49
     */
    public static void strToArr(Map<String, Object> map, String key, String separator) {
        map.put(key, StrUtil.split(StrUtil.toString(map.get(key)), separator));
    }

    /**
     * 数据按标识符进行分割为数组并存入新字段
     *
     * @param map       数据map
     * @param key       待处理字段
     * @param separator 标识符
     * @dateTime 2021/11/23 11:49
     */
    public static void strToArrForNewFiled(Map<String, Object> map, String key, String separator, String target) {
        map.put(target, StrUtil.split(StrUtil.toString(map.get(key)), separator));
    }

    /**
     * 数据按标识符进行分割保留第一个切割值和原值
     * 03/030900030020 --> 03,03/030900030020
     *
     * @param map       数据map
     * @param key       待处理字段
     * @param separator 标识符
     * @dateTime 2021/11/23 11:49
     */
    public static void strToOneExpandArr(Map<String, Object> map, String key, String separator) {
        String strValue = StrUtil.toString(map.get(key));
        if (StrUtil.contains(strValue, separator)) {
            String before = StrUtil.subBefore(strValue, separator, false);
            map.put(key, ListUtil.of(before, strValue));
        }
    }

    /**
     * 数据先按one进行切分,在按two进行分段切分
     * 03;03/030900030020;12;12/030900180010/030900180010020; --> 12, 03, 12/030900180010/030900180010020, 03/030900030020, 12/030900180010
     *
     * @param map 数据map
     * @param key 待处理字段
     * @dateTime 2021/11/23 11:49
     */
    public static void strToBigMoreExpandArr(Map<String, Object> map, String key, String separatorOne, String separatorTwo) {
        String strValue = StrUtil.toString(map.get(key));
        Set<String> set = new HashSet<>();
        List<String> split = StrUtil.split(strValue, separatorOne, true, true);
        for (String s : split) {
            set.add(s);
            set.add(StrUtil.subBefore(s, separatorTwo, false));
            set.add(StrUtil.subBefore(s, separatorTwo, true));
        }
        map.put(key, set);
    }


    /**
     * 从日期中截取年
     *
     * @param map       数据map
     * @param key       日期字段
     * @param targetKey 截取保留年的字段
     * @author Fan.Wang
     * @dateTime 2021/12/14 15:04
     */
    public static void addYearCol(Map<String, Object> map, String key, String targetKey) {
        Object date = map.get(key);
        if (ObjectUtil.isNotNull(date)) {
            map.put(targetKey, StrUtil.sub(date.toString(), 0, 4));
        }
    }

    /**
     * 增加语言标识lang字段
     *
     * @param map       数据map
     * @param key       基准字段
     * @param targetKey lang字段
     * @author Fan.Wang
     * @dateTime 2021/12/14 15:07
     */
    public static void addLangFlag(Map<String, Object> map, String chFlag, String key, String targetKey) {
        String lang = String.valueOf(map.get(key));
        if (chFlag.equals(lang)) {
            map.put(targetKey, CH);
        } else {
            map.put(targetKey, ENG);
        }
    }


    /**
     * 判断key对应的日期是否为空 为空则添加默认日期1900.01.01
     *
     * @param map 数据map
     * @param key 待处理原字段
     * @author Fan.Wang
     * @dateTime 2022/2/22 16:41
     */
    public static void addDefaultDateForEmpty(Map<String, Object> map, String key) {
        String value = String.valueOf(map.get(key));
        if (StrUtil.isEmpty(value)) {
            map.put(key, "1900.01.01");
        }
    }


    /**
     * 去除html的相关标签
     *
     * @param map 数据map
     * @author Fan.Wang
     * @dateTime 2022/3/01 16:41
     */
    public static void stripTag(Map<String, Object> map, String key) {
        String body = String.valueOf(map.get(key));
        if (StrUtil.isNotEmpty(body)) {
            map.put(key, HtmlUtil.cleanHtmlTag(body));
        }
    }

    /**
     * 符号替换
     *
     * @param map          数据map
     * @param key          待处理字段
     * @param sourceSymbol 想要替换的符号
     * @param targetSymbol 替换为这个符号
     * @author Fan.Wang
     * @dateTime 2022/3/7 9:44
     */
    public static void symbolReplace(Map<String, Object> map, String key, String sourceSymbol, String targetSymbol) {
        String value = String.valueOf(map.get(key));
        if (StrUtil.isNotEmpty(value)) {
            map.put(key, value.replace(sourceSymbol, targetSymbol));
        }
    }

    /**
     * 类型转为String
     *
     * @param map 数据map
     * @param key 待处理字段
     * @author Fan.Wang
     * @dateTime 2022/3/7 10:12
     */
    public static void toString(Map<String, Object> map, String key) {
        String value = String.valueOf(map.get(key));
        if (StrUtil.isNotEmpty(value)) {
            map.put(key, value);
        }
    }

    /**
     * copy字段的值赋值给其他字段
     *
     * @param map       数据map
     * @param sourceKey 待复制字段
     * @param targetKey 待粘贴字段
     * @author Fan.Wang
     * @dateTime 2022/3/7 10:12
     */
    public static void copyTo(Map<String, Object> map, String sourceKey, String targetKey) {
        String value = String.valueOf(map.get(sourceKey));
        if (StrUtil.isNotEmpty(value)) {
            map.put(targetKey, value);
        }
    }

    /**
     * 增加文本长度字段
     *
     * @param map    数据map
     * @param key    待处理字段
     * @param target 填充目标
     * @author Fan.Wang
     * @dateTime 2022/3/7 10:12
     */
    public static void addDocLength(Map<String, Object> map, String key, String target) {
        String value = String.valueOf(map.get(key));
        if (StrUtil.isNotEmpty(value)) {
            map.put(target, value.length());
        }
    }

    /**
     * 按某符号拼接多个字段的值
     *
     * @param map 数据map
     * @param keys 字段名
     * @param connector 连接符
     * @param target 拼接后放的位置
     * @dateTime 2022/3/15 13:40
     */
    public static void joinValue(Map<String, Object> map, List<String> keys, String connector, String target) {
        StrBuilder strBuilder = StrUtil.strBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = String.valueOf(map.get(key));
            if (StrUtil.isNotEmpty(value)) {
                StrBuilder tempBuilder = strBuilder.append(value);
                if (i != keys.size() - 1) {
                    tempBuilder.append(connector);
                }
            }
        }
        map.put(target, strBuilder.toString());
    }

    /**
     * 给空值设置 ""
     *
     * @param map 数据map
     * @dateTime 2022/3/15 13:40
     */
    public static void castNull(Map<String, Object> map, String key) {
        if (ObjectUtil.isNull(map.get(key))) {
            map.put(key, "");
        }
    }
}