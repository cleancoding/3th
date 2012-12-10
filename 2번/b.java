public boolean isCalendarMail(final Part part) throws MessagingException {
        if (part instanceof MimeBodyPart) {
            MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
            String contentType = mimeBodyPart.getContentType();
            
            if (contentType.contains("text/calendar")) {
                try {   
                    // ical 정보 가져오기 
                    InputStream inputStream = mimeBodyPart.getBody().getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));             
                    StringBuilder sb = new StringBuilder();
                    String line;
                    
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    
                    br.close();             
                    mInputStream = sb.toString();
                }
                catch (Exception e) {
                    if (NHNMail.DEBUG) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            return false;
        }
        
        if (part.getBody() instanceof Multipart) {
            Multipart  mp = (Multipart) part.getBody();
            for (int  i = 0; i < mp.getCount(); i++) {
                if (isCalendarMail(mp.getBodyPart(i))) {
                    return true;
                }
            }
        }
        
        return false;
    }