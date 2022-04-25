package com.db2es.util;

import org.junit.jupiter.api.Test;

class DingMessageUtilsTest {

    @Test
    void sendMessage() {
        DingMessageUtils.sendMessage("[big_data]同步同步成功,用时900s.TEST");
    }
}