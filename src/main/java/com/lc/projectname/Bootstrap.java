package com.lc.projectname;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: lucheng
 * @data: 2021/11/27 17:17
 * @version: 1.0
 */
@SpringBootApplication
@MapperScan("mapper/*.xml")
public class Bootstrap {
    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
