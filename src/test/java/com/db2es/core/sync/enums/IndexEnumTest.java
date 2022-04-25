package com.db2es.core.sync.enums;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;

class IndexEnumTest {

    @Test
    void getByName() {
        String indexName = "big_data";
        for (IndexEnum indexEnum : IndexEnum.values()) {
            if (indexName.equals(indexEnum.indexName)) {
                Assert.state(true);
                return;
            }
        }
        Assert.state(false);
    }
}