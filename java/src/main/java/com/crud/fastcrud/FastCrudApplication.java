package com.crud.fastcrud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.crud.fastcrud.mapper")
public class FastCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastCrudApplication.class, args);
    }

}
