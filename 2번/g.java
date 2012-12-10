
    public void renderAttachments(final Part part, int depth, final Message message, final boolean updateFlag) throws MessagingException {
        if (part.getBody() instanceof Multipart) {
            Multipart  mp = (Multipart) part.getBody();
            for (int  i = 0; i < mp.getCount(); i++)
                renderAttachments(mp.getBodyPart(i), depth + 1, message, updateFlag);
        }
        else if (part instanceof LocalStore.LocalAttachmentBodyPart) {
            // 첨부 파일 1개에 대한 layout 추가
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        _fillAttachmentListLayoutFromPart(part, updateFlag);
                    }
                    catch (MessagingException e) {
                    }
                }
            });
        }
    }

