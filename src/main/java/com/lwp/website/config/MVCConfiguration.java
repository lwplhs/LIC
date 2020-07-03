package com.lwp.website.config;

import com.lwp.website.interceptor.BaseInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MVCConfiguration implements WebMvcConfigurer {

    @Resource
    private BaseInterceptor baseInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/").setViewName("forward:/index.html");

        //registry.addViewController("/chat").setViewName("forward:/chat_room.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseInterceptor).excludePathPatterns("/static/**").excludePathPatterns("/media/**");
        /*registry.addInterceptor(pageInterceptor).excludePathPatterns("/static/**").excludePathPatterns("/media/**");*/
    }

    /**
     * 添加静态资源文件 ，外部可以直接访问地址
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
/*        registry.addResourceHandler("/media/**").addResourceLocations(TaleUtils.getUEditorPath()+"/media/");
        registry.addResourceHandler("/upload/**").addResourceLocations(TaleUtils.getUploadFilePath()+"upload/");*/
    }
}
