package com.example.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration  // 标记这是一个配置类，Spring 启动时会加载它
public class CorsConfig {

    @Bean  // 把这个方法返回的对象注册为 Spring 的一个 Bean（组件）
    public CorsFilter corsFilter() {
        // 创建跨域配置源对象:存放跨域规则
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //创建跨域配置对象:定义跨域的具体规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //corsConfiguration.addAllowedOrigin("http://localhost:5173");  // Vue 默认端口
        corsConfiguration.addAllowedOrigin("*"); // 1 设置访问源地址,允许所有来源
        corsConfiguration.addAllowedHeader("*"); // 2 设置访问源请求头
        corsConfiguration.addAllowedMethod("*"); // 3 设置访问源请求方法
        source.registerCorsConfiguration("/**", corsConfiguration); // 4 对接口配置跨域设置
        return new CorsFilter(source); //创建并返回 CorsFilter
    }
}