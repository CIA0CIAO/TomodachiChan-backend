package com.tomodachi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tomodachi.mapper")
public class TomodachiChanApplication {
    public static void main(String[] args) {
        SpringApplication.run(TomodachiChanApplication.class, args);
    }
}
