package com.db2es.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2022/3/25 13:35
 * @des 钉钉消息配置
 */
public class DingMessageUtils {

    /**
     *
     * @dateTime 2022/3/25 9:53
     * @param msg 消息内容
     */
    public static void sendMessage(String msg) {
        //钉钉机器人地址（配置机器人的webhook）
        String dingUrl = "https://oapi.dingtalk.com/robot/send?access_token=你自己的token";
        //通知具体人的手机号码列表
        List<String> mobileList = Lists.newArrayList();
        //钉钉机器人消息内容
        String content = StrUtil.format("[db2es]{}可能需要签名!",msg);
        //组装请求内容
        String reqStr = buildReqStr(content, mobileList);
        //推送消息（http请求）
        HttpUtil.post(dingUrl, reqStr);
    }

    /**
     * 组装请求报文
     *
     * @param content 消息内容
     * @return String
     */
    private static String buildReqStr(String content, List<String> mobileList) {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", false);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);
        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);
        return JSONUtil.toJsonStr(reqMap);
    }
}
