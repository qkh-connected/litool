package io.leaderli.litool.core.meta.ra;

import io.leaderli.litool.core.lang.DisposableRunnableProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author leaderli
 * @since 2022/9/1
 */
class TerminalRa<T> extends RaWithPrevPublisher<T> {

    private final Function<List<T>, Iterable<T>> deliverAction;


    public TerminalRa(PublisherRa<T> prevPublisher, Function<List<T>, Iterable<T>> deliverAction) {
        super(prevPublisher);
        Objects.requireNonNull(deliverAction);
        this.deliverAction = deliverAction;
    }

    @Override
    public void subscribe(SubscriberRa<? super T> actualSubscriber) {
        prevPublisher.subscribe(new TerminalSubscriberSubscription(actualSubscriber));
    }


    private final class TerminalSubscriberSubscription implements SubscriberRa<T>, SubscriptionRa {


        private final SubscriberRa<? super T> actualSubscriber;
        private final List<T> cache = new ArrayList<>();
        SubscriptionRa prevSubscription;
        private final Runnable disposable = DisposableRunnableProxy.of(() -> prevSubscription.request(0));
        private SubscriptionRa terminalSubscription;

        private TerminalSubscriberSubscription(SubscriberRa<? super T> actualSubscriber) {
            this.actualSubscriber = actualSubscriber;
        }


        @Override
        public void request(int state) {
            disposable.run();
            terminalSubscription.request(state);
        }

        @Override
        public void cancel() {
            // terminal don not accept cancel  signal
        }

        @Override
        public void onSubscribe(SubscriptionRa prevSubscription) {
            this.prevSubscription = prevSubscription;
            actualSubscriber.onSubscribe(this);
        }

        @Override
        public void next(T t) {
            this.cache.add(t);
        }

        @Override
        public void next_null() {
            this.cache.add(null);
        }

        @Override
        public void onComplete() {
            completeTerminal();
        }

        void completeTerminal() {

            if (deliverAction != null) {
                terminalSubscription = new IterableRa<>(deliverAction.apply(cache)).newGenerator(actualSubscriber);
            } else {
                terminalSubscription = new IterableRa<>(cache).newGenerator(actualSubscriber);
            }
        }

        @Override
        public void onCancel() {
            completeTerminal();
        }

    }

}
