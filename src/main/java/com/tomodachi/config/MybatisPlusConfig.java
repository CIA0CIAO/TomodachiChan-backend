package com.tomodachi.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.tomodachi.constant.SystemConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlus 配置
 */
@Configuration
public class MybatisPlusConfig {
    /**
     * 配置拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 创建分页拦截器
        PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor();
        // 设置单页数量限制
        innerInterceptor.setMaxLimit(Long.valueOf(SystemConstant.MAX_PAGE_SIZE));
        // 添加分页拦截器
        interceptor.addInnerInterceptor(innerInterceptor);
        return interceptor;
    }
}
