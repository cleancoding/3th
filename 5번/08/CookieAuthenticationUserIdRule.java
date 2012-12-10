/*
 * @(#)CookieAuthenticationUserIdRule.java $version 2012. 11. 15
 *
 * Copyright 2010 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.naver.blog.api;

import javax.servlet.http.HttpServletRequest;

import com.naver.blog.mylog.bloguser.BlogUser;

/**
 * CookieAuthenticationUserIdRule class.
 *
 */
public class CookieAuthenticationUserIdRule implements AuthenticationUserIdRule {

	@Override
	public String getUserId(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		/*User user = BlogContext.getUserContext().getUser();
		if (user == null) {
			return null;
		}
		return user.getUserId();*/

		return request.getHeader(BlogUser.HEADER_USERID);
	}
}
