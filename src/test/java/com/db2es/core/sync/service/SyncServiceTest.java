package com.db2es.core.sync.service;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.db2es.SyncApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SyncApplication.class)
class SyncServiceTest {

    @Value("${mail.to}")
    private String mailSendTo;

    @Value("${spring.profiles.active}")
    private String activeEnv;

    @Test
    void sendMail() {
        MailAccount mailAccount = SpringUtil.getBean("getMailAccount");
        MailUtil.send(mailAccount, mailSendTo, "[" + activeEnv + "环境]" + "a" + "索引同步异常", "测试内容", false);
    }

}