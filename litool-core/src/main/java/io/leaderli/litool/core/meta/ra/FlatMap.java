package io.leaderli.litool.core.meta.ra;

import io.leaderli.litool.core.exception.InfinityException;

import java.util.Iterator;
import java.util.function.Function;

/**
 * @author leaderli
 * @since 2022/7/18
 */
public class FlatMap<T, R> extends Some<R> {


    private final Function<? super T, Iterator<? extends R>> mapper;
    private final Publisher<T> prevPublisher;

    public FlatMap(Publisher<T> prevPublisher, Function<? super T, Iterator<? extends R>> mapper) {
        this.prevPublisher = prevPublisher;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Subscriber<? super R> actualSubscriber) {
        prevPublisher.subscribe(new FlatMapSubscriberSubscription(actualSubscriber));

    }


    class FlatMapSubscriberSubscription extends IntermediateSubscriberSubscription<T, R> {


        protected Subscription barricadeSubscription;
        private int actual_states;


        private FlatMapSubscriberSubscription(Subscriber<? super R> actualSubscriber) {
            super(actualSubscriber);
        }

        @Override
        public void request(int state) {
            actual_states = state;
            if (LiraBit.isTerminal(state)) {
                super.request(state);
            } else {

                if (barricadeSubscription == null) {
                    super.request(state);
                } else {
                    barricadeSubscription.request(state);
                }
            }
        }

        @Override
        public void next_null() {

            // not provide an iterator to init a new  barricadeSubscription
        }

        @Override
        public void onComplete() {
            if (this.barricadeSubscription == null) {
                this.actualSubscriber.onComplete();
            } else {
                this.barricadeSubscription = null;
            }
        }

        @Override
        public void next(T t) {

            Iterator<? extends R> iterator = mapper.apply(t);
            if (iterator == null || !iterator.hasNext()) {
                return;
            }
            if (LiraBit.isTerminal(actual_states)) {
                iterator.forEachRemaining(e -> SubscriberUtil.next(actualSubscriber, e));
            } else {
                barricadeSubscription = new BarricadeGenerator(this, actualSubscriber, iterator);
                barricadeSubscription.request(actual_states);

            }

        }

        /**
         * @author leaderli
         * @since 2022/7/16
         */
        public final class BarricadeGenerator extends GeneratorSubscription<R> {


            public BarricadeGenerator(Completable mediate, Subscriber<? super R> actualSubscriber,
                                      Iterator<? extends R> iterator) {
                super(actualSubscriber, iterator);
            }


            @SuppressWarnings("java:S2583")
            @Override
            public void request(int state) {
                LiraBit bit = LiraBit.of(state);
                if (bit.have(LiraBit.DROP)) {
                    if (drop_count-- == 0) {
                        throw new InfinityException("generator arrived max drop \r\n\tat " + iterator);
                    }
                }
                if (LiraBit.isTerminal(state)) {
                    throw new IllegalStateException("barricade in flat map not support terminal request");
                }

                performRequest();

            }

            private void performRequest() {
                if (completed) {
                    // just tell  barricade is completed, not tell actualSubscriber
                    onComplete();
                    return;
                }
                if (iterator.hasNext()) {
                    try {
                        SubscriberUtil.next(actualSubscriber, iterator.next());
                    } catch (Throwable throwable) {
                        actualSubscriber.next_null();
                        actualSubscriber.onError(throwable, this);
                    }
                } else {
                    completed = true;
                    onComplete();
                }
            }

        }
    }
}
