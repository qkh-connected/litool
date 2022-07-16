package io.leaderli.litool.core.meta.reactor;

import io.leaderli.litool.core.exception.LiThrowableConsumer;
import io.leaderli.litool.core.exception.LiThrowableFunction;
import io.leaderli.litool.core.meta.LiBox;
import io.leaderli.litool.core.meta.LiConstant;
import io.leaderli.litool.core.meta.Lino;
import io.leaderli.litool.core.meta.Lira;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link Lira} 的有值实现类，采用响应式编程思路，仅在实际获取值的方法操作中才会对底层元素进行各种操作，也可以使用 {@link #eager()} 来提前触发动作
 *
 * @author leaderli
 * @since 2022/6/28
 */
public abstract class SomeRa<T> implements Lira<T> {


    @Override
    public boolean present() {
        return first().present();
    }

    @Override
    public String name() {
        return "List";
    }


    @Override
    public <R> Lira<R> cast(Class<R> type) {
        return map(m -> Lino.of(m).cast(type).get());

    }

    @Override
    public <K, V> Lira<Map<K, V>> cast(Class<K> keyType, Class<V> valueType) {
        return map(m -> Lino.of(m).cast(keyType, valueType).get());
    }

    @Override
    public Lira<T> filter(Function<? super T, Object> filter) {
        return new FilterRa<>(this, filter);

    }

    @Override
    public Lira<T> filter() {
        return filter(null);
    }

    @Override
    public Lino<T> first() {
        LiBox<T> value = LiBox.none();

        this.subscribe(new BiConsumerSubscriberRa<>((v, s) -> {
            value.value(v.get());
            s.cancel();
        }));
        return value.lino();
    }

    @Override
    public Lino<T> last() {

        LiBox<T> value = LiBox.none();

        this.subscribe(new ConsumerSubscriberRa<>(v -> value.value(v.get())));
        return value.lino();
    }

    @Override
    public Lino<T> first(Function<? super T, Object> filter) {
        return filter(filter).first();
    }

    @Override
    public void forEachLino(Consumer<Lino<T>> consumer) {
        this.subscribe(new ConsumerSubscriberRa<>(consumer));
    }


    @Override
    public void forEach(Consumer<T> consumer) {
        this.subscribe(new ConsumerSubscriberRa<>((v) -> consumer.accept(v.get())));
    }


    @Override
    public void forThrowableEach(LiThrowableConsumer<T> consumer) {
        forThrowableEach(consumer, Throwable::printStackTrace);
    }

    @Override
    public void forThrowableEach(LiThrowableConsumer<T> consumer, Consumer<Throwable> whenThrow) {

        LiThrowableConsumer<T> finalConsumer = consumer == null ? t -> {
        } : consumer;
        this.subscribe(new SubscriberRa<T>() {

            @Override
            public void onSubscribe(SubscriptionRa prevSubscription) {
                prevSubscription.request();

            }

            @Override
            public void next(Lino<? extends T> t) {
                try {
                    finalConsumer.accept(t.get());
                } catch (Throwable e) {
                    whenThrow.accept(e);
                }

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public List<Lino<T>> get() {
        List<Lino<T>> result = new ArrayList<>();
        this.subscribe(new ConsumerSubscriberRa<>(result::add));

        return result;
    }


    @Override
    public Lino<T> get(int index) {

        if (index < 0) {
            return Lino.none();
        }
        LiBox<Integer> count = LiBox.of(0);
        LiBox<T> result = LiBox.none();
        this.subscribe(new SubscriberRa<T>() {
            private SubscriptionRa prevSubscription;

            @Override
            public void onSubscribe(SubscriptionRa prevSubscription) {
                this.prevSubscription = prevSubscription;
                prevSubscription.request();
            }

            @Override
            public void next(Lino<? extends T> t) {

                if (count.value() == index) {
                    result.value(t.get());
                    prevSubscription.cancel();
                }
                count.value(count.value() + 1);

            }

        });

        return result.lino();
    }

    @Override
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapping, Function<? super T, ? extends V> valueMapping) {

        Map<K, V> result = new HashMap<>();

        this.subscribe(new ConsumerSubscriberRa<>(e -> e.map(keyMapping).ifPresent(key -> result.put(key, e.map(valueMapping).get()))));

        return result;
    }

    @Override
    public Lira<T> limit(int n) {
        if (n >= 0) {
            return new LimitRa<>(this, n);
        }
        return this;
    }

    @Override
    public List<T> getRaw() {

        List<T> result = new ArrayList<>();
        this.subscribe(new ConsumerSubscriberRa<>(v -> result.add(v.get())));
        return result;
    }

    @Override
    public <R> Lira<R> map(Function<? super T, ? extends R> mapping) {
        return new MapRa<>(this, mapping);

    }

    @Override
    public Lira<T> skip(int n) {
        if (n > 0) {
            return new SkipRa<>(this, n);
        }

        return this;
    }

    @Override
    public Lira<T> sort() {
        return sort(null);
    }

    @Override
    public Lira<T> sort(Comparator<? super T> comparator) {

        List<T> raw = getRaw();
        raw.sort(comparator);
        return Lira.of(raw);
    }

    @Override
    public <R> Lira<R> throwable_map(LiThrowableFunction<? super T, ? extends R> mapping) {
        return new ThrowableMapRa<>(this, mapping, LiConstant.WHEN_THROW);


    }

    @Override
    public <R> Lira<R> throwable_map(LiThrowableFunction<? super T, ? extends R> mapping, Consumer<Throwable> whenThrow) {
        return new ThrowableMapRa<>(this, mapping, whenThrow);

    }

    @Override
    public Lira<T> eager() {
        return Lira.of(getRaw());
    }

    @SafeVarargs
    @Override
    public final Lira<T> or(T... others) {

        return or(Arrays.asList(others));
    }

    @Override
    public Lira<T> or(Iterator<T> others) {
        List<T> raw = getRaw();
        if (raw.isEmpty()) {
            return Lira.of(others);
        }
        return Lira.of(raw);
    }

    @Override
    public Lira<T> or(Iterable<T> others) {
        return or(others.iterator());
    }


    @Override
    public int size() {
        return this.get().size();
    }


}
