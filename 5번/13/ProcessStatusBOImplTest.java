/*
 * @(#)ProcessStatusBOImplTest.java $version 2012. 4. 23.
 *
 * Copyright 2011 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhncorp.ips.common.bo;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.nhncorp.common.util.FailIdRemover;
import com.nhncorp.common.util.WorkingdayCalculator;
import com.nhncorp.ips.common.dao.ProcessStatusDAO;
import com.nhncorp.ips.common.exception.NotReportedProductException;
import com.nhncorp.ips.model.Admin;
import com.nhncorp.ips.model.Code;
import com.nhncorp.ips.model.Owner;
import com.nhncorp.ips.model.Presume;
import com.nhncorp.ips.model.ReportProduct;
import com.nhncorp.ips.model.WorkRequest;
import com.nhncorp.ips.model.WorkStatus;
import com.nhncorp.ips.process.api.ProcessStatusBO;
import com.nhncorp.ips.remote.pmon.PmonInvoker;
import com.nhncorp.ips.seller.bo.PresumeBO;
import com.nhncorp.pmon.ips.response.IpsResponse;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessStatusBOImplTest {
	private ProcessStatusBO sut;
	@Mock
	private ProcessStatusDAO processStatusDAO;
	@Mock
	private PmonInvoker pmonInvoker;
	@Mock
	private PresumeBO presumeBO;
	@Mock
	private FailIdRemover failIdRemover;
	@Mock
	private WorkingdayCalculator workingdayCalculator;
	@Mock
	private ResultSenderBO resultSenderBO;

	private WorkRequest workRequest;
	private Set<Long> idList;
	private long productId;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sut = new ProcessStatusBOImpl();

		ReflectionTestUtils.setField(sut, "processStatusDAO", processStatusDAO);
		ReflectionTestUtils.setField(sut, "pmonInvoker", pmonInvoker);
		ReflectionTestUtils.setField(sut, "failIdRemover", failIdRemover);
		ReflectionTestUtils.setField(sut, "workingdayCalculator", workingdayCalculator);
		ReflectionTestUtils.setField(sut, "presumeBO", presumeBO);
		ReflectionTestUtils.setField(sut, "resultSenderBO", resultSenderBO);

		workRequest = new WorkRequest();
		productId = 200L;

		final ArrayList<ReportProduct> productList = new ArrayList<ReportProduct>();
		productList.add(new ReportProduct(productId));
		workRequest.setProductList(productList);
		workRequest.setAdmin(new Admin("IPS_ADMIN", "IPS_ADMIN"));

		idList = new HashSet<Long>();
		idList.add(productId);

		when(pmonInvoker.updateStatus(anySet(), anyString(), anyString())).thenReturn(new IpsResponse());
		final ArrayList<ReportProduct> list = new ArrayList<ReportProduct>();
		list.add(new ReportProduct());
		when(failIdRemover.adjust(anyList(), anySet())).thenReturn(list);
	}

	/**
		 * Test method for {@link com.nhncorp.ips.common.bo.ProcessStatusBOImpl#modifyProcessStatus(com.nhncorp.ips.common.model.WorkRequest)}.
		 * @throws NotReportedProductException 
		 */
	@Test
	public final void testModifyProcessStatusPresumeSend_신고승인() throws NotReportedProductException {
		// given
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.REPORT_ACCEPTED));
		when(processStatusDAO.updateProductProcessStatus(workRequest)).thenReturn(1);
		when(processStatusDAO.insertProcessStatusHistory(workRequest)).thenReturn(1);
		when(workingdayCalculator.calc((Date) any(), eq(3))).thenReturn(new Date());

		// when
		sut.modifyProcessStatus(workRequest);

		// then
		verifyModifyProcessStatus(WorkStatus.REPORT_ACCEPTED);
		verify(workingdayCalculator).calc((Date) any(), eq(3));
	}

	@Test
	public final void testModifyProcessStatusPresumeSend_신고반려() throws NotReportedProductException {
		// given
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.REPORT_REJECTED));

		when(processStatusDAO.updateProductProcessStatus(workRequest)).thenReturn(1);
		when(processStatusDAO.insertProcessStatusHistory(workRequest)).thenReturn(1);

		// when
		sut.modifyProcessStatus(workRequest);

		// then
		verifyModifyProcessStatus(WorkStatus.REPORT_REJECTED);
	}

	@Test
	public void testModifyProcessStatusPresumeSend_소명승인() throws Exception {
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.PRESUME_ACCEPTED));

		when(processStatusDAO.updateProductProcessStatus(workRequest)).thenReturn(1);
		when(processStatusDAO.insertProcessStatusHistory(workRequest)).thenReturn(1);

		// when
		sut.modifyProcessStatus(workRequest);

		// then
		verifyModifyProcessStatus(WorkStatus.PRESUME_ACCEPTED);
		verify(presumeBO).modifyPresume(workRequest);
	}

	@Test
	public void testModifyProcessStatusPresumeSend_소명반려() throws Exception {
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.PRESUME_REJECTED));

		when(processStatusDAO.updateProductProcessStatus(workRequest)).thenReturn(1);
		when(processStatusDAO.insertProcessStatusHistory(workRequest)).thenReturn(0);

		// when
		sut.modifyProcessStatus(workRequest);

		// then
		verify(processStatusDAO).updateProductProcessStatus(workRequest);
		verify(processStatusDAO).insertProcessStatusHistory(workRequest);
		verify(presumeBO).modifyPresume(workRequest);
	}
	
	@Test
	public void 신고승인하였는데_이미_판매금지되어_신고승인처리할_수_없는_경우() throws Exception {
		// given
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.REPORT_ACCEPTED));
		
		final IpsResponse response = new IpsResponse();
		response.getFailProductIdList().add(productId);
		
		when(pmonInvoker.updateStatus(idList, workRequest.getWkResult().getCode(), workRequest.getAdmin().getAdminId())).thenReturn(response);
		
		final ArrayList<ReportProduct> emptyProductList = new ArrayList<ReportProduct>();
		when(failIdRemover.adjust(workRequest.getProductList(), response.getFailProductIdList())).thenReturn(emptyProductList);
		
		// when
		IpsResponse result = sut.modifyProcessStatus(workRequest);
		
		// then 
		assertThat(result, is(response));
	}

	@Test(expected = NotReportedProductException.class)
	public void testModifyProcessStatusPresumeSend_소명승인_신고된_상품이_없는_경우() throws Exception {
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.PRESUME_ACCEPTED));

		when(processStatusDAO.updateProductProcessStatus(workRequest)).thenReturn(-1);
		when(processStatusDAO.insertProcessStatusHistory(workRequest)).thenReturn(1);

		// when
		sut.modifyProcessStatus(workRequest);

		// then
		verifyModifyProcessStatus(WorkStatus.PRESUME_ACCEPTED);
		verify(presumeBO).modifyPresume(workRequest);
	}

	private void verifyModifyProcessStatus(WorkStatus workStatus) throws NotReportedProductException {
		verify(processStatusDAO).updateProductProcessStatus(workRequest);
		verify(processStatusDAO).insertProcessStatusHistory(workRequest);
		verify(pmonInvoker).updateStatus(idList, workStatus.getCode(), "IPS_ADMIN");
		verify(resultSenderBO).send(workRequest);
	}

	@Test
	public void testModifyProcessStatusPresumeSend() throws Exception {
		final Presume presume = new Presume();
		final ReportProduct reportProduct = new ReportProduct(1L);
		final Owner owner = new Owner("no", "id");
		reportProduct.setReportingOwner(owner);
		presume.setReportProduct(reportProduct);
		
		workRequest.getProductList().add(reportProduct);
		workRequest.setOwner(owner);
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.PRESUME_SENDED));
		workRequest.setWorkDate(new Date());
		
		when(processStatusDAO.updateProductProcessStatus((WorkRequest) anyObject())).thenReturn(1);
		when(processStatusDAO.insertProcessStatusHistory((WorkRequest) anyObject())).thenReturn(1);
		
		sut.modifyProcessStatusPresumeSend(presume);
		
		verify(processStatusDAO).updateProductProcessStatus((WorkRequest) anyObject());
		verify(processStatusDAO).insertProcessStatusHistory((WorkRequest) anyObject());
		verifyNoMoreInteractions(pmonInvoker);
	}
}
