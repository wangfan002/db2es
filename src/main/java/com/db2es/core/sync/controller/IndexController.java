package com.db2es.core.sync.controller;

import com.db2es.apiresult.ApiResult;
import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.core.sync.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @author Fan.Wang
 * @date 2022/1/12 15:02
 */
@RestController
@Api("索引操作类")
@RequestMapping("index")
@AllArgsConstructor
public class IndexController {

    private final IndexService indexService;

    @ApiOperation("查看所有索引别名")
    @GetMapping("select-all-index-name")
    public ApiResult selectAllIndexName() {
        return ApiResult.success(IndexEnum.getAllIndexName());
    }

    @ApiOperation("查看ES或DB的差异id")
    @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data")
    @GetMapping("check-differ-ids")
    public ApiResult checkDifferIds(@RequestParam @NotNull String indexName) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        return ApiResult.success(indexService.checkDifferIdsByIndexName(indexEnum));
    }

    @ApiOperation("根据索引trueName删除索引")
    @ApiImplicitParam(name = "indexTrueName", value = "索引真实名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query")
    @PostMapping("delete-index-by-true-name")
    public ApiResult deleteIndexByTrueName(@RequestParam @NotNull String indexTrueName) {
        String indexName = indexTrueName.substring(0, indexTrueName.lastIndexOf("_"));
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        return indexService.deleteIndexByName(indexEnum, indexTrueName);
    }

    @ApiOperation("设置索引别名关联")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "indexName", value = "索引名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big_data"),
            @ApiImplicitParam(name = "indexTrueName", value = "待关联的索引真实名称", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query"),
    })
    @PostMapping("alias-index")
    public ApiResult aliasIndex(@RequestParam @NotNull String indexName, @RequestParam @NotNull String indexTrueName) {
        IndexEnum indexEnum = IndexEnum.getByName(indexName);
        if (indexEnum == null) {
            return ApiResult.failure("索引:[ " + indexName + "]不存在");
        }
        return indexService.aliasIndex(indexEnum, indexTrueName);
    }

    @ApiOperation("根据前缀删除索引")
    @ApiImplicitParam(name = "prefix", value = "索引前缀", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query", example = "big")
    @GetMapping("delete-index-by-prefix")
    public ApiResult deleteIndexByPrefix(@RequestParam @NotNull String prefix) {
        return indexService.deleteIndexByPrefix(prefix);
    }

    @ApiOperation("查看哪些索引没用做")
    @GetMapping("check-index-miss")
    public ApiResult checkIndexMiss() {
        return indexService.checkIndexMiss();
    }
}