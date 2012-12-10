
    @Override
    public final void onReceive(Context context, Intent intent) {
        onBroadcastReceiving(context, intent);

        Log.d(LOG_TAG, "broadcast received");
        String intentAction = intent.getAction();

        if (intentAction.equals(NPushIntent.RESPONSE_SUBSCRIBE_INTENT)) {
            doConcurrentSubscription(context, intent);

        } else if (intentAction.equals(NPushIntent.RESPONSE_UNSUBSCRIBE_INTENT)) {
            // unsubscribed (cancellation of subscription)
            doConcurrentUnsubscription(context, intent);

        } else if (severalSystemEventsRaised(intentAction)) {
            Log.d(LOG_TAG, "system event related to sustain service is raised");
            onSeveralSystemEventRaised(context, intent);

            // keep service running if developer wants
            if (backgroundServiceNeverDie()) {
                Log.d(LOG_TAG, "user wants to keep NNI service running");
                NPushMessaging.requestCheckKeepAlive(context, getServiceId());
            }

        } else if (intentAction.equals(NPushIntent.INFORM_STATUS_INTENT)) {
            // received something related to server status
            Log.d(LOG_TAG, "server status has been changed");
            whenStatusChanged(context, intent);

        } else if (intentAction.equals(NPushIntent.NOTIFICATION_RECEIVE_INTENT)) {
            // message received!
            Log.d(LOG_TAG, "notification message has been received");
            onMessageReceived(context, intent);

        } else if (intentAction.equals(PweNniIntent.TIMEOUT_INTENT)) {
            // timeout!
            Log.d(LOG_TAG, "time out");
            whenTimeout(context, intent);
        }

        onBroadcastReceived(context, intent);
    }
