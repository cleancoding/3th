package com.nbp.nsight.apache.portlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nbp.nsight.apache.manager.ApacheManager;
import com.nbp.nsight.apache.manager.ApacheManagerImpl;
import com.nbp.nsight.common.base.BasePortlet;
import com.nbp.nsight.common.dto.SearchParam;
import com.nbp.nsight.server.manager.ServerManager;
import com.nbp.nsight.server.manager.ServerManagerImpl;

/**
 * Portlet implementation class ApacheGroupStatus
 */

@Service
public class ApacheInformationPortlet extends BasePortlet {

	@Autowired
	private ApacheManager apacheManager = new ApacheManagerImpl();
	private ServerManager serverManager = new ServerManagerImpl();

	@Override
	protected void beforeInclude(RenderRequest request, RenderResponse response) {
		request.setAttribute("listApacheInformationURL", buildURL(response, "listApacheInformation"));
	}
	
	@Override
	protected void afterInclude(RenderRequest request, RenderResponse response) {
		
	}

	public void listApacheInformation(RenderRequest request, RenderResponse response)
			throws IOException, PortletException, SQLException {
		// Check Parameter
		SearchParam search = parseSearchParam(request);	
		search.setApplTpCd("APPTP_APACHE");
		// Call Manager
		List<HashMap> apacheInfo = apacheManager.getApacheHostInformation(search);
		//List<HashMap> apacheOperationInfo = apacheManager.getApacheHostOperatorInformation(search);
		List<Map> operators = serverManager.getServerOperators(search.getHostId());

		// Pass to JSP			
		request.setAttribute("apacheInfo", apacheInfo);
		request.setAttribute("operators", operators);
		include("listApacheInformation.jsp", request, response);
	}
}