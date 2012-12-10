/*
 * @(#)AuthenticationUserIdFinder.java $version 2012. 11. 15
 *
 * Copyright 2010 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.naver.blog.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.naver.blog.foundation.log.BlogLogFactory;
import com.naver.blog.foundation.zookeeper.OnOffStatusHolder;
import com.naver.blog.foundation.zookeeper.ZookeeperServiceType;

/**
 * AuthenticationUserIdFinder class.
 *
 */
@Component
public class AuthenticationUserIdFinder {
	private static final Log LOG = BlogLogFactory.getLog(AuthenticationUserIdFinder.class);
	private static final String AUTH_TYPE = "AuthType";

	private OnOffStatusHolder onOffStatusHolder;

	@Autowired
	public AuthenticationUserIdFinder(OnOffStatusHolder onOffStatusHolder) {
		this.onOffStatusHolder = onOffStatusHolder;
	}

	public String getUserId(HttpServletRequest request) {

		if (onOffStatusHolder.on(ZookeeperServiceType.APIGW_AUTH_TYPE)) {
			CloseApiAuthType apiAuthType = CloseApiAuthType.find(request.getHeader(AUTH_TYPE));
			return apiAuthType.getAuthenticationUserIdRule().getUserId(request);
		} else {
			return CloseApiAuthType.UNKNOWN.getAuthenticationUserIdRule().getUserId(request);
		}
	}
}
