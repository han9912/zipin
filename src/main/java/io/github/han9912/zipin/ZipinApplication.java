package io.github.han9912.zipin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = "io.github.han9912.zipin") // 指定扫描的顶级包路径

public class ZipinApplication {

	public static void main(String[] args) {
		ApplicationContext context =
				SpringApplication.run(ZipinApplication.class, args);

		// 打印所有加载的 Bean
		// String[] beanNames = context.getBeanDefinitionNames();
		// Arrays.stream(beanNames).sorted().forEach(System.out::println);

	}

}
