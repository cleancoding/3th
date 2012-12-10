package com.nbp.nsight.apache.portlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.nbp.nsight.common.base.BasePortlet;
import com.nbp.nsight.common.dto.SearchParam;

/**
 * Portlet implementation class ApacheGroupStatus
 */
public class ApacheStatusPortlet extends BasePortlet {

	@Override
	protected void beforeInclude(RenderRequest request, RenderResponse response) {
		request.setAttribute("listApacheStatusURL", buildURL(response, "listApacheStatus"));
	}
	
	@Override
	protected void afterInclude(RenderRequest request, RenderResponse response) {
		// Check after
	}

	public void listApacheStatus(RenderRequest request, RenderResponse response)
			throws IOException, PortletException, SQLException {
		
		// Check Parameter		
		SearchParam search = parseSearchParam(request);	
		
		// Call Manager

		// Pass to JSP
		request.setAttribute("search", search);		
		include("listApacheStatus.jsp", request, response);
	}
}