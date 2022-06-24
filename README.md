## db2es:1.0
数据库同步ES的Swagger版(脚手架)


## 标签
ElasticSearch7.10 ES 数据库 同步 mysql sqlserver 数据清洗 多线程 大数据量 全量 增量 定时 脚手架


## 优点
* 对大数据量同步性能更出色,基于id或自增列进行多线程分批同步
* 完全由java语言进行开发,数据清洗更为简单
* 基于Sqlserver日志的CDC增量同步
* 支持同步完毕的邮件和钉钉通知
* 同步基于sql,支持mysql,sqlserver等关系型数据库
* 支持多ES集群

## DEMO
* 例:同步大数据量数据  10W以上数据 --> 仿照BigData
* 例:同步小数据量数据  10W以下数据 --> 仿照SmallData



## 接口展示
![](images/img_1.jpg)




## 访问地址
* dev
  * db2es地址:http://localhost:8888/doc.html

## 主要流程图
![](images/img.png)


## 技术架构
springboot2 maven mybatis-plus mysql sqlserver logback ElasticSearch7.10 swagger2

## 联系我
Fan.Wang@bjtu.edu.cn



