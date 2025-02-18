package io.leaderli.litool.core.meta;

import io.leaderli.litool.core.collection.*;
import io.leaderli.litool.core.function.ThrowableConsumer;
import io.leaderli.litool.core.function.ThrowableFunction;
import io.leaderli.litool.core.lang.EqualComparator;
import io.leaderli.litool.core.meta.ra.*;
import io.leaderli.litool.core.type.ClassUtil;
import io.leaderli.litool.core.type.LiTypeToken;
import io.leaderli.litool.core.util.BooleanUtil;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * a collection provide some convenient method, the action only execute when the terminal action are performed
 *
 * <p>
 * lira should not use  the tool class that contain lira, such as {@link  io.leaderli.litool.core.bit.BitStr}
 * <p>
 * lira chain will catch all exception to perform an {@link  Exceptionable#onError(Throwable, CancelSubscription)}
 * action, you can listen exception on {@link #onError(Exceptionable)}, and rethrow the exception
 * to interrupt the chain with a runtimeException, or just cancel the chain by  {@link  CancelSubscription#cancel()}.
 * to be convenient, you can use {@link  #assertNoError()} or {@link  #assertTrue(Function)} to interrupt the chain
 * manually with runtimeException. or throw a specific {@link LiraRuntimeException}
 * <p>
 * most terminal action will remove null element
 *
 * @author leaderli
 * @since 2022/6/19
 */
public interface Lira<T> extends LiValue, PublisherRa<T>, Iterable<T> {


    Lira<?> NONE_INSTANCE = new IterableRa<>(NoneItr.of());

    /**
     * Returns the narrow type lira, convert {@code <? extends T>} to {@code  <T>}, same as {@link  Lino#narrow(Lino)}
     *
     * @param lira the lira with wild type
     * @param <T>  the type of the lira
     * @return the narrow type lira
     */
    @SuppressWarnings("unchecked")
    static <T> Lira<T> narrow(Lira<? extends T> lira) {

        return (Lira<T>) lira;

    }

    /**
     * Returns the new lira consisting the elements
     *
     * @param elements an array
     * @param <T>      the type of array
     * @return the new lira
     */
    @SafeVarargs
    static <T> Lira<T> of(T... elements) {

        return of(IterableItr.ofs(elements));

    }

    /**
     * Returns an lira that consisting elements provided by given stream
     *
     * @param stream the stream that provide  elements
     * @param <T>    the type of elements returned by this stream
     * @return the new lira
     */
    static <T> Lira<T> of(Stream<? extends T> stream) {

        return of(IterableItr.of(stream));

    }

    /**
     * Returns the unique none lira that not consisting any element
     *
     * @param <T> the type of lira
     * @return the unique  none lira
     * @see #NONE_INSTANCE
     */
    @SuppressWarnings("unchecked")
    static <T> Lira<T> none() {
        return (Lira<T>) NONE_INSTANCE;

    }

    /**
     * Returns an lira that consisting elements provided by given iterableItr
     *
     * @param iterableItr the iterableItr that provide  elements
     * @param <T>         the type of elements returned by this iterableItr
     * @return the new lira
     */
    static <T> Lira<T> of(IterableItr<? extends T> iterableItr) {

        if (iterableItr != null && iterableItr.hasNext()) {

            return new IterableRa<>(iterableItr);
        }
        return none();
    }

    /**
     * Returns an lira that consisting elements provided by given obj
     *
     * @param obj the obj
     * @param <T> the type of elements returned by this obj
     * @return the new lira
     * @see IterableItr#of(Object)
     */
    static <T> Lira<T> iterableItr(Object obj) {
        return of(IterableItr.of(obj));

    }

    /**
     * @return the lira of a infinity generator of auto-increment integer start with 0
     * @see Generators#range()
     */
    static Lira<Integer> range() {
        return Lira.of(Generators.range());
    }

    /**
     * Returns an lira that consisting elements provided by given iterator
     *
     * @param iterator the iterator that provide  elements
     * @param <T>      the type of elements returned by this iterator
     * @return the new lira
     */
    static <T> Lira<T> of(Iterator<? extends T> iterator) {

        return of(IterableItr.of(iterator));

    }

    /**
     * Returns an lira that consisting elements provided by given iterable
     *
     * @param iterable the iterator that provide  elements
     * @param <T>      the type of elements returned by this iterable
     * @return the new lira
     */
    static <T> Lira<T> of(Iterable<? extends T> iterable) {

        if (iterable == null || !iterable.iterator().hasNext()) {
            return none();
        }
        return new IterableRa<>(iterable);
    }

    /**
     * filter element that can be cast to map , and  the map key, value can be cast.
     * if the element is null, will keep it
     *
     * @param <K>       the generic parameter of casted map key type
     * @param <V>       the generic parameter of casted map value type
     * @param keyType   the type of casted map key
     * @param valueType the type of casted map value
     * @return the new lira
     */
    <K, V> Lira<Map<K, V>> cast(Class<? extends K> keyType, Class<? extends V> valueType);

    /**
     * Returns if this lira contains the specified element
     * <p>
     * it's terminal action
     *
     * @param t element whose presence in this lira is to be tested
     * @return if this lira contains the specified element
     */
    boolean contains(T t);

    /**
     * remove element that equals with
     *
     * @param t the removed element
     * @return a new lira
     * @see #filter(Function)
     * @see #filter_null()
     */
    default Lira<T> remove(T t) {

        if (t == null) {
            return filter_null();
        }
        return filter(v -> !t.equals(v));
    }

    /**
     * filter the element beyond lira, the element will remain if  the result.{@link  Function#apply(Object)}
     * parsed  by {@link  BooleanUtil#parse(Boolean)} is {@code  true}
     * <p>
     * on the default the null element will be removed, except the filter is instance of {@link NullableFunction}
     *
     * @param filter the filter function
     * @return a new lira
     * @see BooleanUtil#parse(Object)
     */
    Lira<T> filter(Function<? super T, ?> filter);

    /**
     * remove null element
     *
     * @return a new lira
     * @see #filter(Function)
     */
    Lira<T> filter_null();

    /**
     * if lira {@link  #present()} return the lino of first element, otherwise return {@link  Lino#none()}
     * <p>
     * it's terminal action
     *
     * @return the first element  under lira
     */
    Lino<T> first();


    /**
     * if lira {@link  #present()} after filter return the lino of first element, otherwise return {@link  Lino#none()}
     * <p>
     * it's terminal action
     *
     * <pre>
     *    return  filter(filter).first()
     * </pre>
     *
     * @param filter the filter function
     * @return the first element  under lira
     */
    default Lino<T> first(Function<? super T, ?> filter) {
        return filter(filter).first();
    }

    /**
     * if lira {@link  #present()} after filter return the lino of first last, otherwise return {@link  Lino#none()}
     * <p>
     * it's terminal action
     *
     * <pre>
     *    return  filter(filter).last()
     * </pre>
     *
     * @param filter the filter function
     * @return the last element  under lira
     */
    default Lino<T> last(Function<? super T, ?> filter) {
        return filter(filter).last();
    }

    /**
     * if lira {@link  #present()} return the lino of last element, otherwise return {@link  Lino#none()}
     * <p>
     * it's terminal action
     *
     * @return the last element  under lira
     */
    Lino<T> last();

    /**
     * if lira element at index is exists return the lino of index element, otherwise return
     * {@link  Lino#none()}
     * <p>
     * index support negative , indicate calculating the position calculated from the back, if
     * the negative is outbound, it will also return {@link  Lino#none()}
     *
     * @param index the index of element
     * @return the lino of index element
     */
    Lino<T> get(int index);

    /**
     * a terminal action
     *
     * @return the iterator of lira that has removed element of {@code null}
     */
    @Override
    Iterator<T> iterator();

    /**
     * a terminal action
     *
     * @param consumer The action to be performed for each element
     */
    @Override
    void forEach(Consumer<? super T> consumer);

    /**
     * a terminal action
     *
     * @param consumer The action to be performed for each element that may contain element of {@code null}
     */
    void forNullableEach(Consumer<? super T> consumer);

    /**
     * a terminal action
     *
     * @return the iterator of lira that may contain element of {@code null}
     */
    Iterator<T> nullableIterator();

    /**
     * will only remain at most n element, should be aware of
     * the {@link Generator} will drop the limit element. And
     * and the null element will counted as valid limit element,
     * but the terminal action will drop the null-element.
     * eg:
     * <pre>
     *     Lira.of(1,null,2,3).limit(3).size() == 2
     * </pre>
     * <p>
     * to prevent that situation, you can code this way
     * <pre>
     *     Lira.of(1,null,2,3).filter_null().limit(3).size() == 3
     * </pre>
     *
     * @param max the limit of max element
     * @return a new lira
     */
    Lira<T> limit(int max);

    /**
     * use {@link IterableItr#of(Object)} as iterator mapper
     *
     * @param <R> the type of the flatted lira
     * @return a new lira
     * @see #flatMap(Function)
     */
    <R> Lira<R> flatMap();

    /**
     * @param mapper the function that accept element and provide iterator
     * @param <R>    the type of the flatted lira
     * @return a new lira
     * @see #flatMap(Function)
     */
    <R> Lira<R> flatMap(Function<? super T, Iterator<? extends R>> mapper);

    /**
     * <pre>
     *     return map(mapper).map(Supplier::get)
     * </pre>
     *
     * @param mapper the function accept element and return lino
     * @param <R>    the type of after unzip
     * @return a new lira of type R
     */
    default <R> Lira<R> unzip(Function<? super T, Supplier<? extends R>> mapper) {
        return map(mapper).map(Supplier::get);
    }

    /**
     * @param mapper the  mapper
     * @param <R>    the type of after mapper
     * @return a new lira of type R
     */
    <R> Lira<R> map(Function<? super T, ? extends R> mapper);

    /**
     * when the error occurs will  perform {@link  SubscriberRa#next_null()} and
     * {@link SubscriberRa#onError(Throwable, CancelSubscription)}
     *
     * @param mapper the  mapper
     * @param <R>    the type of after mapper
     * @return a new lira of type R
     * @see #throwable_map(ThrowableFunction, Consumer)
     * @see LiConstant#WHEN_THROW
     */
    <R> Lira<R> throwable_map(ThrowableFunction<? super T, ? extends R> mapper);

    /**
     * when the error occurs will  perform {@link  SubscriberRa#next_null()} and
     * {@link SubscriberRa#onError(Throwable, CancelSubscription)}
     *
     * @param mapper    the  mapper
     * @param <R>       the type of after mapper
     * @param whenThrow the consumer when {@link  ThrowableFunction#apply(Object)} throw
     * @return a new lira of type R
     */
    <R> Lira<R> throwable_map(ThrowableFunction<? super T, ? extends R> mapper, Consumer<Throwable> whenThrow);

    /**
     * a terminal action, trigger the lira to execute and store the element
     * at list
     *
     * @param deliverAction the consumer perform on the cache list
     * @return a new lira
     */
    Lira<T> terminal(Function<List<T>, Iterable<T>> deliverAction);

    /**
     * a terminal action, trigger the perform a action prev elements
     *
     * @param deliverAction the consumer perform on the cache list
     * @return a new lira
     * @see #terminal(Function)
     */
    default Lira<T> terminal(Consumer<List<T>> deliverAction) {
        return terminal(list -> {
            deliverAction.accept(list);
            return list;
        });
    }

    /**
     * consumer the elements until filter is satisfied
     *
     * @param filter cancel signal push
     * @return a new lira
     * @see BooleanUtil#parse(Object)
     */

    Lira<T> takeWhile(Function<? super T, ?> filter);

    /**
     * @param filter drop element util filter is satisfied
     * @return a new lira
     * @see BooleanUtil#parse(Object)
     */
    Lira<T> dropWhile(Function<? super T, ?> filter);

    default Lira<T> nullable(T supplier) {
        return nullable(() -> supplier);
    }

    /**
     * if element is null will converted to a element by this supplier.
     * if element is not null, just deliver it self
     *
     * @param supplier provide a  new element
     * @return a result if call {@link  io.leaderli.litool.core.meta.ra.SubscriberRa#next_null()}
     */
    Lira<T> nullable(Supplier<? extends T> supplier);

    /**
     * skip the first n element, only  accept the element after min
     *
     * @param min skip the first n element
     * @return a new lira
     */
    Lira<T> skip(int min);

    default Lira<T> assertTrue(Function<? super T, ?> filter) {
        return assertTrue(filter, "");
    }

    default Lira<T> assertTrue(Function<? super T, ?> filter, String err) {
        return assertTrue(filter, () -> err);
    }

    default Lira<T> assertTrue(Function<? super T, ?> filter, Supplier<String> errMsg) {
        return debug(new DebugConsumer<T>() {

            @Override
            public void accept(T e) {
                if (!BooleanUtil.parse(filter.apply(e))) {
                    throw new LiraRuntimeException("assert fail of " + filter + " : " + errMsg.get());
                }
            }

            @Override
            public void onNull() {
                throw new LiraRuntimeException("assert fail of " + filter);
            }
        });
    }

    /**
     * if element is not null return {@link  Either#right(Object)}
     * otherwise return {@link  Either#left(Object)}
     *
     * @param l   left value
     * @param <L> the type of left value
     * @return convert the lira value to   either value
     */
    default <L> Lira<Either<L, T>> either(L l) {
        return eitherSupplier(() -> l);
    }

    /**
     * if element is not null return {@link  Either#right(Object)}
     * otherwise return {@link  Either#left(Object)}
     *
     * @param l   left value provider
     * @param <L> the type of left value
     * @return convert the lira value to   either value
     */
    <L> Lira<Either<L, T>> eitherSupplier(Supplier<? extends L> l);

    /**
     * Performs an action  for each element of this lira
     *
     * @param action a  action to perform on the elements
     * @return this
     */
    Lira<T> debug(DebugConsumer<T> action);

    default Lira<T> assertNoError() {
        return onError((t, c) -> {
            throw new IllegalStateException(t);
        });
    }

    /**
     * if lira throw a exception when perform an action on the element, the lira will not call
     * {@link SubscriberRa#next(Object)}, replaced it will call {@link  SubscriberRa#next_null()}
     * and {@link  SubscriberRa#onError(Throwable, CancelSubscription)}. eventually the error will
     * deliver to {@link  Exceptionable#onError(Throwable, CancelSubscription)}
     *
     * @param onError a onError action
     * @return a new lira
     */
    Lira<T> onError(Exceptionable onError);

    /**
     * a terminal action, will call {@link  #present()} to judge the element at here
     * whether present. if present just return this, or return a {@link  #of(Object[])}
     *
     * @param alternate the arr
     * @return lira
     */
    @SuppressWarnings("unchecked")
    Lira<T> or(T... alternate);

    /**
     * a terminal action, will call {@link  #present()} to judge the element at here
     * whether present. if present just return this, or return a {@link  #of(Iterator)} }
     *
     * @param alternate the iterator
     * @return lira
     */
    Lira<T> or(Iterator<? extends T> alternate);

    /**
     * a terminal action, will call {@link  #present()} to judge the element at here
     * whether present. if present just return this, or return a {@link  #of(Iterable)} }
     *
     * @param alternate the iterable
     * @return lira
     */
    Lira<T> or(Iterable<? extends T> alternate);

    /**
     * Performs a reduction on the element of this lira, using the associative accumulation functions,
     * and returns the reduced value. this is equivalent to:
     * <pre> {@code
     *     T result =  null
     *     for (T element : this stream){
     *          if(result == null){
     *              result = element;
     *          }else{
     *             result = accumulator.apply(result, element)
     *             if(result == null){
     *                 return null;
     *             }
     *          }
     *     }
     *     return result;
     * }</pre>
     *
     * <p>This is a terminal operation
     *
     * @param accumulator an associative non-interfering stateless function for combining two values
     * @return the result of reduction
     */
    Lino<T> reduce(BinaryOperator<T> accumulator);

    /**
     * Performs a reduction on the element of this lira, using the associative accumulation functions,
     * and returns the reduced value. this is equivalent to:
     * <pre> {@code
     *     T result =  identify
     *     for (T element : this stream){
     *             result = accumulator.apply(result, element)
     *             if(result == null){
     *                 return null;
     *             }
     *     }
     *     return result;
     * }</pre>
     *
     * <p>This is a terminal operation
     *
     * @param identity    the identity value for the accumulating function
     * @param accumulator an associative non-interfering stateless function for combining two values
     * @return the result of reduction
     */
    Lino<T> reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * a terminal action
     *
     * @return the number of elements in this list
     */
    int size();

    /**
     * Performs an {@code System.out.println(element)} for each element of this lira
     *
     * @return this
     * @see #debug(DebugConsumer)
     */
    Lira<T> debug();

    /**
     * just sleep a while when perform on this action
     *
     * @param milliseconds sleep duration
     * @return a new lira
     */

    default Lira<T> sleep(long milliseconds) {
        return sleep(1, milliseconds);
    }

    /**
     * just sleep a while when perform on this action on every n time
     *
     * @param countdown    the count
     * @param milliseconds sleep duration
     * @return a new lira
     */


    Lira<T> sleep(int countdown, long milliseconds);

    /**
     * use {@link  Objects#equals(Object, Object)} as {@link  EqualComparator}
     *
     * @return a new lira
     * @see #distinct(EqualComparator)
     * @see Objects#equals(Object, Object)
     * @see EqualComparator
     */
    default Lira<T> distinct() {
        return distinct(Objects::equals);
    }

    /**
     * Return this that consisting of the distinct elements according to {@code EqualComparator}
     * <p>
     * it's a middle terminal action
     *
     * @param comparator a {@code EqualComparator}  to be used to compare lira elements is equals
     * @return a new lira
     * @see #terminal(Function)
     */
    Lira<T> distinct(EqualComparator<? super T> comparator);

    /**
     * use default {@link  Comparator}, {@code  {sorted(null)}}
     *
     * @return the new lira
     * @see #terminal(Function)
     */
    default Lira<T> sorted() {
        return sorted(null);
    }

    /**
     * Returns a lira consisting of the elements of this lira ,sorted  according to  the provided {@code Comparator}
     * <p>
     * it's a middle terminal action
     *
     * @param comparator the provided {@link Comparator}
     * @return the new lira
     * @see #terminal(Function)
     */
    Lira<T> sorted(Comparator<? super T> comparator);

    /**
     * use {@link  LiConstant#WHEN_THROW} as error consumer
     * <p>
     * This is a terminal operation
     * <p>
     * when exception occur , it will perform Throwable on {@link LiConstant#WHEN_THROW} action
     *
     * @param action a  catchable action to perform on the elements
     */
    void forThrowableEach(ThrowableConsumer<? super T> action);

    /**
     * <p>This is a terminal operation
     *
     * @param action    a  catchable action to perform on the elements
     * @param whenThrow use to perform on {@code action} throw an Throwable
     */
    void forThrowableEach(ThrowableConsumer<? super T> action, Consumer<Throwable> whenThrow);

    /**
     * it's a terminal action
     *
     * @return the new stream
     * @see Stream
     */
    default Stream<T> stream() {
        return get().stream();
    }

    /**
     * Returns an typed array containing the typed elements in this lira, if the element is not satisfied
     * the type will be removed
     *
     * <pre>
     *     cast(type).get().toArray(new T[0])
     * </pre>
     *
     * <p>This is a terminal operation
     *
     * @param type the array type
     * @param <R>  the type of arr
     * @return an array containing the elements in this lira
     */
    default <R> R[] toArray(Class<R> type) {

        Object[] original = cast(type).toArray();

        R[] copy = ClassUtil.newWrapperArray(type, original.length);
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }


    /**
     * return an {@code List} consisting of the elements
     * <p>
     * it's a terminal action
     *
     * @return the new list
     * @deprecated use {@link #get()}
     */
    @Deprecated
    default List<T> getRaw() {
        return get();
    }

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a terminal operation
     *
     * @return an array containing the elements of this  lira
     * @throws ClassCastException if element is empty and T is not Object
     */
    default T[] toArray() {
        return ArrayUtils.toArray(get().toArray());
    }

    /**
     * return an {@code List} consisting of the elements exclude null element
     * <p>
     * it's a terminal action
     *
     * @return the new list
     */
    List<T> get();

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a nullable terminal operation
     *
     * @return an array containing the elements of this  lira
     * @throws ClassCastException if element is empty and T is not Object
     */
    default T[] toNullableArray() {
        return ArrayUtils.toArray(nullableGet().toArray());
    }

    /**
     * return an {@code List} consisting of the elements include null element
     * <p>
     * it's a terminal action
     *
     * @return the new list
     */
    List<T> nullableGet();

    /**
     * Returns an typed array containing the typed elements in this lira, if the element is not satisfied
     * the type will be removed
     *
     * <pre>
     *     cast(type).nullableGet().toArray(new T[0])
     * </pre>
     *
     * <p>This is a nullable terminal operation
     *
     * @param type the array type
     * @param <R>  the type parameter of arr
     * @return an array containing the elements in this lira
     */
    default <R> R[] toNullableArray(Class<R> type) {

        Object[] original = cast(type).toNullableArray();
        R[] copy = ClassUtil.newWrapperArray(type, original.length);
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }

    <R> Lira<LiTuple2<T, R>> tuple(Function<? super T, ? extends R> mapper);


    /**
     * filter element that can be cast, if the element is null, will keep it
     *
     * @param type the class or interface represented by elements
     * @param <R>  the generic parameter of {@code type}
     * @return this
     */
    <R> Lira<R> cast(Class<? extends R> type);

    /**
     * filter element that can be cast, if the element is null, will keep it
     * <p>
     * the typeToken is for generic use, the real cast class is {@link LiTypeToken#getRawType()}
     *
     * @param typeToken the typeToken of cast class
     * @param <R>       the generic parameter of {@code typeToken}
     * @return this
     * @see #cast(Class)
     */

    @SuppressWarnings("unchecked")
    default <R> Lira<R> cast(LiTypeToken<R> typeToken) {
        return (Lira<R>) cast(typeToken.getRawType());
    }

    /**
     * Returns a map  consisting of the results of applying the given function to the elements of this stream
     * using {@code keyMapper.apply(element), valueMapper.apply(element)} as key, value
     *
     * <p> the {@code key == null } entry will be  removed
     *
     * <p>This is a terminal operation
     *
     * @param keyMapper   a stateless function apply to each element
     * @param valueMapper a stateless function apply to each element
     * @param <K>         the type of  {@code Map} key
     * @param <V>         the type of  {@code Map} value
     * @return the new map
     */

    <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper);

    /**
     * Returns a map  consisting of the results of applying the given function to the elements of this stream
     * using {@code LiTuple2.-1, LiTuple2.-2} as key, value
     *
     * <p> the {@code key == null } entry will be  removed
     *
     * <p>This is a terminal operation
     *
     * @param mapper a stateless function apply to each element
     * @param <K>    the type of  {@code Map} key
     * @param <V>    the type of  {@code Map} value
     * @return the new map
     */
    <K, V> Map<K, V> toMap(Function<? super T, LiTuple2<? extends K, ? extends V>> mapper);


}
