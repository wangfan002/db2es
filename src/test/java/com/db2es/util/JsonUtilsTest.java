package com.db2es.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Charsets;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

    @Test
    void getJsonStringSource() {
        String fileStr = JSONUtil.parseObj(ResourceUtil.readStr("json/" + "big_data" + ".json", Charsets.UTF_8)).toString();
        System.out.println(fileStr);
    }
}