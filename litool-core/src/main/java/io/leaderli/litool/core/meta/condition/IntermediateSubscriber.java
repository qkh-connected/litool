package io.leaderli.litool.core.meta.condition;

/**
 * @author leaderli
 * @since 2022/6/22
 */
abstract class IntermediateSubscriber<T, R> implements Subscriber<T, R>, Subscription {
    protected final Subscriber<T, R> actualSubscriber;
    private Subscription prevSubscription;

    protected IntermediateSubscriber(Subscriber<T, R> actualSubscriber) {
        this.actualSubscriber = actualSubscriber;
    }

    @Override
    public void onSubscribe(Subscription prevSubscription) {
        this.prevSubscription = prevSubscription;
        actualSubscriber.onSubscribe(this);
    }

    @Override
    public void next(T t) {
        this.actualSubscriber.next(t);
    }

    @Override
    public void onComplete(R value) {
        this.actualSubscriber.onComplete(value);
    }

    @Override
    public void request() {
        this.prevSubscription.request();
    }

}
