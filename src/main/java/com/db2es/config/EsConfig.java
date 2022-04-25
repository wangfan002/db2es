package com.db2es.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fan.Wang
 * @date 2021/2/25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class EsConfig {

    private String[] cluster1Hosts;
    private String cluster1Username;
    private String cluster1Password;

    private String[] cluster2Hosts;
    private String cluster2Username;
    private String cluster2Password;

    private int maxConnTotal;
    private int maxConnPerRoute;

    @Bean(name = "cluster1Client")
    public RestHighLevelClient cluster1Client() {
        return getRestHighLevelClient(cluster1Username, cluster1Password, cluster1Hosts);
    }

    @Bean(name = "cluster2Client")
    public RestHighLevelClient cluster2Client() {
        return getRestHighLevelClient(cluster2Username, cluster2Password, cluster2Hosts);
    }



    /**
     * 获取EsClient
     *
     * @param username ES集群用户名
     * @param password ES集群密码
     * @param hosts    ES集群地址
     * @return org.elasticsearch.client.RestHighLevelClient
     * @dateTime 2022/3/29 17:20
     */
    private RestHighLevelClient getRestHighLevelClient(String username, String password, String[] hosts) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        RestClientBuilder builder = RestClient.builder(Arrays.stream(hosts).map(HttpHost::create).toArray(HttpHost[]::new))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(builder);
    }

    public List<RestHighLevelClient> getAllRestHighLevelClient(){
        List<RestHighLevelClient> clientList = new ArrayList<>();
        clientList.add(SpringUtil.getBean("cluster1Client"));
        clientList.add(SpringUtil.getBean("cluster2Client"));
        return clientList;
    }
}
