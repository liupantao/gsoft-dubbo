package com.gsoft.dubbo.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ "classpath:dubbo/applicationContext-dubbo.xml" })
public class DubboConfig {

}