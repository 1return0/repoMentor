package com.lsr.repomentor;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lsr.repomentor.mapper")
public class RepoMentorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoMentorApplication.class, args);
    }

}
