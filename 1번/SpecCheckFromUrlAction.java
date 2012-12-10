/*
 * @(#)SpecCheckFromUrlAction.java $version 2012. 4. 19.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.hangame.admin.specmgr.action;

import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hangame.admin.specmgr.bo.ProductRegistrationBO;
import com.hangame.admin.specmgr.model.ProductSpec;
import com.hangame.admin.specmgr.model.SpecModel;
import com.hangame.admin.specmgr.util.SpecCheckDataParseUtils;
import com.hangame.iims.action.IIMSBaseAction;
import com.nhncorp.lucy.common.container.SpringBeanFactory;
import com.nhncorp.lucy.common.data.DataHandlerFactory;

/**
 * @author NHN
 */
public class SpecCheckFromUrlAction extends IIMSBaseAction {
	private static final Log LOG = LogFactory.getLog(SpecCheckFromUrlAction.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 5162613811781080728L;

	private long startMillis;
	private long endMillis;
	/**
	 * @return
	 * @throws Exception
	 * @see com.hangame.ds.cbt.action.DSBaseAction#execute()
	 */
	@Override
	public String execute() throws Exception {
		return null;
	}
	
	public String cpuSpecXPath() {
		String d = params.getString("dn", "");
	

		final String CPU_URL = DataHandlerFactory.getDataHandler().get("specmgr/cpu_url");
		List<SpecModel> specList = specXPath("CPU", CPU_URL, "cpulist");
		setAttribute("cpu", specList);
		return d.toLowerCase().equals("y") ? "cpuSpecDn" : "cpuSpec";
	}

	public String vgaSpecXPath() {
		String d = params.getString("dn", "");
		
		final String VGA_URL = DataHandlerFactory.getDataHandler().get("specmgr/vga_url");
		List<SpecModel> specList = specXPath("VGA", VGA_URL, "cpulist");
		setAttribute("vga", specList);
		return d.toLowerCase().equals("y") ? "vgaSpecDn" :"vgaSpec";
	}
	
	private List<SpecModel> specXPath(String type, String url, String className) {
		int matchCount = 0;
		
		startMillis = System.currentTimeMillis();
		List<SpecModel> specList = SpecCheckDataParseUtils.getListByHtmlCleanerUsingXPath(type, url, className);
		endMillis = System.currentTimeMillis();
		LOG.debug("elapse time(ms) : " + (endMillis - startMillis));
		
		ProductRegistrationBO registrationBO = (ProductRegistrationBO)SpringBeanFactory.getBean("registrationBO");
		final List<ProductSpec> productList = registrationBO.getProductList(type, "ASC");
		for (SpecModel sm : specList) {
			for (ProductSpec ps : productList) {
				
				if (StringUtils.isEmpty(sm.getSpecFilterName())) {
					break;
				}
				if (sm.getSpecFilterName().equals(ps.getSpecName())) {
					LOG.debug("[MATCHED!!] specFilterName:  " + sm.getSpecFilterName() + ", product spec name: " + ps.getSpecName());
					sm.setIsRegister(true);
					matchCount++;
					break;
				}
			}
		}
		
		LOG.debug("Match Count: " + matchCount + ", productCount: " + productList.size());
		return specList;
	}
	
}
