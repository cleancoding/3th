package com.nbp.nsight.apache.portlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.nbp.nsight.common.base.BasePortlet;

/**
 * Portlet implementation class ApacheProcessPerformance
 */
public class ApacheProcessPerformancePortlet extends BasePortlet {

	@Override
	protected void beforeInclude(RenderRequest request, RenderResponse response) {
		request.setAttribute("listApacheProcessPerformanceURL", buildURL(response, "listApacheProcessPerformance"));
	}
	
	@Override
	protected void afterInclude(RenderRequest request, RenderResponse response) {
		// Check after		
	}

	public void listApacheProcessPerformance(RenderRequest request, RenderResponse response)
			throws IOException, PortletException, SQLException {
		// Check Parameter

		// Call Manager
	
		include("listApacheProcessPerformance.jsp", request, response);
	}
}