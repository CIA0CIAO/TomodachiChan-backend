package com.tomodachi.config;

import com.tomodachi.common.role.Role;
import com.tomodachi.common.role.RoleCheck;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Swagger 配置
 */
@Configuration
@Profile({"dev", "test"})
public class SwaggerConfig {
    /**
     * 配置 API 文档信息
     */
    @Bean
    public OpenAPI ApiInfo() {
        return new OpenAPI().info(new Info().title("API 接口文档")
                                            .description("API 接口文档")
                                            .contact(new Contact().name("淡笑莫言"))
                                            .version("1.0"));
    }

    /**
     * 配置管理员权限的 API 分组
     */
    @Bean
    public GroupedOpenApi adminGroup() {
        return GroupedOpenApi.builder()
                             .group("管理员权限")
                             .pathsToMatch("/**")
                             .addOperationCustomizer((operation, handlerMethod) -> {
                                 RoleCheck roleCheck = handlerMethod.getMethodAnnotation(RoleCheck.class);
                                 return roleCheck != null && roleCheck.value() == Role.ADMIN ? operation : null;
                             })
                             .build();
    }

    /**
     * 配置用户权限的 API 分组
     */
    @Bean
    public GroupedOpenApi userGroup() {
        return GroupedOpenApi.builder()
                             .group("用户权限")
                             .pathsToMatch("/**")
                             .addOperationCustomizer((operation, handlerMethod) -> {
                                 RoleCheck roleCheck = handlerMethod.getMethodAnnotation(RoleCheck.class);
                                 return roleCheck != null && roleCheck.value() == Role.USER ? operation : null;
                             })
                             .build();
    }

    /**
     * 配置游客权限的 API 分组
     */
    @Bean
    public GroupedOpenApi guestGroup() {
        return GroupedOpenApi.builder()
                             .group("游客权限")
                             .pathsToMatch("/**")
                             .addOperationCustomizer((operation, handlerMethod) -> {
                                 RoleCheck roleCheck = handlerMethod.getMethodAnnotation(RoleCheck.class);
                                 return roleCheck == null || roleCheck.value() == Role.GUEST ? operation : null;
                             })
                             .build();
    }
}
