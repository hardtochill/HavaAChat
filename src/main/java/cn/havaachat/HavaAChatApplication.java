package cn.havaachat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@MapperScan(basePackages = {"cn.havaachat.mappers"})
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication(scanBasePackages = ("cn.havaachat"))
public class HavaAChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(HavaAChatApplication.class,args);
    }
}
