package io.leaderli.litool.core.meta.condition;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * the node provide a target value  by {@link  #mapper}
 * it only perform on prev node is test {@code  true}
 *
 * @param <T> the declare type of source value
 * @param <M> the  real type of source value  {@link  LiIf#_instanceof(Class)}
 * @param <R> the type of target value
 * @author leaderli
 * @see LiThen#then(Supplier)
 * @see LiThen#then(Function)
 * @see LiThen#then(Object)
 * @since 2022/7/17
 */
class FulfillNode<T, M extends T, R> extends Node<T, R> {

    private final Publisher<T, R> prevPublisher;
    private final Function<? super M, ? extends R> mapper;


    public FulfillNode(Publisher<T, R> prevPublisher, Function<? super M, ? extends R> mapper) {
        this.prevPublisher = prevPublisher;
        this.mapper = mapper;
    }


    @Override
    public void subscribe(Subscriber<? super T, R> actualSubscriber) {
        prevPublisher.subscribe(new SubscriberCase(actualSubscriber));

    }

    private class SubscriberCase extends IntermediateSubscriber<T, R> {

        public SubscriberCase(Subscriber<? super T, R> actualSubscriber) {
            super(actualSubscriber);

        }

        @SuppressWarnings("unchecked")
        @Override
        public void apply(T t) {
            R apply = mapper.apply((M) t);
            this.onComplete(apply);

        }
    }
}
