/*
 * @(#)CorrectionBOImpl.java $version 2012. 3. 12.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nhncorp.mct.correction.bo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nhncorp.lucy.spring.file.FileUploadInfo;
import com.nhncorp.lucy.spring.file.FileUploadItem;
import com.nhncorp.lucy.spring.file.manager.FileUploadManager;
import com.nhncorp.mct.common.dao.WrkHistDAO;
import com.nhncorp.mct.common.model.PanoWork;
import com.nhncorp.mct.common.type.FileType;
import com.nhncorp.mct.common.type.WrkStatCd;
import com.nhncorp.mct.common.type.WrkTypeCd;
import com.nhncorp.mct.common.util.FileHandler;
import com.nhncorp.mct.correction.dao.CorrectionDAO;
import com.nhncorp.mct.correction.model.CorrectionFileInfo;
import com.nhncorp.mct.user.model.UserInfo;

/**
 */
@Service
public class CorrectionBOImpl implements CorrectionBO {
	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private CorrectionDAO dao;

	@Autowired
	private WrkHistDAO histDao;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private FileUploadManager fileUploadManager;

	@Autowired
	private FileUploadInfo fileUploadInfo;

	private static final List<FileType> UPLOAD_FILE_TYPE = new ArrayList<FileType>();
	private static final int UPLOAD_FILE_COUNT = 3;

	static {
		UPLOAD_FILE_TYPE.add(FileType.ORGN);
		UPLOAD_FILE_TYPE.add(FileType.PANO);
		UPLOAD_FILE_TYPE.add(FileType.THUMB);
	}

	@Override
	@Transactional
	public void correctionProcessBySingle(UserInfo userInfo, WrkTypeCd wrkTypeCd, PanoWork panoWork) throws DeadlockLoserDataAccessException, Exception {
		if (checkCorrectionInfo(userInfo, wrkTypeCd, panoWork) == false) {
			return;
		}

		dao.updatePano(panoWork);
		if (wrkTypeCd == WrkTypeCd.BLR || wrkTypeCd == WrkTypeCd.INSP) {
			// dao.mergeBlrCont(panoWork);
			dao.replaceBlrCont(panoWork);
		}

		histDao.insertWrkHist(userInfo, wrkTypeCd, panoWork);
		log.info("correctionProcessBySingle - " + userInfo.getWrkrId() + " : " + wrkTypeCd + " : " + panoWork.getPanoNo());
	}

	/**
	 * 색상보정, 스티칭, 촬영선보정 작업 진행
	 * @param userInfo
	 * @param wrkTypeCd
	 * @param panoWork
	 * @return
	 * @throws Exception
	 * @see com.nhncorp.mct.correction.bo.CorrectionBO#correctionFileProcessBySingle(com.nhncorp.mct.user.model.UserInfo, com.nhncorp.mct.common.type.WrkTypeCd, com.nhncorp.mct.common.model.PanoWork)
	 */
	@Override
	@Transactional
	public int correctionFileProcessBySingle(UserInfo userInfo, WrkTypeCd wrkTypeCd, PanoWork panoWork) throws Exception {
		long stTime = System.currentTimeMillis();
		if (checkCorrectionInfo(userInfo, wrkTypeCd, panoWork) == false) {
			return 0;
		}

		int uploadCount = 0;
		String wrkrId = userInfo.getWrkrId();
		String wrkStatCd = panoWork.getWrkStatCd();
		String panoNo = panoWork.getPanoNo();

		if (StringUtils.equals(wrkStatCd, WrkStatCd.CLR.toString()) || StringUtils.equals(wrkStatCd, WrkStatCd.SNPST.toString()) || StringUtils.equals(wrkStatCd, WrkStatCd.STCH.toString())) {
			if (!panoWork.isAllExistAttachFile()) {
				return uploadCount;
			}

			List<MultipartFile> panoAttachFileList = panoWork.getAttachfileList();
			List<CorrectionFileInfo> correctionFileList = new ArrayList<CorrectionFileInfo>();
			String path = messageSource.getMessage("imgpath", null, Locale.getDefault());
			String delim = messageSource.getMessage("delim", null, Locale.getDefault());

			String wrkHistSeq = histDao.insertWrkHist(userInfo, wrkTypeCd, panoWork); // 작업 로그 쌓기
			String uploadPath = path + FileType.valueOf(UPLOAD_FILE_TYPE.get(0).toString()).getFileName(delim, panoNo);
			FileUploadItem[] items = new FileUploadItem[UPLOAD_FILE_COUNT];

			try {
				// 원본파일을 백업하고 업로드하려는 파일을 원본파일의 자리로 복사
				for (int index = 0; index < UPLOAD_FILE_COUNT; index++) {
					FileType targetFileType = UPLOAD_FILE_TYPE.get(index);

					String originFileName = targetFileType.getFileName(delim, panoNo);
					String originFilePath = path + originFileName;
					String backupFilePath = path + targetFileType.getFileName(delim, panoNo, wrkHistSeq);

					CorrectionFileInfo correctionFileInfo = new CorrectionFileInfo();
					File originFile = new File(originFilePath);
					File backupFile = new File(backupFilePath);

					correctionFileInfo.setOriginFileName(originFileName);
					correctionFileInfo.setOriginFilePath(originFilePath);
					correctionFileInfo.setOriginFile(originFile);
					correctionFileInfo.setBackupFile(backupFile);
					correctionFileList.add(correctionFileInfo);

					boolean backupSuccess = FileHandler.renameToFromFile(originFile, backupFile); // 원본 파일, 업로드 파일의 이름을 바꿈
					if (backupSuccess == true) {
						correctionFileInfo.setBackupSuccess(backupSuccess);
					} else {
						throw new Exception("correctionFileProcessBySingle File Rename Error - " + wrkrId + " : " + wrkTypeCd + " : " + panoNo);
					}

					items[index] = new FileUploadItem(uploadPath, panoAttachFileList.get(index));
					items[index].setOriginalFilename(correctionFileInfo.getOriginFileName());
				}

				fileUploadInfo.setRepositoryPath(path);
				fileUploadManager.setFileUploadInfo(fileUploadInfo);
				fileUploadManager.upload(items);

				panoWork.setWrkStatCd(String.valueOf(wrkTypeCd.getToWrkStatCd()));

				dao.updatePano(panoWork);
				uploadCount++;

				log.info("correctionFileProcessBySingle File Upload Complete! - " + wrkrId + " : " + wrkTypeCd + " : " + panoNo + " : " + (System.currentTimeMillis() - stTime) + " milli Sec");
			} catch (Exception e) {
				// 파일 백업 작업 중 오류 발생 시 백업 이전 상태로 다시 파일을 복구
				log.info("correctionFileProcessBySingle File Upload Error");
				histDao.deleteWrkHist(wrkHistSeq); // insert 했던 작업로그도 다시 삭제
				rollbackFileBackup(correctionFileList, panoNo, wrkHistSeq);
				throw e;
			}
		} else {
			// 지정해제, 미사용일 경우 여기서 처리
			dao.updatePano(panoWork);
			histDao.insertWrkHist(userInfo, wrkTypeCd, panoWork);
			log.info("correctionFileProcessBySingle Complete! - " + wrkrId + " : " + wrkTypeCd + " : " + panoNo + " : " + (System.currentTimeMillis() - stTime) + " milli Sec");
		}

		return uploadCount;
	}

	/**
	 * 색상보정, 스티칭, 촬영선보정 업로드 전 파일 백업 실패 시 백업을 다시 롤백 진행
	 * @param panoNo
	 * @return
	 * @throws Exception
	 * @see com.nhncorp.mct.correction.bo.CorrectionBO#getCorrectionFileInfoList(List, String, String)
	 */
	private void rollbackFileBackup(List<CorrectionFileInfo> correctionFileList, String panoNo, String wrkHistSeq) throws Exception {
		String path = messageSource.getMessage("imgpath", null, Locale.getDefault());
		String delim = messageSource.getMessage("delim", null, Locale.getDefault());

		try {
			for (int index = 0; index < correctionFileList.size(); index++) {
				CorrectionFileInfo correctionFileInfo = correctionFileList.get(index);

				if (correctionFileInfo.isBackupSuccess() == true) {
					FileType targetFileType = UPLOAD_FILE_TYPE.get(index);
					String renamePath = path + targetFileType.getFileName(delim, panoNo, wrkHistSeq);
					String fromPath = path + targetFileType.getFileName(delim, panoNo);
					FileHandler.renameTo(renamePath, fromPath);
				}
			}

			log.info("correctionFileProcessBySingle File Backup Rollback Complete! - panoNo: " + panoNo + ", wrkHistSeq: " + wrkHistSeq);
		} catch (Exception e) {
			throw new Exception("파일 백업 롤백이 제대로 진행되지 않았습니다. 관리자에게 꼭 연락하여 주시길 바랍니다. (panoNo: " + panoNo + ", wrkHistSeq: " + wrkHistSeq + ")");
		}
	}

	private boolean checkCorrectionInfo(UserInfo userInfo, WrkTypeCd wrkTypeCd, PanoWork panoWork) {
		if (userInfo == null) {
			return false;
		}

		if (userInfo.getWrkrSeq() == null || userInfo.getWrkrId() == null) {
			return false;
		}

		if (wrkTypeCd == null) {
			return false;
		}

		if (panoWork == null) {
			return false;
		}

		return true;
	}
}
