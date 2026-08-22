package com.planwith.planwith_fo_chat.adapter.in.web.auth;

public final class AuthenticatedUserContext {

	private static final ThreadLocal<AuthenticatedUser> HOLDER = new ThreadLocal<>();

	private AuthenticatedUserContext() {
	}

	public static void set(AuthenticatedUser user) {
		HOLDER.set(user);
	}

	public static AuthenticatedUser get() {
		return HOLDER.get();
	}

	public static void clear() {
		HOLDER.remove();
	}
}
