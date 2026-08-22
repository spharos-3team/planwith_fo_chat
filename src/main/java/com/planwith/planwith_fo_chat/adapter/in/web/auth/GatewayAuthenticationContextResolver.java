package com.planwith.planwith_fo_chat.adapter.in.web.auth;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_chat.application.exception.BusinessException;
import com.planwith.planwith_fo_chat.application.exception.ErrorCode;
import com.planwith.planwith_fo_chat.config.GatewayTrustProperties;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class GatewayAuthenticationContextResolver {

	public static final String HEADER_USER_ID = "X-Auth-User-Id";
	public static final String HEADER_ROLES = "X-Auth-Roles";
	public static final String HEADER_SCOPES = "X-Auth-Scopes";
	public static final String HEADER_SESSION_ID = "X-Auth-Session-Id";
	public static final String HEADER_REQUEST_ID = "X-Request-Id";
	public static final String HEADER_GATEWAY_TOKEN = "X-Gateway-Internal-Token";

	private final GatewayTrustProperties properties;

	public GatewayAuthenticationContextResolver(GatewayTrustProperties properties) {
		this.properties = properties;
	}

	public AuthenticatedUser resolveOptional(HttpServletRequest request) {
		String userIdHeader = request.getHeader(HEADER_USER_ID);
		if (!StringUtils.hasText(userIdHeader)) {
			return null;
		}
		UUID userId;
		try {
			userId = UUID.fromString(userIdHeader.trim());
		}
		catch (IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "X-Auth-User-Id 형식이 올바르지 않습니다.");
		}
		return new AuthenticatedUser(
				userId,
				splitCsv(request.getHeader(HEADER_ROLES)),
				splitCsv(request.getHeader(HEADER_SCOPES)),
				blankToNull(request.getHeader(HEADER_SESSION_ID)),
				blankToNull(request.getHeader(HEADER_REQUEST_ID))
		);
	}

	public AuthenticatedUser requireUser() {
		AuthenticatedUser user = AuthenticatedUserContext.get();
		if (user == null || user.userId() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		return user;
	}

	public void requireTrustedGateway(HttpServletRequest request) {
		if (!properties.trustCheckEnabled()) {
			return;
		}
		String presented = request.getHeader(HEADER_GATEWAY_TOKEN);
		String expected = properties.internalToken();
		if (!StringUtils.hasText(expected) || !expected.equals(presented)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "Gateway Trust 검증에 실패했습니다.");
		}
	}

	private List<String> splitCsv(String value) {
		if (!StringUtils.hasText(value)) {
			return List.of();
		}
		return Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
	}

	private String blankToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
