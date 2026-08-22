package com.planwith.planwith_fo_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.planwith.planwith_fo_chat.adapter.in.web.auth.GatewayTrustInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final GatewayTrustInterceptor gatewayTrustInterceptor;

	public WebMvcConfig(GatewayTrustInterceptor gatewayTrustInterceptor) {
		this.gatewayTrustInterceptor = gatewayTrustInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(gatewayTrustInterceptor);
	}
}
