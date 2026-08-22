package com.planwith.planwith_fo_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_chat.config.AuthProperties;
import com.planwith.planwith_fo_chat.config.ChatKafkaProperties;
import com.planwith.planwith_fo_chat.config.ChatRedisProperties;
import com.planwith.planwith_fo_chat.config.DeployProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		ChatKafkaProperties.class,
		ChatRedisProperties.class,
		DeployProperties.class
})
public class PlanwithFoChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoChatApplication.class, args);
	}

}
