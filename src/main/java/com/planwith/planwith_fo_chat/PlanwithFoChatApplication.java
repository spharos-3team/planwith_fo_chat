package com.planwith.planwith_fo_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_chat.config.AuthProperties;
import com.planwith.planwith_fo_chat.config.ChatKafkaProperties;
import com.planwith.planwith_fo_chat.config.ChatRedisProperties;
import com.planwith.planwith_fo_chat.config.DeployProperties;
import com.planwith.planwith_fo_chat.config.GatewayTrustProperties;
import com.planwith.planwith_fo_chat.config.LocalDotenvLoader;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		ChatKafkaProperties.class,
		ChatRedisProperties.class,
		DeployProperties.class,
		GatewayTrustProperties.class
})
public class PlanwithFoChatApplication {

	public static void main(String[] args) {
		LocalDotenvLoader.load("planwith_fo_chat");
		SpringApplication.run(PlanwithFoChatApplication.class, args);
	}

}
