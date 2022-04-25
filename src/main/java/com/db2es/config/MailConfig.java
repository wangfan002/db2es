package com.db2es.config;

import cn.hutool.extra.mail.MailAccount;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fan.Wang
 * @date 2022/1/25 13:35
 * @des 邮件配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailConfig {

    /**
     * 邮件服务器的SMTP地址，可选，默认为smtp.<发件人邮箱后缀>
     */
    private String host;

    /**
     * 邮件服务器的SMTP端口，默认是25端口
     */
    private Integer port;

    /**
     * 发件人（必须正确，否则发送失败）
     */
    private String from;

    /**
     * 用户名，必须要设置成你自己使用邮箱的名称，否则会报错，权限认证失败  535错误
     */
    private String user;

    /**
     * 密码 此处注意，这里是授权码
     */
    private String pass;


    @Bean
    public MailAccount getMailAccount() {
        return new MailAccount()
                .setHost(host)
                .setPort(port)
                .setFrom(from)
                .setUser(user)
                .setPass(pass);
    }
}