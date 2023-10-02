package com.tomodachi.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置
 */
@Configuration
public class OSSConfig {
    @Value("${tomodachi.aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${tomodachi.aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${tomodachi.aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 创建 OSS 实例
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
