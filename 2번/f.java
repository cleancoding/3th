        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            WebView  webView = (WebView) findViewById(R.id.mail_read_content);
            LinearLayout  contentLayout = (LinearLayout) findViewById(R.id.mail_read_content_layout);
            int  paddingTop = contentLayout.getPaddingTop();

            if (dY > 0) {
                if (mDirection == DIRECTION_UP) {
                    int  webviewTop = webView.getTop();

                    if (webviewTop > MARGIN_VALUE && webView.getBottom() + MARGIN_VALUE >= contentLayout.getHeight()) {
                        contentLayout.setPadding(0, (int) -dY + paddingTop, 0, 0);
                        return true;
                    }
                    if (webviewTop < MARGIN_VALUE) {
                        contentLayout.setPadding(0, -webviewTop + paddingTop + MARGIN_VALUE, 0, 0);
                        return true;
                    }
                }

                mDirection = DIRECTION_UP;
            }
            else if (dY < 0) {
                if (mDirection == DIRECTION_DOWN) {
                    if (paddingTop < 0 && webView.getScrollY() == 0) {
                        contentLayout.setPadding(0, (int) -dY + paddingTop, 0, 0);
                        return true;
                    }
                    if (paddingTop > 0) {
                        contentLayout.setPadding(0, 0, 0, 0);
                        return true;
                    }
                }

                mDirection = DIRECTION_DOWN;
            }

            return super.onScroll(e1, e2, dX, dY);
        }