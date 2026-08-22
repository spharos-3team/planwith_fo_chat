package com.planwith.planwith_fo_chat.adapter.in.web.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.planwith.planwith_fo_chat.config.GatewayTrustProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayTrustInterceptor implements HandlerInterceptor {

	private final GatewayTrustProperties properties;
	private final GatewayAuthenticationContextResolver resolver;

	public GatewayTrustInterceptor(
			GatewayTrustProperties properties,
			GatewayAuthenticationContextResolver resolver
	) {
		this.properties = properties;
		this.resolver = resolver;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		AuthenticatedUserContext.clear();
		if (properties.trustCheckEnabled() && !isExcluded(request.getRequestURI())) {
			resolver.requireTrustedGateway(request);
		}
		AuthenticatedUser user = resolver.resolveOptional(request);
		if (user != null) {
			AuthenticatedUserContext.set(user);
		}
		return true;
	}

	@Override
	public void afterCompletion(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception ex
	) {
		AuthenticatedUserContext.clear();
	}

	private boolean isExcluded(String uri) {
		return uri.equals("/oauth2/jwks")
				|| uri.equals("/actuator/health")
				|| uri.equals("/actuator/health/liveness")
				|| uri.equals("/actuator/health/readiness");
	}
}
