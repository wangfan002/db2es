package com.db2es.core.sync.controller;

import cn.hutool.core.thread.ThreadUtil;
import com.db2es.apiresult.ApiResult;
import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.core.sync.service.SyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Fan.Wang
 * @date 2021/11/18 16:56
 */
@RestController
@Api("索引同步 Sqlserver到ES的数据")
@RequestMapping("sync")
@AllArgsConstructor
public class SyncController {
    private final SyncService syncService;

    @ApiOperation("索引全量同步")
    @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data")
    @PostMapping("rebuild-index")
    public ApiResult rebuildIndex(@RequestParam @NotNull String indexName) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        ThreadUtil.execAsync(() ->syncService.rebuildIndex(indexEnum));
        return ApiResult.success();
    }

    @ApiOperation("索引增量同步")
    @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data")
    @PostMapping("sync-index-by-time")
    public ApiResult syncIndexByTime(@RequestParam @NotNull String indexName) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        ThreadUtil.execAsync(() -> syncService.syncIndexByTime(indexEnum));
        return ApiResult.success();
    }


    @ApiOperation("索引指定id同步")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data"),
            @ApiImplicitParam(name = "ids", value = "待同步的id集合(id之间用,分割)", required = true, dataType = "List", dataTypeClass = List.class, paramType = "query")
    })
    @PostMapping("sync-index-by-id")
    public ApiResult syncIndexById(@RequestParam @NotNull String indexName, @RequestParam @NotNull List<String> ids) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        return syncService.syncIndexById(indexEnum, ids);
    }

    @ApiOperation("索引指定id删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data"),
            @ApiImplicitParam(name = "ids", value = "待删除的id集合(id之间用,分割)", required = true, dataType = "List", dataTypeClass = List.class, paramType = "query")
    })
    @PostMapping("del-index-by-id")
    public ApiResult delIndexById(@RequestParam @NotNull String indexName, @RequestParam @NotNull List<String> ids) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        return syncService.delIndexById(indexEnum, ids);
    }

    @ApiOperation("按顺序重建多个索引")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "indexNames", value = "待重建的index集合(index之间用,分割)", required = true, dataType = "List", dataTypeClass = List.class, paramType = "query")
    })
    @PostMapping("rebuild-multi-index")
    public ApiResult rebuildMultiIndex(@RequestParam @NotNull List<String> indexNames) {
        ThreadUtil.execAsync(() ->syncService.rebuildMultiIndex(indexNames));
        return ApiResult.success();
    }

}