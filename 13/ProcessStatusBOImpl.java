/*
 * @(#)ProcessStatusBOImpl.java $version 2012. 4. 23.
 *
 * Copyright 2011 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhncorp.ips.common.bo;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhncorp.common.util.FailIdRemover;
import com.nhncorp.common.util.WorkingdayCalculator;
import com.nhncorp.ips.common.dao.ProcessStatusDAO;
import com.nhncorp.ips.common.exception.NotReportedProductException;
import com.nhncorp.ips.model.Admin;
import com.nhncorp.ips.model.Code;
import com.nhncorp.ips.model.Presume;
import com.nhncorp.ips.model.Product;
import com.nhncorp.ips.model.ReportProduct;
import com.nhncorp.ips.model.WorkRequest;
import com.nhncorp.ips.model.WorkStatus;
import com.nhncorp.ips.process.api.ProcessStatusBO;
import com.nhncorp.ips.remote.pmon.PmonInvoker;
import com.nhncorp.ips.seller.bo.PresumeBO;
import com.nhncorp.pmon.ips.response.IpsResponse;
import com.nhncorp.support.util.date.DateUtil;

/**
 */
@Service
public class ProcessStatusBOImpl implements ProcessStatusBO {
	@Autowired
	private ProcessStatusDAO processStatusDAO;
	@Autowired
	private PmonInvoker pmonInvoker;
	@Autowired
	private FailIdRemover failIdRemover;
	@Autowired
	private WorkingdayCalculator workingdayCalculator;
	@Autowired
	private PresumeBO presumeBO;
	@Autowired
	private ResultSenderBO resultSenderBO;

	private Logger logger = LoggerFactory.getLogger(getClass());

	/* (non-Javadoc)
	 * @see com.nhncorp.ips.admin.bo.ProcessStatusBO#modifyProcessStatus(com.nhncorp.ips.admin.model.WorkRequest)
	 */
	@Transactional
	@Override
	public IpsResponse modifyProcessStatus(WorkRequest workRequest) {
		final List<ReportProduct> productList = workRequest.getProductList();
		IpsResponse response = new IpsResponse();
		logger.debug("modifyProcessStatus : {}", productList);
		if (isRelatedWithPresume(workRequest)) {
			presumeBO.modifyPresume(workRequest);
		}

		if (isRequireInvokePmon(workRequest)) {
			// 변경할 아이디 추출
			Set<Long> idList = new HashSet<Long>();
			for (Product reportProduct : productList) {
				idList.add(reportProduct.getProductId());
			}

			response = pmonInvoker.updateStatus(idList, workRequest.getWkResult().getCode(), workRequest.getAdmin().getAdminId());
			logger.debug("## 호출 결과 pmonInvoker.updateStatus() 's response = {}", ToStringBuilder.reflectionToString(response, ToStringStyle.MULTI_LINE_STYLE));
			workRequest.setProductList(failIdRemover.adjust(productList, response.getFailProductIdList()));
		}

		// REPORT_PROD_MSTR에 있는 각각의 신고된 상품마다 처리 상태를 업데이트한다.
		logger.debug("## workRequest.getWkResult() : {}", workRequest.getWkResult());
		if (WorkStatus.REPORT_ACCEPTED.getCode().equals(workRequest.getWkResult().getCode()) || WorkStatus.REPORT_RE_ACCEPTED.getCode().equals(workRequest.getWkResult().getCode())) {
			final int restDateCountToLimitation = 3;
			Date currentDate = new Date();
			String currentDateWithoutHour = DateUtil.dateTOString(currentDate, "yyyy-MM-dd") + " 23:59:59";
			Date today = DateUtil.stringToDate(currentDateWithoutHour, "yyyy-MM-dd HH:mm:ss");
			Date limitDate = workingdayCalculator.calc(today, restDateCountToLimitation);
			workRequest.setLimitDate(limitDate);
		}

		if (workRequest.getProductList().size() == 0) {
			logger.debug("workRequest안에 productList가 비어있습니다.");
			return response;
		}

		logger.debug("workRequest = {}", ToStringBuilder.reflectionToString(workRequest, ToStringStyle.MULTI_LINE_STYLE));
		Integer result = processStatusDAO.updateProductProcessStatus(workRequest);
		if (result != workRequest.getProductList().size()) {
			throw new NotReportedProductException(workRequest);
		}
		processStatusDAO.insertProcessStatusHistory(workRequest);
		
		resultSenderBO.send(workRequest);
		
		return response;
	}

	/**
	 * @param workRequest
	 * @return
	 */
	private boolean isRelatedWithPresume(WorkRequest workRequest) {
		if (WorkStatus.PRESUME_ACCEPTED.getCode().equals(workRequest.getWkResult().getCode()) || WorkStatus.PRESUME_REJECTED.getCode().equals(workRequest.getWkResult().getCode()) || WorkStatus.ADMIN_PRESUME_ACCEPTED.getCode().equals(workRequest.getWkResult().getCode()) || WorkStatus.ADMIN_PRESUME_REJECTED.getCode().equals(workRequest.getWkResult().getCode())) {
			return true;
		}
		return false;
	}

	/**
	 * 신고승인이거나 소명승인이라면 PMON Invoker를 통해서 상태 변경을 취한다.
	 * 신고반려일 경우에도 PMON Invoker를 통해 Pmon에 알려주어 검수자가 정상 검수할 수 있어야 한다.
	 * @param workRequest
	 * @return
	 */
	private boolean isRequireInvokePmon(WorkRequest workRequest) {
		if (!WorkStatus.PRESUME_SENDED.getCode().equals(workRequest.getWkResult().getCode()) 
			&& !WorkStatus.PRESUME_REJECTED.getCode().equals(workRequest.getWkResult().getCode())
			&& !WorkStatus.ETC_PROHIBITION.getCode().equals(workRequest.getWkResult().getCode())) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.nhncorp.ips.admin.bo.ProcessStatusBO#modifyProcessStatus(com.nhncorp.ips.owner.model.Presume)
	 */
	@Override
	public void modifyProcessStatusPresumeSend(Presume presume) {
		WorkRequest workRequest = new WorkRequest();
		workRequest.setAdmin(new Admin(presume.getSeller().getId(), presume.getSeller().getName()));
		workRequest.setOwner(presume.getReportProduct().getReportingOwner());
		workRequest.getProductList().add(presume.getReportProduct());
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.PRESUME_SENDED));
		workRequest.setWorkDate(new Date());

		modifyProcessStatus(workRequest);
	}
}
