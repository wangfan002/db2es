package com.db2es.core.sync.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.db2es.SyncApplication;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = SyncApplication.class)
class EsServiceTest {
    private RestHighLevelClient restHighLevelClient;

    private static final String INDEX_NAME = "my_index";
    private static final String INDEX_TRUE_NAME = "my_index_001";

    @BeforeEach
    void beforeClass() {
        restHighLevelClient = SpringUtil.getBean("lawOtherClient");
    }

    @Order(1)
    @SneakyThrows
    @Test
    void createIndex() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_TRUE_NAME);
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        Assert.isTrue(createIndexResponse.isAcknowledged());
    }

    @Order(2)
    @SneakyThrows
    @Test
    void checkIndex() {
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_TRUE_NAME);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        Assert.isTrue(exists);
    }

    @Order(3)
    @SneakyThrows
    @Test
    void aliasIndex() {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        IndicesAliasesRequest.AliasActions add = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD);
        add.alias(INDEX_NAME);
        add.index(INDEX_TRUE_NAME);
        indicesAliasesRequest.addAliasAction(add);
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        Assert.isTrue(acknowledgedResponse.isAcknowledged());
    }

    @Order(4)
    @SneakyThrows
    @Test
    void changeIndexVersion() {
        PutMappingRequest request = new PutMappingRequest(INDEX_NAME);
        Map<String, Map<String, String>> jsonMap = new HashMap<>();
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("version", INDEX_NAME + "_" + DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN));
        jsonMap.put("_meta", metaMap);
        request.source(jsonMap);
        AcknowledgedResponse putMappingResponse = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        boolean acknowledged = putMappingResponse.isAcknowledged();
        Assert.isTrue(acknowledged);
    }

    @Order(5)
    @SneakyThrows
    @Test
    void updateIndexSetting() {
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest();
        updateSettingsRequest.indices(INDEX_NAME);
        JSONObject settings = new JSONObject();
        settings.set("number_of_replicas", 0);
        settings.set("refresh_interval", 1 + "s");
        updateSettingsRequest.settings(settings);
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
        Assert.isTrue(acknowledgedResponse.isAcknowledged());
    }

    @Order(6)
    @SneakyThrows
    @Test
    void bulkInsert() {
        BulkRequest request = new BulkRequest();
        List<Map<String, Object>> list = CollUtil.newArrayList();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", "1");
        map1.put("title", "这是一个标题");
        list.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", "2");
        map2.put("title", "这是另一个标题");
        list.add(map2);
        for (Map<String, Object> map : list) {
            IndexRequest id = new IndexRequest(INDEX_NAME).id(map.get("id").toString());
            IndexRequest source = id.source(JSONUtil.toJsonStr(map), XContentType.JSON);
            request.add(source);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        Assert.isFalse(bulkResponse.hasFailures());
    }

    @Order(7)
    @SneakyThrows
    @Test
    void getDocCount() {
        ThreadUtil.sleep(1000);
        CountRequest countRequest = new CountRequest(INDEX_TRUE_NAME);
        CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        Assert.isTrue(countResponse.getCount() == 2);
    }

    @Order(8)
    @SneakyThrows
    @Test
    void bulkDelete() {
        List<String> ids = ListUtil.of("1", "2");
        BulkRequest request = new BulkRequest();
        for (String id : ids) {
            request.add(new DeleteRequest(INDEX_NAME, id));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        Assert.isFalse(bulkResponse.hasFailures());
    }

    @Order(9)
    @SneakyThrows
    @Test
    void getCurrentIndexNameByAlias() {
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest(INDEX_NAME);
        GetAliasesResponse getAliasesResponse = restHighLevelClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        Set<String> aliases = getAliasesResponse.getAliases().keySet();
        Assert.isTrue(CollUtil.getLast(new ArrayList<>(aliases)).equals(INDEX_TRUE_NAME));
    }

    @Order(10)
    @SneakyThrows
    @Test
    void deleteIndex() {
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(new DeleteIndexRequest(INDEX_TRUE_NAME), RequestOptions.DEFAULT);
        Assert.isTrue(acknowledgedResponse.isAcknowledged());
    }

    @Order(11)
    @SneakyThrows
    @Test
    void GetIndexesByPrefix() {
        GetIndexRequest getIndexRequest = new GetIndexRequest("*");
        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        String[] indices = getIndexResponse.getIndices();
        List<String> asList = Arrays.asList(indices);
        Assert.notEmpty(asList);
    }

}