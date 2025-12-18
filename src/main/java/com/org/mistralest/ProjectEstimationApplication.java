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


//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class ProjectEstimationApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    //    SpringApplication.run(ProjectEstimationApplication.class, args);

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
        List<String> names = new ArrayList<String>();
        names.add("Ajeet");
        names.add("Negan");
        names.add("Aditya");
        names.add("Steve");
        names.add("Ajeet");
        names.add("Steve");
 /// count strings with length less than 6
        // asending order
        long count = names.stream().filter(str->str.length() < 6).count();
        System.out.println("count strings with length less than 6:"+count);
        Map<String,Long> result = names.stream()
                .collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        result.forEach((key, value) ->
                System.out.println(key + " kkk occurs " + value + " times"));

        String data = "aaDfgGJjjGdduuAAaaGGGJ";
        Map<Character,Long> charCount = data.chars() //IntStream of [65, 106, 101, 101, 116] - they are ASCII/Unicode integer values.
                .mapToObj(ch->(char)ch) //Converts each int means ASCII to its actual char
                .collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        charCount.forEach((key,value) -> System.out.println(key+"  - "+value));

        Map<String,Integer> cnt = names.stream()
                .collect(Collectors.toMap(Function.identity(),String::length,  (oldValue, newValue) -> oldValue));
        cnt.forEach((key, value) ->
                System.out.println(key + " character occurs " + value + " times"));

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> sum = numbers.stream().filter(i->i %2 == 0 ).collect(Collectors.toList());
        System.out.println("--");
        sum.forEach(System.out::println);
        Integer sumInt = numbers.stream().filter(i->i %2 == 0 ).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
        System.out.println("****");
        System.out.println(sumInt);

        List<Integer> mun = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int sumOfSquares = IntStream.range(0, numbers.size()).map(numbers::get).sum();
        System.out.println(sumOfSquares);
    }
}
