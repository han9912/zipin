package io.github.han9912.zipin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "io.github.han9912.zipin") // 指定扫描的顶级包路径

public class ZipinApplication {

	public static void main(String[] args) {
				SpringApplication.run(ZipinApplication.class, args);
	}

}
