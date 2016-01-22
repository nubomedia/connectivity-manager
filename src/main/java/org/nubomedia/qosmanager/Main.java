package org.nubomedia.qosmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by maa on 02.12.15.
 */
@SpringBootApplication
@EnableScheduling
public class Main {

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

}
