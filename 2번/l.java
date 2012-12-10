
    protected void onTimeout(Context context, Intent intent) {
    }

    protected void onSubscriptionTimeout(Context context, Intent intent) {
    }

    protected abstract String getServiceId();

    protected abstract void onSubscribed(String targetId, Context context, Intent intent);

    protected abstract void onUnsubscribed(Context context, Intent intent);

