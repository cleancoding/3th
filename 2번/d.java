

                        String  contentType = null;
                        String  contentDisposition = null;

                        try {
                            contentType = MimeUtility.unfoldAndDecode( attachmentPart.getContentType() );
                            contentDisposition = MimeUtility.unfoldAndDecode( attachmentPart.getDisposition() );
                        }
                        catch (Exception e) {
                        }

                        String  filename = MimeUtility.getHeaderParameter(contentType, "name");
                        if (filename == null) {
                            filename = MimeUtility.getHeaderParameter(contentDisposition, "filename");
                            if (filename == null)
                                filename = "";
                        }