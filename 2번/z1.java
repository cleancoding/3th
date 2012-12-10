

    /**
     * 새로운 연락처 정보를 DB에 insert 후 현재 Activity를 종료
     */
    protected void insertNewContact() {
        StopWatch watch = new StopWatch();
        watch.start();
        validate();
        watch.stop();
        long s1 = watch.getTime();

        watch.reset();
        watch.start();
        long newRawContactId = contactBO.addContact(contactDetail);
        watch.stop();
        long s2 = watch.getTime();

        Log.d(TAG, "validate time : " + s1);
        Log.d(TAG, "insert to DB time : " + s2);
        Log.d(TAG, "total time : " + (s1 + s2));

        String oldFileName = "0.jpg";
        String newFileName = newRawContactId + ".jpg";
        photoMakeManager.changeFileName(oldFileName, newFileName);

        setResult(RESULT_OK, null);
        finish();
    }

