package com.jewelry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源配置（图片访问）
 * 同时支持 classpath 和文件系统，解决运行时上传图片无法访问的问题
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 文件系统路径（运行时上传的图片）
        String uploadPath = new File(System.getProperty("user.dir"),
                "src/main/resources/static/images/").getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .addResourceLocations("classpath:/static/images/");
    }
}
