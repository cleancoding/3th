/*
 * @(#)CloseApiAuthType.java $version 2012. 11. 15
 *
 * Copyright 2010 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.naver.blog.api;

/**
 * CloseApiAuthType class.
 *
 */
public enum CloseApiAuthType {
	OAUTH("OAuth", new DefaultAuthenticationUserIdRule()), // 
	COOKIE("Cookie", new CookieAuthenticationUserIdRule()),
	UNKNOWN("UNKNOWN", new DefaultAuthenticationUserIdRule());

	private String type;
	private AuthenticationUserIdRule authenticationUserIdRule;

	private CloseApiAuthType(String type, AuthenticationUserIdRule authenticationUserIdRule) {
		this.type = type;
		this.authenticationUserIdRule = authenticationUserIdRule;
	}

	public static CloseApiAuthType find(String type) {
		for (CloseApiAuthType each : CloseApiAuthType.values()) {
			if (each.type.equals(type)) {
				return each;
			}
		}
		return CloseApiAuthType.UNKNOWN;
	}

	public AuthenticationUserIdRule getAuthenticationUserIdRule() {
		return authenticationUserIdRule;
	}
}
