package com.org.mistralest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class ProjectEstimationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectEstimationApplication.class, args);

    }
}
