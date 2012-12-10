    private void makeEventControlElement(Event event, boolean withAnimation) {
        LinearLayout element = withAnimation ? eventControl.addElement(in, eventContextMenuManager) : eventControl.addElement(null, eventContextMenuManager);

        eventControl.setEventTypeCodeToElement(event.getEventTypeCode(), element);

        boolean isSolar = event.getDayTypeCode() == DayTypeCode.S ? true : false;
        eventControl.setSolarToElement(isSolar, element);

        EditText edittext = eventControl.getEditTextOf(element);
        String dateString = event.getValue();
        if (dateString != null) {
            Date date = null;
            String fixedDateString = dateString.replace("-", "");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            try {
                date = formatter.parse(fixedDateString);
            } catch (Exception e) {
                Log.e(TAG, "date formatting error");
            }

            edittext.setText(DateFormat.format("yyyy-MM-dd", date));
            eventControl.setRightImageButtonVisibilityWitchContain(edittext, View.VISIBLE);
        }
        edittext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar(view);
            }
        });

        eventControl.setEditControlEventListener(new EditControlEventListener() {

            @Override
            public void onClickDeleteButton(View view) {
                eventControl.removeElementWhichContain(view, out);
            }
        });
    }

JODA Date
---
    /**
     * @param edittext
     */
    protected void showCalendar(View view) {
        calendarEditText = (EditText)view;
        String calendarValue = calendarEditText.getText().toString();

        Calendar cal = new GregorianCalendar();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (StringUtils.isNotEmpty(calendarValue)) {
            String spliter = "-";
            String[] arr = StringUtils.split(calendarValue, spliter);
            if (arr[0] != null) {
                year = Integer.valueOf(arr[0]);
            }
            if (arr[1] != null) {
                month = Integer.valueOf(arr[1]);
            }
            if (arr[2] != null) {
                day = Integer.valueOf(arr[2]);
            }
        }

        PWEDateInfo dateInfo = PWEDatePickerSQLiteDAO.getInstatnce(this).getSDate(year, month, day);
        Bundle extras = new Bundle();
        extras.putSerializable("PWEDateInfo", dateInfo);

        Intent intent = new Intent(this, PWEDatePickerSimpleDialog.class);
        intent.putExtras(extras);
        startActivityForResult(intent, REQUEST_CALENDAR);

    }