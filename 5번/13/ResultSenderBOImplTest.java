/*
 * @(#)ResultSenderBOImplTest.java $version 2012. 6. 25.
 *
 * Copyright 2011 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhncorp.ips.common.bo;


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.nbp.nmp.api.product.v1.RemoteSimpleProduct;
import com.nbp.nmp.api.product.v1.RemoteSimpleProductService;
import com.nbp.nmp.seller.SellerType;
import com.nbp.nmp.seller.api.v1.BusinessSeller;
import com.nbp.nmp.seller.api.v1.RemoteSeller;
import com.nbp.nmp.seller.api.v1.RemoteSellerService;
import com.nhncorp.ips.model.Code;
import com.nhncorp.ips.model.Owner;
import com.nhncorp.ips.model.Presume;
import com.nhncorp.ips.model.ReportProduct;
import com.nhncorp.ips.model.Seller;
import com.nhncorp.ips.model.UserType;
import com.nhncorp.ips.model.WorkRequest;
import com.nhncorp.ips.model.WorkStatus;
import com.nhncorp.ips.owner.bo.OwnerBO;
import com.nhncorp.support.util.cos.CosService;
import com.nhncorp.support.util.mex.SmsUtil;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultSenderBOImplTest {
	ResultSenderBO sut;
	
	@Mock
	private CosService cosService;
	@Mock
	private SmsUtil smsUtil;
	@Mock
	private RemoteSellerService remoteSellerService;
	@Mock
	private RemoteSimpleProductService remoteSimpleProductService;
	@Mock
	private OwnerBO ownerBO;

	private WorkRequest workRequest;

	private String sellerId;

	private long productId;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sut = new ResultSenderBOImpl();
		
		ReflectionTestUtils.setField(sut, "cosService", cosService);
		ReflectionTestUtils.setField(sut, "smsUtil", smsUtil);
		ReflectionTestUtils.setField(sut, "remoteSellerService", remoteSellerService);
		ReflectionTestUtils.setField(sut, "remoteSimpleProductService", remoteSimpleProductService);
		ReflectionTestUtils.setField(sut, "ownerBO", ownerBO);
		
		workRequest = new WorkRequest();
		workRequest.setWkResult(new Code<WorkStatus>(WorkStatus.REPORT_ACCEPTED));
		
		sellerId = "sellerId";
		productId = 1L;
	}
	
	@Test
	public void 상품상태변경결과에_대해_메일_문자_발송을_한다() throws Exception {
		final RemoteSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.PERSONAL);
		remoteSeller.setCellPhoneNumber("010-3188-2329");
		
		결과발송(remoteSeller);
	}
	
	@Test
	public void 사업자일경우() throws Exception {
		final RemoteSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.BUSINESS);
		remoteSeller.setCellPhoneNumber("010-3188-2329");
		
		결과발송(remoteSeller);
	}
	
	@Test
	public void 글로벌_국내거주_개인일경우() throws Exception {
		final RemoteSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.DOMESTIC_PERSONAL);
		remoteSeller.setCellPhoneNumber("010-3188-2329");
		
		결과발송(remoteSeller);
	}
	
	@Test
	public void 글로벌_국내거주_회사일경우() throws Exception {
		final RemoteSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.DOMESTIC_BUSINESS);
		remoteSeller.setCellPhoneNumber("010-3188-2329");
		
		결과발송(remoteSeller);
	}
	
	@Test
	public void 글로벌_해외거주_개인일경우() throws Exception {
		final RemoteSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.OVERSEAS_PERSONAL);
		remoteSeller.setOverseasTelephoneNumber("010-3188-2329");
		
		결과발송(remoteSeller);
	}
	
	@Test
	public void 글로벌_해외거주_회사일경우() throws Exception {
		final BusinessSeller remoteSeller = new BusinessSeller();
		remoteSeller.setType(SellerType.OVERSEAS_BUSINESS);
		remoteSeller.setChargerOverseasTelephoneNumber("010-3188-2329");
		
		final RemoteSimpleProduct prod = new RemoteSimpleProduct();
		prod.setSellerId(sellerId);
		when(remoteSimpleProductService.get(productId)).thenReturn(prod);
		
		결과발송(remoteSeller, null);
	}

	private void 결과발송(final RemoteSeller remoteSeller, final Presume presume) {
		final ReportProduct product = new ReportProduct();
		
		product.setProductId(productId);
		product.setPresume(presume);
		
		workRequest.getProductList().add(product);

		when(remoteSellerService.findByLoginId(sellerId)).thenReturn(remoteSeller);
		
		final Owner owner = new Owner();
		owner.setUserTypeCd(UserType.MEMBER.getCode());
		owner.setUserTypeGrCd(UserType.MEMBER.getGroupCode());
		
		when(ownerBO.findOwnerByReportedProduct(product)).thenReturn(owner);
		// when
		sut.send(workRequest);
		
		// then
		
		// "presume에 seller정보가 셋팅되어 있으므로 seller id를 알기 위해 api를 호출하지 않는다"
		assertThat(product.getSeller().getPhoneNumber(), is("010-3188-2329"));
	}
	
	private void 결과발송(final RemoteSeller remoteSeller) {
		final Seller presumeSeller = new Seller(1L, sellerId, "name", "shopName", "phoneNumber");
		final Presume presume = new Presume();
		presume.setSeller(presumeSeller);
		
		결과발송(remoteSeller, presume);
		verifyZeroInteractions(remoteSimpleProductService);
	}
	
	@Test
	public void 문자발송_메일발송을_모두off하면_아무동작도_하지않는다() throws Exception {
		// given
		workRequest.setMailSendYn("N");
		workRequest.setSmsSendYn("N");

		// when
		sut.send(workRequest);
		
		// then
		verifyNoMoreInteractions(cosService,smsUtil,remoteSellerService,remoteSimpleProductService,ownerBO);
	}

}
