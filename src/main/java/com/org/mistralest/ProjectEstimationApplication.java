package com.org.mistralest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class ProjectEstimationApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(ProjectEstimationApplication.class, args);

      /*  ConfigurableApplicationContext ctx =
                SpringApplication.run(ProjectEstimationApplication.class, args);

        ChatClient chat = ctx.getBean(ChatClient.class);

        System.out.println("Sending test prompt...");

        String reply = chat
                .prompt()
                .user("Say: Mistral API is working")
                .call()
                .content();

        System.out.println("======== MISTRAL RESPONSE ========");
        System.out.println(reply);
        System.out.println("==================================");*/
    }
}
