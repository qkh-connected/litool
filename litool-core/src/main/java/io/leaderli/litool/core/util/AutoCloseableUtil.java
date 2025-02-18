package io.leaderli.litool.core.util;

import io.leaderli.litool.core.function.ThrowableConsumer;
import io.leaderli.litool.core.function.ThrowableFunction;
import io.leaderli.litool.core.function.ThrowableSupplier;

/**
 * a util provide a normalize closeable resource action
 *
 * @author leaderli
 * @since 2022/9/5
 */
public class AutoCloseableUtil {


    /**
     * use try-resource style code to auto-invoke  {@link  AutoCloseable#close()} after
     * acton has performed
     *
     * @param supplier a supplier provide  {@link  AutoCloseable} instance
     * @param consumer a  consumer
     * @param <T>      the type that extends {@link  AutoCloseable}
     */
    public static <T extends AutoCloseable> void closeableConsumer(ThrowableSupplier<T> supplier,
                                                                   ThrowableConsumer<T> consumer) {

        try (T closeable = supplier.get()) {
            consumer.accept(closeable);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * use try-resource style code to auto-invoke  {@link  AutoCloseable#close()} after
     * acton has performed
     *
     * @param supplier a supplier provide  {@link  AutoCloseable} instance
     * @param function a function
     * @param <T>      the type that extends {@link  AutoCloseable}
     * @param <R>      the type of  function provide
     * @return the function  result
     */
    public static <T extends AutoCloseable, R> R closeableFunction(ThrowableSupplier<T> supplier,
                                                                   ThrowableFunction<T, R> function) {

        try (T closeable = supplier.get()) {
            return function.apply(closeable);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
