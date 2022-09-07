package io.leaderli.litool.core.meta.condition;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 该类与前一个节点配合使用
 *
 * @see LiIf
 */
@FunctionalInterface
public interface LiThen<T, M extends T, R> extends Publisher<T, R> {


    /**
     * @param mapping 转换函数，当条件满足时执行
     * @return 返回一个新的 LiIf ,  以方便链式调用
     */
    default LiIf<T, R> then(Function<? super M, ? extends R> mapping) {

        return new FulfillNode<>(this, mapping);
    }

    /**
     * @param supplier 当条件满足时提供值
     * @return 返回一个新的 LiIf ,  以方便链式调用
     */
    default LiIf<T, R> then(Supplier<R> supplier) {

        return new FulfillNode<>(this, v -> supplier.get());
    }

    /**
     * @param value 当条件满足时保存的值
     * @return 返回一个新的 LiIf ,  以方便链式调用
     */
    default LiIf<T, R> then(R value) {

        return new FulfillNode<>(this, v -> value);
    }

    /**
     * @param consumer 消费者，当条件满足时执行，不需要保存结构，会存储为 null
     * @return 返回一个新的 LiIf ,  以方便链式调用
     */
    @SuppressWarnings("unchecked")
    default LiIf<T, R> then(Consumer<? super M> consumer) {

        return new FulfillNode<>(this, v -> {
            consumer.accept((M) v);
            return null;
        });
    }


}
