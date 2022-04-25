package com.db2es.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Fan.Wang
 * @date 2022/1/7 11:49
 * @des 集合工具类
 */
public class ListUtils {
    /**
     * 借助Map来获取esIdList,、dbIdList的不同元素集合
     *
     * @param list 集合A
     * @param otherList 集合B
     * @return list<String>
     */
    public static List<String> getDifferListByMap(List<String> list, List<String> otherList) {
        Map<String, String> tempMap = otherList.parallelStream().collect(Collectors.toMap(Function.identity(), Function.identity(), (oldData, newData) -> newData));
        return list.parallelStream().filter(str-> !tempMap.containsKey(str)).collect(Collectors.toList());
    }
}  