
    private static void startUnsubscriptionTimeout() {
        Handler backgroundWorkerHandler = new Handler();
        backgroundWorkerHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // timeout: critical section
                synchronized (PweNniReceiver.UnsubscriptionTimeoutLock) {
                    if (!PweNniReceiver.isUnsubscriptionResultBroadcasted) {
                        // TIME OUT
                        broadcastTimeout(PweNniIntent.TIMEOUT_EXTRA_UNSUBSCRIBE);
                        PweNniReceiver.isUnsubscriptionResultBroadcasted = true;

                    } else {
                        // IN TIME: Do nothing
                        doNothing();
                    }
                }
            }
        }, mTimeout);
    }

    private static void broadcastTimeout(int timeoutStatuscode) {
        Intent timeoutIntent = new Intent(PweNniIntent.TIMEOUT_INTENT);
        timeoutIntent.putExtra(PweNniIntent.TIMEOUT_EXTRA, timeoutStatuscode);
        PweBroadcastManager.sendBroadcastMessage(timeoutIntent, mContext);
    }

    /*
     * Actually, this method really does nothing 
     * but it does exist for TESTABILITY
     */
    private static void doNothing() {
    }
