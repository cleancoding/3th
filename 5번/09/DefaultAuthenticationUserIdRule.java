/*
 * @(#)OAuthAuthenticationUserIdRule.java $version 2012. 11. 15
 *
 * Copyright 2010 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.naver.blog.api;

import javax.servlet.http.HttpServletRequest;

/**
 * OAuthAuthenticationUserIdRule class.
 *
 */
public class DefaultAuthenticationUserIdRule implements AuthenticationUserIdRule {
	private static final String USER_ID = "userId";

	@Override
	public String getUserId(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		return request.getHeader(USER_ID);
	}

}
