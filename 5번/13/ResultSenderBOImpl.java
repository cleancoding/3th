/*
 * @(#)ResultSenderBOImpl.java $version 2012. 6. 13.
 *
 * Copyright 2011 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.nhncorp.ips.common.bo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nbp.nmp.api.product.v1.RemoteSimpleProduct;
import com.nbp.nmp.api.product.v1.RemoteSimpleProductService;
import com.nbp.nmp.seller.SellerType;
import com.nbp.nmp.seller.api.v1.BusinessSeller;
import com.nbp.nmp.seller.api.v1.RemoteSeller;
import com.nbp.nmp.seller.api.v1.RemoteSellerService;
import com.nhncorp.common.model.MailParam;
import com.nhncorp.common.model.Sms;
import com.nhncorp.ips.model.Owner;
import com.nhncorp.ips.model.ReportProduct;
import com.nhncorp.ips.model.Seller;
import com.nhncorp.ips.model.WorkRequest;
import com.nhncorp.ips.model.WorkStatus;
import com.nhncorp.ips.owner.bo.OwnerBO;
import com.nhncorp.support.util.cos.CosService;
import com.nhncorp.support.util.mex.SmsUtil;

/**
 */
@Service
public class ResultSenderBOImpl implements ResultSenderBO {
	@Autowired
	private CosService cosService;
	@Autowired
	private SmsUtil smsUtil;
	@Autowired
	private RemoteSellerService remoteSellerService;
	@Autowired
	private RemoteSimpleProductService remoteSimpleProductService;
	@Autowired
	private OwnerBO ownerBO;

	private Logger logger = LoggerFactory.getLogger(getClass());

	/* (non-Javadoc)
	 * @see com.nhncorp.ips.common.bo.MailSenderBO#send(com.nhncorp.ips.model.WorkRequest)
	 */
	@Override
	public void send(WorkRequest request) {
		if (StringUtils.equals(request.getSmsSendYn(), "N") && StringUtils.equals(request.getMailSendYn(), "N")) {
			logger.debug("### mail, sms 발송 안함 ###\n {}", request);
			return;
		}

		setupDetailInfo(request);
		logger.debug("owner info : {}", ToStringBuilder.reflectionToString(request.getOwner(), ToStringStyle.MULTI_LINE_STYLE));
		for (WorkStatus workStatus : WorkStatus.values()) {
			if (StringUtils.equals(workStatus.getCode(), request.getWkResult().getCode())) {
				MailParam mailParam = workStatus.getMailParam(request);
				logger.debug("#######################################");
				logger.debug("조건값 체크 : mailParam = {}", mailParam);
				logger.debug("조건값 체크 : request.getMailSendYn() = {}", request.getMailSendYn());
				logger.debug("#######################################");
				if (mailParam != null && request.getMailSendYn().equals("Y")) {
					//  && request.getMailSendYn().equals("Y")
					logger.debug("#######################################");
					logger.debug("메일 전송 테스트 : {}", ToStringBuilder.reflectionToString(mailParam, ToStringStyle.MULTI_LINE_STYLE));
					logger.debug("#######################################");
					cosService.send(mailParam);
				}
				List<Sms> smsParam = workStatus.getSmsParam(request);
				logger.debug("#######################################");
				logger.debug("조건값 체크 : smsParam = {}", smsParam);
				logger.debug("조건값 체크 : request.getSmsSendYn() = {}", request.getSmsSendYn());
				logger.debug("#######################################");
				if (smsParam != null && StringUtils.equals(request.getSmsSendYn(), "Y")) {
					logger.debug("#######################################");
					logger.debug("SMS 전송 테스트 : {}", ToStringBuilder.reflectionToString(smsParam, ToStringStyle.MULTI_LINE_STYLE));
					for (Sms sms : smsParam) {
						logger.debug("sms : {}", sms);
					}
					logger.debug("#######################################");
					smsUtil.send(smsParam);
				}
			}
		}
	}

	/**
	 * @param request
	 */
	private void setupDetailInfo(WorkRequest request) {
		// seller 정보 설정.
		List<ReportProduct> productList = request.getProductList();
		Seller seller;
		for (ReportProduct reportProduct : productList) {
			// 소명서 관련 이벤트라면 seller정보가 소명서에 들어있음으로
			// 소명서에서 seller 정보를 가져와 상품 정보에 넣어준다.
			if (reportProduct.getPresume() != null) {
				reportProduct.setSeller(reportProduct.getPresume().getSeller());
			}
			seller = reportProduct.getSeller();

			// seller정보가 존재하지 않는다면
			// 상품 정보는 존재한다.
			if (seller.getId() == null) {
				// 따라서 상품 ID를 기반으로 Product API를 호출하여 seller정보를 받아온다.
				RemoteSimpleProduct product = remoteSimpleProductService.get(reportProduct.getProductId());

				seller.setId(product.getSellerId());
				seller.setNo(product.getSellerNo());
				seller.setShopName(product.getSellerShopName());
			}

			logger.debug("#### {} 당한 상품 판매자는 누규? {}", request.getWkResult().getName(), seller);

			// 존재하는 seller정보를 기반으로 seller api를 호출하고 상세 seller정보에서
			// seller의 전화번호를 세팅한다.
			// TODO 만약에 상품 API에서 seller의 연락처를 바로 가져올 수 있다면 하나의 API만 태울 수 있는 경우도 존재한다.
			RemoteSeller remoteSeller = remoteSellerService.findByLoginId(seller.getId());

			logger.debug("### 실시간 연동한 seller 상세정보 : {}", ToStringBuilder.reflectionToString(remoteSeller, ToStringStyle.MULTI_LINE_STYLE));
			if (StringUtils.equals(remoteSeller.getType().getCode(), SellerType.PERSONAL.getCode()) || StringUtils.equals(remoteSeller.getType().getCode(), SellerType.DOMESTIC_PERSONAL.getCode()) || StringUtils.equals(remoteSeller.getType().getCode(), SellerType.BUSINESS.getCode()) || StringUtils.equals(remoteSeller.getType().getCode(), SellerType.DOMESTIC_BUSINESS.getCode())) {
				seller.setPhoneNumber(remoteSeller.getCellPhoneNumber());
			} else if (StringUtils.equals(remoteSeller.getType().getCode(), SellerType.OVERSEAS_PERSONAL.getCode())) {
				seller.setPhoneNumber(remoteSeller.getOverseasTelephoneNumber());
			} else if (StringUtils.equals(remoteSeller.getType().getCode(), SellerType.OVERSEAS_BUSINESS.getCode())) {
				BusinessSeller businessSeller = (BusinessSeller)remoteSeller;
				seller.setPhoneNumber(businessSeller.getChargerOverseasTelephoneNumber());
			}

			// 권리자 정보 설정
			final Owner owner = ownerBO.findOwnerByReportedProduct(reportProduct);
			reportProduct.setReportingOwner(owner);
			request.setOwner(owner);
		}

	}

}
