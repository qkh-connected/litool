package io.leaderli.litool.core.type;

import io.leaderli.litool.core.collection.ArrayUtils;
import io.leaderli.litool.core.collection.CollectionUtils;
import io.leaderli.litool.core.exception.AssertException;
import io.leaderli.litool.core.meta.LiValue;
import io.leaderli.litool.core.meta.Lino;
import io.leaderli.litool.core.meta.Lira;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author leaderli
 * @since 2022/6/17
 */
@SuppressWarnings("ConstantConditions")
class ClassUtilTest {


    @Test
    void getJarFile() {

        Assertions.assertNotNull(ClassUtil.getJarFile(Assertions.class).get());
        Assertions.assertNull(ClassUtil.getJarFile(ClassUtil.class).get());
    }

    @Test
    void getSuperTypeAndInterfacesRecursively() {

        Assertions.assertArrayEquals(new Class[]{Object.class}, ClassUtil.getSuperTypeAndInterfacesRecursively(int[].class));
        Assertions.assertArrayEquals(new Class[]{Object.class}, ClassUtil.getSuperTypeAndInterfacesRecursively(int.class));
        Assertions.assertArrayEquals(new Class[0], ClassUtil.getSuperTypeAndInterfacesRecursively(Object.class));
        Assertions.assertEquals(Number.class, ClassUtil.getSuperTypeAndInterfacesRecursively(Integer.class)[0]);
    }

    @Test
    void getRecentlyInheritance() {
        // interface
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(String.class, List.class));
        Assertions.assertSame(AbstractList.class, ClassUtil.getRecentlyInheritance(LinkedList.class, ArrayList.class));
        Assertions.assertSame(Collection.class, ClassUtil.getRecentlyInheritance(Queue.class, ArrayList.class));
        Assertions.assertSame(Collection[].class, ClassUtil.getRecentlyInheritance(Queue[].class, ArrayList[].class));

        // a b
        Assertions.assertSame(int.class, ClassUtil.getRecentlyInheritance(int.class, int.class));
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(int.class, double.class));
        Assertions.assertSame(int[].class, ClassUtil.getRecentlyInheritance(int[].class, int[].class));
        Assertions.assertSame(Object[].class, ClassUtil.getRecentlyInheritance(int[].class, double[].class));
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(null, null));
        Assertions.assertSame(Number.class, ClassUtil.getRecentlyInheritance(Integer.class, Double.class));
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(Integer.class, Double[].class));
        Assertions.assertSame(Number[].class, ClassUtil.getRecentlyInheritance(Integer[].class, Double[].class));
        Assertions.assertSame(Number[][].class, ClassUtil.getRecentlyInheritance(Integer[][].class, Double[][].class));
        Assertions.assertSame(Comparable[][].class, ClassUtil.getRecentlyInheritance(Integer[][].class, String[][].class));

        // arr
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(ArrayUtils.of()));
        Assertions.assertSame(Integer.class, ClassUtil.getRecentlyInheritance(ArrayUtils.of(1, 2, null)));
        Assertions.assertSame(Number.class, ClassUtil.getRecentlyInheritance(ArrayUtils.of(1, 2, 1.0, null)));
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(ArrayUtils.of(1, 2, 1.0, true)));
        Assertions.assertSame(Integer[].class, ClassUtil.getRecentlyInheritance(ArrayUtils.of(ArrayUtils.of(1, 2), ArrayUtils.of(3, 4))));
        Integer[] of1 = ArrayUtils.of(1, 2);
        Double[] of2 = ArrayUtils.of(3.0, 4.0);
        Number[][] of = ArrayUtils.of(of1, of2);
        Assertions.assertSame(Number[].class, ClassUtil.getRecentlyInheritance(of));
        Assertions.assertSame(Object.class, ClassUtil.getRecentlyInheritance(new Object[]{null, null, null}));
    }

    @Test
    void castDouble() {

        Assertions.assertEquals(Byte.class, ClassUtil.castDouble(1d, PrimitiveEnum.BYTE).getClass());
        Assertions.assertEquals(Float.class, ClassUtil.castDouble(1d, PrimitiveEnum.FLOAT).getClass());
        Assertions.assertEquals(Boolean.class, ClassUtil.castDouble(1d, PrimitiveEnum.BOOLEAN).getClass());
        Assertions.assertEquals(true, ClassUtil.castDouble(1d, PrimitiveEnum.BOOLEAN));
        Assertions.assertEquals(false, ClassUtil.castDouble(0d, PrimitiveEnum.BOOLEAN));
        Assertions.assertEquals(Character.class, ClassUtil.castDouble(1d, PrimitiveEnum.CHAR).getClass());
        Assertions.assertEquals(Double.class, ClassUtil.castDouble(1d, PrimitiveEnum.DOUBLE).getClass());
        Assertions.assertEquals(Long.class, ClassUtil.castDouble(1d, PrimitiveEnum.LONG).getClass());
        Assertions.assertEquals(Integer.class, ClassUtil.castDouble(1d, PrimitiveEnum.INT).getClass());
        Assertions.assertEquals(Short.class, ClassUtil.castDouble(1d, PrimitiveEnum.SHORT).getClass());

    }

    @Test
    void getComponentType() {

        Assertions.assertNull(ClassUtil.getComponentType(null));
        Assertions.assertEquals(Integer.class, ClassUtil.getComponentType(new Integer[]{}));
        Object[] objects = new Integer[]{};
        Assertions.assertEquals(Integer.class, ClassUtil.getComponentType(objects));

    }

    @Test
    void testGetClass() throws NoSuchFieldException, NoSuchMethodException {

        //noinspection ConstantConditions
        Assertions.assertNull(ClassUtil.getDeclaringClass((Method) null));

        Assertions.assertEquals(Integer.class, ClassUtil.getDeclaringClass(1));
        Class<CharSequence> type = ClassUtil.getDeclaringClass("");
        Assertions.assertNotSame(type, CharSequence.class);
        Assertions.assertSame(type, String.class);


        assertEquals(String.class, ClassUtil.getType(ReflectUtilTest.LittleBean.class.getDeclaredField("name")));
        assertEquals(int.class, ClassUtil.getType(ReflectUtilTest.LittleBean.class.getDeclaredField("age")));
        assertNull(ClassUtil.getType((Field) null));
        assertEquals(void.class, ClassUtil.getType(ReflectUtilTest.LittleBean.class.getDeclaredMethod("m3")));

        assertEquals(ReflectUtilTest.LittleBean.class,
                ClassUtil.getDeclaringClass(ReflectUtilTest.LittleBean.class.getDeclaredField("age")));
        assertEquals(ReflectUtilTest.LittleBean.class,
                ClassUtil.getDeclaringClass(ReflectUtilTest.LittleBean.class.getDeclaredMethod("m3")));
        assertEquals(ReflectUtilTest.LittleBean.class,
                ClassUtil.getDeclaringClass(ReflectUtilTest.LittleBean.class.getConstructor()));


    }

    @Test
    void box() {

        int a = 1;
        Assertions.assertEquals(Integer.class, ClassUtil.box(a).getClass());
        Object obj = a;
        Assertions.assertEquals(Integer.class, ClassUtil.box(obj).getClass());

    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    void narrow() {


        Assertions.assertDoesNotThrow(() -> {
            Class<CharSequence> narrow = ClassUtil.narrow(String.class);
            narrow = ClassUtil.narrow(null);
        });
    }

    @Test
    void primitiveToWrapper() {
        Assertions.assertNull(ClassUtil.primitiveToWrapper(null));
        Assertions.assertSame(ClassUtil.primitiveToWrapper(int.class), Integer.class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(void.class), Void.class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(String.class), String.class);

        Assertions.assertSame(ClassUtil.primitiveToWrapper(int[].class), int[].class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(int[][].class), int[][].class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(Integer[].class), Integer[].class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(Integer[][].class), Integer[][].class);
        Assertions.assertSame(ClassUtil.primitiveToWrapper(String[].class), String[].class);

    }

    @Test
    void wrapperToPrimitive() {
        Assertions.assertNull(ClassUtil.wrapperToPrimitive(null));
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(Integer.class), int.class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(Void.class), void.class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(String.class), String.class);

        Assertions.assertSame(ClassUtil.wrapperToPrimitive(int[].class), int[].class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(int[][].class), int[][].class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(Integer[].class), Integer[].class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(Integer[][].class), Integer[][].class);
        Assertions.assertSame(ClassUtil.wrapperToPrimitive(String[].class), String[].class);

    }

    @Test
    void isAssignableFromOrIsWrapper() {

        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(null, null));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(null, String.class));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(String.class, null));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(String.class, CharSequence.class));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(String[].class, CharSequence.class));

        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(int.class, Integer.class));
        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(Integer.class, int.class));
        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(CharSequence.class, String.class));
        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(CharSequence[].class, String[].class));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(int[].class, Integer[].class));
        Assertions.assertFalse(ClassUtil.isAssignableFromOrIsWrapper(int[][].class, Integer[][].class));
        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(int[].class, int[].class));


        Assertions.assertFalse(int[].class.isAssignableFrom(Integer[].class));
        Assertions.assertTrue(Integer[].class.isAssignableFrom(Integer[].class));
        Assertions.assertFalse(Integer[].class.isAssignableFrom(int[].class));


        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(List.class, ArrayList.class));


    }

    @Test
    void _instanceof() {

        Assertions.assertFalse(ClassUtil._instanceof(null, null));
        Assertions.assertFalse(ClassUtil._instanceof(null, Integer.class));
        Assertions.assertFalse(ClassUtil._instanceof(1, null));

        Assertions.assertTrue(ClassUtil._instanceof(1, Integer.class));
        Assertions.assertTrue(ClassUtil._instanceof(1, int.class));
        Assertions.assertTrue(ClassUtil._instanceof(1, Number.class));

        Assertions.assertFalse(ClassUtil._instanceof(1, String.class));

        Assertions.assertTrue(ClassUtil._instanceof(new int[]{1}, int[].class));
        Assertions.assertFalse(ClassUtil._instanceof(new Integer[]{1}, int[].class));
        Assertions.assertFalse(ClassUtil._instanceof(new int[]{1}, Integer[].class));
        Assertions.assertTrue(ClassUtil._instanceof(new String[]{"1"}, CharSequence[].class));

    }

    @Test
    void getArrayClass() {

        Assertions.assertSame(ClassUtil.getArrayClass(int.class), int[].class);
        Assertions.assertSame(ClassUtil.getArrayClass(Integer.class), Integer[].class);
        Assertions.assertSame(ClassUtil.getArrayClass(int[].class), int[][].class);
        Assertions.assertThrows(NullPointerException.class, () -> ClassUtil.getArrayClass(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ClassUtil.getArrayClass(void.class));
    }

    @Test
    void getAppJars() {

        Assertions.assertTrue("file:/".matches("^[^/]++/$"));
        Assertions.assertTrue("jar:file:/".matches("^[^/]++/$"));
        Assertions.assertFalse("/jar/".matches("^[^/]++/$"));
        Assertions.assertTrue(ClassUtil.getAppJars().size() > 0);

        Lira<String> a = ClassUtil.getJavaClassPaths();
        Lira<String> b = Lira.of(ClassUtil.getAppJars());


    }


    @Test
    void newWrapperArray() {


        Assertions.assertSame(Integer[].class, ClassUtil.newWrapperArray(Integer.class, 0).getClass());
        Assertions.assertSame(Integer[].class, ClassUtil.newWrapperArray(int.class, 0).getClass());
        Assertions.assertSame(int[][].class, ClassUtil.newWrapperArray(int[].class, 0).getClass());
        Assertions.assertSame(Integer[][].class, ClassUtil.newWrapperArray(Integer[].class, 0).getClass());

        CharSequence[] strings = ClassUtil.newWrapperArray(String.class, 1);
        Assertions.assertEquals(1, strings.length);

        Assertions.assertTrue(ClassUtil.isAssignableFromOrIsWrapper(CharSequence[].class,
                ClassUtil.newWrapperArray(String.class, 0).getClass()));


        Assertions.assertThrows(NullPointerException.class, () -> ClassUtil.newWrapperArray(null, 0));


    }

    @Test
    void toArray() {

        Assertions.assertNull(CollectionUtils.toArray(null));
        Assertions.assertNull(CollectionUtils.toArray(1));
        Assertions.assertArrayEquals(new Integer[]{1}, CollectionUtils.toArray(new int[]{1}));
        Assertions.assertArrayEquals(new Integer[]{1}, CollectionUtils.toArray(new Integer[]{1}));

    }


    @Test
    void cast() {

        Object a = "123";
        Assertions.assertEquals("123", ClassUtil.cast(a, String.class));
        Assertions.assertEquals("123", ClassUtil.cast(a, CharSequence.class));
        Assertions.assertNull(ClassUtil.cast(a, int.class));


        a = 1;
        Assertions.assertSame(Integer.class, a.getClass());
        Assertions.assertSame(Integer.class, ClassUtil.cast(a, Integer.class).getClass());
        Assertions.assertSame(Integer.class, ClassUtil.cast(a, int.class).getClass());

        Assertions.assertSame(Integer.class, ClassUtil.cast(a, Integer.class).getClass());
        Assertions.assertSame(Integer.class, ClassUtil.cast(a, int.class).getClass());

        a = new int[]{1};

        Assertions.assertSame(int[].class, ClassUtil.cast(a, int[].class).getClass());
        Assertions.assertNull(ClassUtil.cast(a, Integer[].class));
        Assertions.assertEquals(1, ClassUtil.cast(a, int[].class)[0]);

        a = new String[]{"1"};

        Assertions.assertNull(ClassUtil.cast(a, Integer[].class));
        Assertions.assertSame(String[].class, ClassUtil.cast(a, String[].class).getClass());
        CharSequence[] cs = ClassUtil.cast(a, CharSequence[].class);
        Assertions.assertEquals(1, cs.length);
        Object[] cast = ClassUtil.cast(a, Object[].class);
        Assertions.assertEquals("1", cast[0]);


        Type genericInterface = Param.class.getGenericInterfaces()[0];

        Assertions.assertNull(Lino.of(String.class).cast(ParameterizedType.class).get());

    }

    @Test
    void filterCanCastMap() {

        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "1");
        map.put(2, 2);
        Assertions.assertEquals(1, ClassUtil.filterCanCast(map, String.class, String.class).size());
        Assertions.assertEquals(1, ClassUtil.filterCanCast(map, int.class, int.class).size());
        Assertions.assertEquals(0, ClassUtil.filterCanCast(map, String.class, int.class).size());

        Assertions.assertDoesNotThrow(() -> {
            Map<CharSequence, Number> actual = ClassUtil.filterCanCast(map, String.class, int.class);
        });
    }

    @Test
    void addInterface() {

        Assertions.assertThrows(AssertException.class, () -> ClassUtil.addInterface(Proxy.class, new Proxy()));


        ClassUtil.addInterface(Runnable.class, 1);

        MyFunction function = ClassUtil.addInterface(MyFunction.class, new Proxy());

        Assertions.assertSame(123, function.apply("123"));
        Function<String, Integer> function2 = ClassUtil.addInterface(MyFunction.class, new Proxy());
        Assertions.assertSame(123, function2.apply("123"));

    }


    @SuppressWarnings("rawtypes")
    @Test
    void rank() {

        Assertions.assertThrows(NullPointerException.class, () -> ClassUtil.rank(null, null));
        Assertions.assertEquals(1, ClassUtil.rank(Lino.class, LiValue.class));
        Assertions.assertEquals(1, ClassUtil.rank(ArrayList.class, List.class));
        Assertions.assertEquals(3, ClassUtil.rank(ArrayList.class, Iterable.class));
        Assertions.assertEquals(3, ClassUtil.rank(ArrayList.class, Object.class));

        Assertions.assertEquals(2, ClassUtil.rank(C.class, A.class));
        Assertions.assertEquals(2, ClassUtil.rank(C.class, Object.class));


        Lira<Class<? extends Collection>> a = Lira.of(Collection.class, List.class, AbstractList.class, ArrayList.class);
        Lira<Class<? extends Collection>> b = Lira.of(List.class, ArrayList.class, Collection.class, AbstractList.class)
                .sorted(ClassUtil::rank0);
        Assertions.assertEquals(a, b);

    }

    @Test
    void isPrimitiveOrWrapper() {
        Assertions.assertFalse(ClassUtil.isPrimitiveOrWrapper(null));
        Assertions.assertFalse(ClassUtil.isPrimitiveOrWrapper(Object.class));
        Assertions.assertTrue(ClassUtil.isPrimitiveOrWrapper(int.class));
        Assertions.assertTrue(ClassUtil.isPrimitiveOrWrapper(Integer.class));
        Assertions.assertTrue(ClassUtil.isPrimitiveOrWrapper(void.class));
        Assertions.assertTrue(ClassUtil.isPrimitiveOrWrapper(Void.class));
    }

    private interface A {
    }

    @FunctionalInterface
    public interface Fuck extends Function<String, Integer> {

    }

    interface MyFunction extends Function<String, Integer> {

        @Override
        default Integer apply(String s) {
            return null;
        }
    }

    private static class B implements A {
    }

    private static class C extends B {
    }

    public static class Proxy {

        public Object apply(Object s) {
            return apply((String) s);
        }

        public Integer apply(String s) {
            return Integer.valueOf(s);
        }

    }

    private static class Param implements Function<String, String> {

        @Override
        public String apply(String s) {
            return null;
        }
    }

}
