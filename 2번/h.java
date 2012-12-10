
    public void launchQuickView(LocalAttachmentBodyPart part) {
        NHNMail  mailApp = (NHNMail) NHNMail.app;
        String  fileName = mailApp.parsingFileName(part);

        // 첨부파일을 외장메모리로 복사
        Uri  uri = mailApp.copyToExternalStorage(part.getAttachmentId(), ExStorageManager.getCacheDirectoryPath(), fileName, 0x00/*중복파일 허용 안함, 갤러리 업데이트 안함*/);
        if (uri == null) {
            ToastUtils.showToastPopup(mActivity, mActivity.getString(R.string.mail_read_toastmsg_file_copy_failed));
            return;
        }

        // 뷰어 실행 (1/2) - 확장자에 의한 뷰어 호출
        String  mimeType = null;
        boolean  exceptionFlag = false;

        try {
            mimeType = MimeUtility.getMimeTypeByExtension(fileName);

            Intent  intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            mActivity.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            //e.printStackTrace();
            exceptionFlag = true;
        }

        // 뷰어 실행 (2/2) - Content-Type에 의한 뷰어 호출
        if (exceptionFlag /*== true*/) {
            exceptionFlag = false;

            try {
                mimeType = part.getMimeType();
                final Pattern  spacePattern = Pattern.compile("[\\t\\r\\n\\s]");
                
                Matcher  spaceMatcher = spacePattern.matcher(mimeType);
                if (spaceMatcher.find() /*== true*/) {
                    int  pos = spaceMatcher.start();
                    mimeType = mimeType.substring(0, pos);
                }

                Intent  intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                mActivity.startActivity(intent);
            }
            catch (MessagingException e) {          //이유 물어보기
                //e.printStackTrace();
            }
            catch (ActivityNotFoundException e) {
                //e.printStackTrace();
                exceptionFlag = true;
            }
        }

        if (exceptionFlag /*== true*/)
            ToastUtils.showToastPopup(mActivity, mActivity.getString(R.string.mail_read_toastmsg_activity_not_found, mimeType));
    }

