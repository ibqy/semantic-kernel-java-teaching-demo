package com.xb.semantickernel;

/**
 * SemanticKernelTeachingDemoApplication - Semantic Kernel 教学演示项目启动入口
 *
 * 这是整个教学演示项目的 Spring Boot 启动类。
 * 它负责引导自动配置、组件扫描和嵌入式 Tomcat 的启动，
 * 是理解 Spring Boot 应用生命周期的第一个切入点。
 *
 * @author ibqy
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SemanticKernelTeachingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemanticKernelTeachingDemoApplication.class, args);
    }
}