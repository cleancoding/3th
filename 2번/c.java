    @Override
        public void run() {
            if (mReadOnlyFlag /*== true*/)
                return;

            // ...
            Intent intent = getIntent();
            if (intent == null) return;
                TextView  mailboxNameView = (TextView) findViewById(R.id.mail_read_mailbox_name);
                mailboxNameView.setText(intent.getStringExtra(EXTRA_FOLDER_NAME));

                TextView  unreadCountView = (TextView) findViewById(R.id.mail_read_unread_count);
                if (mUnreadCount >= 100)
                    unreadCountView.setText("(99+)");
                else if (mUnreadCount > 0)
                    unreadCountView.setText("(" + mUnreadCount + ")");
                else
                    unreadCountView.setText("");

        }
    };


