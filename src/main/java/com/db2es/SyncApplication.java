package com.db2es;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Fan.Wang
 * @date 2021-11-02 14:37:07
 * @des 启动类
 */
@MapperScan("com.db2es.**.mapper")
@SpringBootApplication
public class SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }

}
