package com.nbp.nsight.apache.portlet;


import java.io.IOException;
import java.sql.SQLException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.nbp.nsight.common.base.BasePortlet;
import com.nbp.nsight.common.dto.SearchParam;

/**
 * Apache모니터링, Tomcat모니터링을 하나의 클래스로 병행하면서 수행한다.
 * getNamespace()나오는 포틀릿 이름에 따라  applName 변수에 구분자를 넣어준다. 
 */
public class ApacheMonitoringRegisterPortlet extends BasePortlet {

	
	protected void beforeInclude(RenderRequest request, RenderResponse response) {
		request.setAttribute("listApacheMonitoringRegisterURL", buildURL(response, "listApacheMonitoringRegister"));
	}
	
	@Override
	protected void afterInclude(RenderRequest request, RenderResponse response) {
	}
	
	public void viewApacheMonitoringRegister(RenderRequest request, RenderResponse response)
			throws IOException, PortletException, SQLException {
		
		
		/**
		 * getNamespace()를 이용하여 모니터링 등록 대상을 구분한다.
		 */
		String applTpCd="";
		if(response.getNamespace().startsWith("_TomcatMonitoringRegister")==true){
			applTpCd = "APPTP_TOMCAT";
		} else if(response.getNamespace().startsWith("_ApacheMonitoringRegister")==true){
			applTpCd = "APPTP_APACHE";
		} else if(response.getNamespace().startsWith("_NginxMonitoringRegister")==true){
			applTpCd = "APPTP_NGINX";
		}
		
		
		// Check Parameter		
		SearchParam search = parseSearchParam(request);
		
		// Pass to JSP			
		request.setAttribute("search", search);
		request.setAttribute("applTpCd", applTpCd);
		
		include("listApacheMonitoringRegister.jsp", request, response);
	}
}
