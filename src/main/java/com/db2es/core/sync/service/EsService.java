package com.db2es.core.sync.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.db2es.config.EsConfig;
import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.core.sync.exception.IndexSyncException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 操作ES
 */
@Service
@Slf4j
public class EsService {
    private static final int PAGE_SIZE = 1000;

    public boolean checkIndex(String indexName, RestHighLevelClient restHighLevelClient) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public boolean aliasIndex(String tempIndexName, String aliasName, String preIndexName, RestHighLevelClient restHighLevelClient) {
        try {
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            IndicesAliasesRequest.AliasActions add = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD);
            add.alias(aliasName);
            add.index(tempIndexName);
            indicesAliasesRequest.addAliasAction(add);
            if (StringUtils.isNotBlank(preIndexName) && checkIndex(preIndexName, restHighLevelClient)) {
                IndicesAliasesRequest.AliasActions remove = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE);
                remove.alias(aliasName);
                remove.index(preIndexName);
                indicesAliasesRequest.addAliasAction(remove);
            }
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public String getCurrentIndexNameByAlias(String indexName, RestHighLevelClient restHighLevelClient) throws IOException {
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest(indexName);
        GetAliasesResponse getAliasesResponse = restHighLevelClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        Set<String> aliases = getAliasesResponse.getAliases().keySet();
        return CollUtil.getLast(new ArrayList<>(aliases));
    }


    public boolean updateIndexSetting(String indexName, int replicas, int refreshInterval, RestHighLevelClient restHighLevelClient) {
        try {
            UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest();
            updateSettingsRequest.indices(indexName);
            JSONObject settings = new JSONObject();
            settings.set("number_of_replicas", replicas);
            settings.set("refresh_interval", refreshInterval + "s");
            settings.set("index.translog.durability", "async");
            updateSettingsRequest.settings(settings);
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean createIndex(String indexName, String source, RestHighLevelClient restHighLevelClient) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.source(source, XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public void bulkInsert(String indexName, IndexEnum indexEnum, List<Map<String, Object>> list, RestHighLevelClient restHighLevelClient) throws Exception {
        BulkRequest request = new BulkRequest();
        for (Map<String, Object> map : list) {
            request.add(new IndexRequest(indexName).id(map.get(indexEnum.getEsIdField()).toString()).source(JSONUtil.toJsonStr(map), XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            log.error(bulkResponse.buildFailureMessage());
            throw new IndexSyncException("批量写入ES存在异常");
        }
    }

    public void bulkDelete(String indexName, List<String> ids, RestHighLevelClient restHighLevelClient) throws IOException {
        if (CollUtil.isNotEmpty(ids)) {
            BulkRequest request = new BulkRequest();
            for (String id : ids) {
                request.add(new DeleteRequest(indexName, id));
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error(bulkResponse.buildFailureMessage());
            }
        }
    }

    public boolean deleteIndex(String indexName, RestHighLevelClient restHighLevelClient) {
        try {
            AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
            return acknowledgedResponse.isAcknowledged();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public long getDocCount(String indexName, RestHighLevelClient restHighLevelClient) {
        if (indexName == null) {
            return 0;
        }
        CountRequest countRequest = new CountRequest(indexName);
        try {
            CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
            return countResponse.getCount();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }


    public List<String> getIdsByIndexName(IndexEnum indexEnum, RestHighLevelClient restHighLevelClient) {
        List<String> list = CollUtil.newArrayList();
        //构造查询条件
        SearchRequest searchRequest = new SearchRequest(indexEnum.getIndexName());
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询超时时间
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        builder.query(QueryBuilders.matchAllQuery());
        builder.storedField(indexEnum.getEsIdField());
        //设置最多一次能够取出1000笔数据，从第1001笔数据开始，将开启滚动查询  PS:滚动查询也属于这一次查询，只不过因为一次查不完，分多次查
        builder.size(PAGE_SIZE);
        searchRequest.source(builder);
        //将滚动放入
        searchRequest.scroll(scroll);
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("滚动查询失败[{}]", e.getMessage(), e);
        }
        assert searchResponse != null;
        SearchHits hits = searchResponse.getHits();
        //记录要滚动的ID
        String scrollId = searchResponse.getScrollId();
        //滚动查询部分，将从第10001笔数据开始取
        SearchHit[] hitsScroll = hits.getHits();
        collectIds(list, hitsScroll);
        while (ArrayUtil.isNotEmpty(hitsScroll)) {
            //构造滚动查询条件
            SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
            searchScrollRequest.scroll(scroll);
            try {
                //响应必须是上面的响应对象，需要对上一层进行覆盖
                searchResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("滚动查询失败[{}]", e.getMessage(), e);
            }
            scrollId = searchResponse.getScrollId();
            hits = searchResponse.getHits();
            hitsScroll = hits.getHits();
            collectIds(list, hitsScroll);
        }
        //清除滚动，否则影响下次查询
        cleanScroll(restHighLevelClient, scrollId);
        return list;
    }

    private void cleanScroll(RestHighLevelClient restHighLevelClient, String scrollId) {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("滚动查询清除失败[{}]", e.getMessage(), e);
        }
        //清除滚动是否成功
        assert clearScrollResponse != null;
        if (!clearScrollResponse.isSucceeded()) {
            log.error("滚动查询清除失败");
        }
    }

    /**
     * 对结果集的处理
     */
    private void collectIds(List<String> list, SearchHit[] hitsScroll) {
        for (SearchHit hit : hitsScroll) {
            list.add(hit.getId());
        }
    }

    public boolean changeIndexVersion(IndexEnum indexEnum, RestHighLevelClient restHighLevelClient) {
        String indexName = indexEnum.getIndexName();
        PutMappingRequest request = new PutMappingRequest(indexName);
        Map<String, Map<String, String>> jsonMap = new HashMap<>(1);
        Map<String, String> metaMap = new HashMap<>(1);
        metaMap.put("version", indexName + "_" + DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN));
        jsonMap.put("_meta", metaMap);
        request.source(jsonMap);
        AcknowledgedResponse putMappingResponse;
        try {
            putMappingResponse = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return putMappingResponse.isAcknowledged();
    }

    public Set<String> getIndexesByPrefix(String prefix) throws IOException {
        Set<String> indexes = new HashSet<>();
        List<RestHighLevelClient> allRestHighLevelClient = new EsConfig().getAllRestHighLevelClient();
        for (RestHighLevelClient restHighLevelClient : allRestHighLevelClient) {
            GetIndexRequest getIndexRequest = new GetIndexRequest(prefix + "*");
            GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
            indexes = CollUtil.newHashSet(getIndexResponse.getIndices());
        }
        return indexes;
    }
}
