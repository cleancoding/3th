
    // OnClickListener interface
    @Override
    public void onClick(View v) {
        NClicksManager  nClicksManager = NClicksManager.getSharedInstance();

        // 비활성화 시킬 필요 없는 버튼/레이아웃
        // eungyo : ADT14이후로 라이브러리의 R.id 값은 final이 아니다 이로 인하여 switch로 코딩할 수 없다.
        int clickedId = v.getId(); 
        
        if (clickedId == R.id.mail_read_titlebar_layout) {              // [타이틀바] 레이아웃
            LinearLayout  bodyLayout = (LinearLayout) mActivity.findViewById(R.id.mail_read_content_layout);
            bodyLayout.setPadding(0, 0, 0, 0);

            WebView  webView = (WebView) mActivity.findViewById(R.id.mail_read_content);
            webView.scrollTo(0, 0);
            return;
        }
        else if (clickedId == R.id.mail_read_reply_menu_layout) {       // [reply 메뉴] 레이아웃
            hideReplyMenuLayout();
            return;
        }
//      else if (clickedId == R.id.mail_read_more_menu_layout) {        // [... 메뉴] 레이아웃
//          hideMoreMenuLayout();
//          return;
//      }       
        // 캘린더 일정 버튼 
        else if (clickedId == R.id.mail_read_cal_accept_button) {       // [수락] 버튼 
            nClicksManager.sendData("rdm.accept");
            _replyCalendar(CalendarHandler.REPLY_ACCEPT);
            return;
        }
        else if (clickedId == R.id.mail_read_cal_tentative_button) {    // [미정] 버튼 
            nClicksManager.sendData("rdm.notyet");
            _replyCalendar(CalendarHandler.REPLY_TENTATIVE);    
            return;
        }
        else if (clickedId == R.id.mail_read_cal_reject_button) {       // [거절] 버튼 
            nClicksManager.sendData("rdm.reject");
            _replyCalendar(CalendarHandler.REPLY_REJECT);
            return;
        }
        else if (clickedId == R.id.mail_read_cal_open_button) {     // [내 캘린더 보기] 버튼 
            String url = mActivity.getCalendarLinkUrl();
            if (url != null) {
                NHNMail.viewCalendar(mActivity, url);
                return;
            }
        }

        // 비활성화 시킬 필요 있는 버튼/레이아웃
        v.setEnabled(false);

        if (clickedId == R.id.mail_read_mailbox) {                      // [메일함] 버튼
            nClicksManager.sendData("rdt.prelist");
            _finishActivity();
        }
        else if (clickedId == R.id.mail_read_new_mail) {                // [새 메일] 버튼
            nClicksManager.sendData("rdt.new");
            launchWriteActivity(null);
        }

        else if (clickedId == R.id.mail_read_previous_button) {         // [이전 메일] 버튼
            nClicksManager.sendData("rdb.prev");
            mActivity.loadPrevMessage(false);
        }
        else if (clickedId == R.id.mail_read_next_button) {             // [다음 메일] 버튼
            nClicksManager.sendData("rdb.next");
            mActivity.loadNextMessage(false);
        }
        else if (clickedId == R.id.mail_read_reply_button) {            // [reply 메뉴] 버튼
            nClicksManager.sendData("rdb.this");
            _showReplyMenuLayout(v);
        }
        else if (clickedId == R.id.mail_read_trash_button) {            // [삭제] 버튼
            nClicksManager.sendData("rdb.del");
            mActivity.getMessageHandler().deleteMessage();
        }
        else if (clickedId == R.id.mail_read_move_button) {             // [이동] 버튼
            nClicksManager.sendData("rdb*l.move");
            _popupMailBoxListActivity();
        }
//      else if (clickedId == R.id.mail_read_more_button) {             // [... 메뉴] 버튼
//          nClicksManager.sendData("rdb.more");
//          _showMoreMenuLayout(v);
//      }

        else if (clickedId == R.id.mail_read_reply_menu_reply) {        // [답장] 버튼
            nClicksManager.sendData("rdb*m.reply");
            _launchWriteActivity(MailWriteActivity.ACTION_REPLY);
        }
        else if (clickedId == R.id.mail_read_reply_menu_reply_all) {    // [전체 답장] 버튼
            nClicksManager.sendData("rdb*m.replyall");
            _launchWriteActivity(MailWriteActivity.ACTION_REPLY_ALL);
        }
        else if (clickedId == R.id.mail_read_reply_menu_forward) {      // [전달] 버튼
            nClicksManager.sendData("rdb*m.forward");
            _launchWriteActivity(MailWriteActivity.ACTION_FORWARD);
        }
        else if (clickedId == R.id.mail_read_reply_menu_schedule) {     // [일정으로 등록] 버튼
            nClicksManager.sendData("rdb*m.calendar");
            hideReplyMenuLayout();
            NHNMail.openCalendar(mActivity, (LocalMessage) mActivity.getMessage(), "schedule");
        }
        else if (clickedId == R.id.mail_read_reply_menu_task) {         // [할일으로 등록] 버튼
            nClicksManager.sendData("rdb*m.todo");
            hideReplyMenuLayout();
            NHNMail.openCalendar(mActivity, (LocalMessage) mActivity.getMessage(), "task");
        }
        else if (clickedId == R.id.mail_read_reply_menu_memo) {         // [메모로 등록] 버튼
            nClicksManager.sendData("rdb*m.memo");
            hideReplyMenuLayout();
            NHNMail.openMemo(mActivity, (LocalMessage) mActivity.getMessage());
        }
        else if (clickedId == R.id.mail_read_reply_menu_spam) {         // 2012.06.13 hongseok: [스팸 신고] 버튼
            nClicksManager.sendData("rdb*m.spam");
            hideReplyMenuLayout();
            mActivity.getMessageHandler().processSpamMessage();
        }
                
//      else if (clickedId == R.id.mail_read_more_menu_move) {          // [폴더 이동] 버튼
//          nClicksManager.sendData("rdb*l.move");
//          _popupMailBoxListActivity();
//      }
//      else if (clickedId == R.id.mail_read_more_menu_star_flag) {     // [중요 표시] 버튼
//          nClicksManager.sendData("rdb*l.star");
//          _toggleStarFlag();
//      }
//      else if (clickedId == R.id.mail_read_more_menu_circle_flag) {   // [읽음/안읽음 표시] 버튼
//          nClicksManager.sendData("rdb*l.read");
//          _toggleReadFlag();
//      }
                
//      else if (clickedId == R.id.mail_read_more_menu_spam_report) {   // [스팸 신고] 버튼
//          nClicksManager.sendData("rdb*l.spam");
//          hideMoreMenuLayout();
//          ToastUtils.showToastPopupOnThread(mActivity, "[스팸 신고] 기능은 지원하지 않습니다.");
//      }

        else if (clickedId == R.id.mail_read_circle_flag) {             // [읽음/안읽음 표시] 버튼
            nClicksManager.sendData("rdm.read");
            _toggleReadFlag();
        }
        else if (clickedId == R.id.mail_read_star_flag) {               // [중요 표시] 버튼
            nClicksManager.sendData("rdm.star");
            _toggleStarFlag();
        }
        else if (clickedId == R.id.mail_read_show_images_button) {      // [이미지 표시] 버튼
            _showImages();
        }
        else if (clickedId == R.id.popup_container) {               // Popup Action 버튼 - [UNDO] 버튼 / [스팸설정] 버튼
            // 클릭 영역이 popup 전체임. Object는 'popup_action_message'에 settag 해 놓았음.
            POPUP_TYPE popupType = (POPUP_TYPE) v.findViewById(R.id.popup_action_message).getTag(R.string.popup_action_type_param);
            
            // nClicks.
            if (popupType == POPUP_TYPE.SPAM_MOVE)          // 스팸메일 처리 : 스팸메일함으로 보내기
                nClicksManager.sendData("rbl.movespam");
            else if (popupType == POPUP_TYPE.SPAM_DELETE)   // 스팸메일 처리 : 영구 삭제
                nClicksManager.sendData("rbl.delspam");
            else if (popupType == POPUP_TYPE.UNDO_MOVE)     // 이동 실행 취소
                nClicksManager.sendData("rbl.unmove");
            else if (popupType == POPUP_TYPE.UNDO_DELETE)   // 삭제 실행 취소
                nClicksManager.sendData("rbl.undel");
            
            // 클릭하면 강제로 popup을 닫기 때문에, 일정시간 후에 popup이 닫히는 코드 취소.
            mActivity.getPopupHandler().removeCallbacks(mActivity.getPopupRunnable());
            
            PopupWindow popup = mActivity.getPopupWindow();
            popup.dismiss();
            
            if (popupType == POPUP_TYPE.SPAM_DELETE || popupType == POPUP_TYPE.SPAM_MOVE) {     
                Intent intent = new Intent(mActivity, PreferenceSubActivity.class);
                intent.putExtra(PreferenceSubActivity.REQUEST_SUBPAGE, R.layout.preference_sub_spam);
                mActivity.startActivityForResult(intent, 0);
            }
            else {
                undoDeleteOrMove();
            }
        }

        v.setEnabled(true);
    }

