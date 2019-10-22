package com.server.test.http.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * @Author : LeePuvier
 * @CreateTime : 2019/10/21  8:39 PM
 * @ContentUse ：设置服务基础配置
 */

@SpringBootTest(
        classes = com.wecash.rh.broodmother.spawnspider.base.Starter.class,
        properties = {
                "app.id=demo-test",
                "apollo.meta=http://127.0.0.1:8080",
                "env=FAT",
                "-Dserver.port=8081",
                "apollo.bootstrap.enabled=true",
                "apollo.bootstrap.eagerLoad.enabled=true",
                "apollo.bootstrap.namespaces=application",
                "eureka.client.register-with-eureka=false"
        }
)
public class BaseCase extends AbstractTestNGSpringContextTests{
}
