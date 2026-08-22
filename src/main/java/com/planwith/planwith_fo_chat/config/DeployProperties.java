package com.planwith.planwith_fo_chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.deploy")
public record DeployProperties(
		String marker
) {
}
