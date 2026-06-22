package com.example.mentorisebackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MentoriseBackendApplication {

    public static void main(String[] args) {
        /// //////FOR LOCAL ENV FILE/////
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()  // שלא יקרוס אם אין .env
                .load();

        // מכניס לערכת ה-System properties כדי ש-Spring יוכל לקרוא את זה כמו ENV
        dotenv.entries().forEach(e -> {
            if (System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        });
        /// /////////
        SpringApplication.run(MentoriseBackendApplication.class, args);
    }

}
