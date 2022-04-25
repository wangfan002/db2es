package com.db2es.core.info.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.db2es.core.info.service.IndexCycleInfoService;
import com.db2es.core.info.entity.IndexCycleInfo;
import com.db2es.core.info.mapper.IndexCycleInfoMapper;
import org.springframework.stereotype.Service;

/**
 * 记录索引元信息(IndexCycleInfo)表服务实现类
 *
 * @author Fan.Wang
 * @since 2021-12-09 09:58:22
 */
@Service("indexCycleInfoService")
@DS("mysql")
public class IndexCycleInfoServiceImpl extends ServiceImpl<IndexCycleInfoMapper, IndexCycleInfo> implements IndexCycleInfoService {

}

