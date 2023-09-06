package com.tomodachi.config;

import com.tomodachi.common.CustomObjectMapper;
import com.tomodachi.controller.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * SpringMVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor) // 添加拦截器
                .addPathPatterns("/**") // 设置拦截器匹配路径
                .excludePathPatterns( // 设置拦截器放行路径
                        "/doc.html", "/webjars/**", // API 文档
                        "/test/**" // 临时测试接口
                );
    }

    /**
     * 配置允许跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 设置允许跨域的路径
                .allowedOriginPatterns("*") // 设置允许跨域请求的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 设置允许的方法
                .allowCredentials(true) // 设置允许证书
                .allowedHeaders("*") // 设置允许的头部信息
                .exposedHeaders("*") // 设置暴露的头部信息
                .maxAge(3600); // 跨域允许时间
    }

    /**
     * 配置消息转换器
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonMessageConverter) {
                jsonMessageConverter.setObjectMapper(new CustomObjectMapper());
                break;
            }
        }
    }
}
