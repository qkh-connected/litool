package io.leaderli.litool.core.type;

import io.leaderli.litool.core.collection.CollectionUtils;
import io.leaderli.litool.core.exception.LiAssertUtil;
import io.leaderli.litool.core.io.FileNameUtil;
import io.leaderli.litool.core.io.FileUtil;
import io.leaderli.litool.core.meta.LiTuple;
import io.leaderli.litool.core.meta.LiTuple2;
import io.leaderli.litool.core.meta.Lino;
import io.leaderli.litool.core.meta.Lira;
import io.leaderli.litool.core.util.ObjectsUtil;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author leaderli
 * @since 2022-01-22
 */
public class ClassUtil {


    /**
     * Return the assumed class of instance , null-safe.
     * <p>
     * the assumed class may not the real class of instance,  it
     * may only the super class of instance, and it can not be used
     * as  assumed class.
     * eg:
     * <pre>
     * {@code
     *
     *  Class<CharSequence> type = ClassUtil.getClass("");
     *  type == CharSequence // false
     * }
     * </pre>
     * type cannot be used as CharSequence
     *
     * @param def the instance
     * @param <T> the type of instance class
     * @return the assumed class of instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getDeclaringClass(T def) {
        if (def == null) {
            return null;
        }
        return (Class<T>) def.getClass();
    }


    /**
     * narrow wild generic type to  specifier generic type
     *
     * @param cls the class
     * @param <T> the type parameter of class or it's superclass
     * @return {@code Class<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> narrow(final Class<? extends T> cls) {

        return (Class<T>) cls;
    }

    /**
     * Return primitive class  when class is wrapper class and is not
     * array class, otherwise  return it self.
     *
     * <p>
     * it's null-safe
     *
     * @param cls the class to be convert
     * @return the converted class
     */
    public static Class<?> wrapperToPrimitive(final Class<?> cls) {

        if (cls != null && !cls.isArray()) {
            Class<?> convertedClass = PrimitiveEnum.WRAPPER_PRIMITIVE_MAP.get(cls);
            if (convertedClass != null) {
                return convertedClass;
            }
        }
        return cls;
    }

    /**
     * The type of an array consisting of the elements of instance of the class
     *
     * @param type a class
     * @return type of an array consisting of the elements of
     */
    public static Class<?> getArrayClass(Class<?> type) {


        return Array.newInstance(type, 0).getClass();
    }

    /**
     * @return get all jar file path under classPath
     */
    public static List<String> getAppJars() {

        return getJavaClassPaths().filter(f -> f.endsWith(FileNameUtil.EXT_JAR)).get();

    }

    /**
     * @return get all classPath except jre
     */
    public static Lira<String> getJavaClassPaths() {
        return Lira.of(System.getProperty("java.class.path").split(System.getProperty("path.separator"))).map(path -> path.replace(File.separatorChar, '/'));
    }

    /**
     * Return whether class is primitive or wrapper
     *
     * @param cls the class
     * @return whether class is primitive or wrapper
     */
    public static boolean isPrimitiveOrWrapper(Class<?> cls) {

        return PrimitiveEnum.PRIMITIVE_WRAPPER_MAP.containsKey(cls) || PrimitiveEnum.PRIMITIVE_WRAPPER_MAP.containsValue(cls);
    }

    /**
     * Return the componentType of obj class, it's null-safe
     *
     * @param obj the obj
     * @return the componentType of obj class
     */
    public static Class<?> getComponentType(Object obj) {

        if (obj == null) {
            return null;
        }
        return obj.getClass().getComponentType();
    }


    /**
     * bfs order
     * <p>
     * exclude {@link  Serializable}, {@link Cloneable}.
     * <p>
     * the primitive or array just return {@code  new Class[]{Object.class}}
     *
     * @param cls the class
     * @return the superClass and interface recursive
     */
    public static Class<?>[] getSuperTypeAndInterfacesRecursively(Class<?> cls) {

        Objects.requireNonNull(cls);
        Set<ObjectPriority<Class<?>>> visit = new HashSet<>();
        findSuperType(cls, visit, 0);
        if (cls.isPrimitive() || cls.isArray()) {
            return new Class[]{Object.class};
        }
        return Lira.of(visit)
                .sorted(Comparator.comparingInt(ObjectPriority::getPriority))
                .map(ObjectPriority::getObject)
                .terminal(list -> {
                    list.remove(cls);
                    list.remove(Serializable.class);
                    list.remove(Cloneable.class);
                })
                .toArray(Class.class);
    }

    private static void findSuperType(Class<?> cls, Set<ObjectPriority<Class<?>>> visit, int level) {


        ObjectPriority<Class<?>> add = new ObjectPriority<>(cls, level * 10 + (cls.isInterface() ? 1 : 0));
        if (visit.contains(add)) {
            return;
        }
        level++;
        visit.add(add);
        if (cls == Object.class) {
            return;
        }

        // bfs order
        Class<?> superclass = cls.getSuperclass();
        if (superclass != null) {
            findSuperType(superclass, visit, level);
        }

        for (Class<?> anInterface : cls.getInterfaces()) {
            findSuperType(anInterface, visit, level);
        }

    }

    /**
     * @param a class a
     * @param b class b
     * @return get two class most recent inheritance, if any one is null, return the other
     */
    public static Class<?> getRecentlyInheritance(Class<?> a, Class<?> b) {

        if (a == null) {
            if (b == null) {
                return Object.class;
            }
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a == Object.class || b == Object.class) {
            return Object.class;
        }
        if (a == b) {
            return a;
        }
        if (a.isPrimitive() || b.isPrimitive()) {
            return Object.class;
        }
        if (a.isArray()) {
            if (b.isArray()) {
                return getArrayClass(getRecentlyInheritance(a.getComponentType(), b.getComponentType()));
            }
            return Object.class;
        }
        if (b.isArray()) {
            return Object.class;
        }

        Class<?>[] aList = getSuperTypeAndInterfacesRecursively(a);
        Class<?>[] bList = getSuperTypeAndInterfacesRecursively(b);


        return Lira.of(CollectionUtils.intersection(aList, bList)).first().get(Object.class);
    }

    /**
     * if all element is null, return Object.class
     *
     * @param arr the arr
     * @return get array elements most recent inheritance, if element is null, just ignore it
     */
    public static Class<?> getRecentlyInheritance(Object[] arr) {
        if (arr == null || arr.length == 0) {
            return Object.class;
        }
        Class<?> result = null;

        for (Object o : arr) {
            if (o != null) {
                Class<?> elementType = o.getClass();

                if (elementType == null) {
                    continue;
                }
                result = getRecentlyInheritance(result, elementType);

                if (result == Object.class) {
                    break;
                }

            }
        }

        if (result == null) {
            result = Object.class;
        }
        return result;

    }

    /**
     * Return a specified length array, if the componentType is primitive, will
     * convert to it's wrapper
     *
     * @param componentType the class of  new array elements
     * @param length        the length of new array
     * @param <T>           the type of new array elements
     * @return a specified length array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newWrapperArray(Class<? extends T> componentType, int length) {
        return (T[]) Array.newInstance(primitiveToWrapper(componentType), length);
    }

    /**
     * Return wrapper class  when class is primitive class and is not
     * array class, otherwise  return it self.
     *
     * <p>
     * it's null-safe
     *
     * @param cls the class to be convert
     * @return the converted class
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {

        if (cls != null && !cls.isArray()) {
            Class<?> convertedClass = PrimitiveEnum.PRIMITIVE_WRAPPER_MAP.get(cls);
            if (convertedClass != null) {
                return convertedClass;
            }
        }
        return cls;

    }

    /**
     * Return return the box value
     *
     * @param origin obj
     * @return return the box value
     */
    public static Object box(Object origin) {
        return origin;
    }

    /**
     * Return the map  cast key and value type and filter the element can not cast
     *
     * @param map       An map object
     * @param keyType   the type of map key can cast to
     * @param valueType the type of map value can cast to
     * @param <K>       the type parameter of  keyType
     * @param <V>       the type parameter of valueType
     * @return the map  cast key and value type and filter the element can not cast
     */
    public static <K, V> Map<K, V> filterCanCast(Map<?, ?> map, Class<? extends K> keyType,
                                                 Class<? extends V> valueType) {

        if (map == null || keyType == null || valueType == null) {
            return new HashMap<>();
        }
        return map.entrySet().stream().map(entry -> {

            K k = cast(entry.getKey(), keyType);
            V v = cast(entry.getValue(), valueType);
            return LiTuple.of(k, v);
        }).filter(LiTuple2::notIncludeNull).collect(Collectors.toMap(tu -> tu._1, tu -> tu._2));
    }

    /**
     * Return casted instance, if obj can not cast will return {@code null}
     * return wrapper  if obj is primitive and not array
     *
     * @param obj      obj
     * @param castType the class that obj can cast
     * @param <T>      the type parameter of  castType
     * @return casted instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> castType) {

        if (obj == null || castType == null) {
            return null;
        }

        if (isAssignableFromOrIsWrapper(castType, obj.getClass())) {

            return (T) obj;
        }

        return null;
    }

    /**
     * Return {@code  son} is instanceof or wrapper of {@code  father}. if {@code  son} , {@code  father}
     * is array class, because wrapper array cannot cast to primitive ,so judge  {@code son} componentType
     * is instanceof {@code father} componentType
     *
     * @param father the super class  or wrapper class
     * @param son    the sub class or primitive class
     * @return {@code  son} is instanceof or wrapper of {@code  father}
     */
    public static boolean isAssignableFromOrIsWrapper(Class<?> father, Class<?> son) {

        if (father == null || son == null) {
            return false;
        }


        if (father.isArray()) {

            if (son.isArray()) {

                father = father.getComponentType();
                son = son.getComponentType();
                // the primitive array cannot cast to wrapper array
                if (father.isPrimitive() || son.isPrimitive()) {
                    return father == son;
                }
                return father.isAssignableFrom(son);
            }
        } else {

            if (father.isAssignableFrom(son)) {
                return true;
            }
            return primitiveToWrapper(son) == primitiveToWrapper(father);
        }
        return false;
    }

    /**
     * Return {@code  son} is instanceof or wrapper of {@code  father}. if {@code  son} , {@code  father}
     * is array class, because wrapper array cannot cast to primitive ,so judge  {@code son} componentType
     * is instanceof {@code father} componentType
     *
     * @param son    the sub instance
     * @param father the super class  or wrapper class
     * @return {@code  son} is instanceof or wrapper of {@code  father}
     */
    public static boolean _instanceof(Object son, Class<?> father) {

        if (father == null || son == null) {
            return false;
        }

        return isAssignableFromOrIsWrapper(father, son.getClass());

    }

    /**
     * Return the primitive value converted  by double value
     * <p>
     * support byte,boolean,char,float,double,long,int,short
     *
     * @param d             a double value
     * @param primitiveEnum a primitive enum
     * @return convert double value to other primitive value
     */
    public static Object castDouble(Double d, PrimitiveEnum primitiveEnum) {
        switch (primitiveEnum) {
            case BYTE:
                return d.byteValue();
            case BOOLEAN:
                return d != 0;
            case CHAR:
                return (char) (double) d;
            case FLOAT:
                return d.floatValue();
            case DOUBLE:
            case OBJECT:
                return d;
            case LONG:
                return d.longValue();
            case INT:
                return d.intValue();
            case SHORT:
                return d.shortValue();
            default:
                throw new IllegalStateException();
        }
    }

    private static int rank(Class<?> sub, Class<?> sup, int rank) {

        if (sub == sup) {
            return rank;
        }
        if (sub == null || sup == null || !sup.isAssignableFrom(sub)) {
            return -1;
        }
        if (sup.isInterface()) {

            Lino<Class<?>> first = Lira.of(sub.getInterfaces()).filter(sup::isAssignableFrom).first();
            if (first.present()) {
                return rank(first.get(), sup, rank + 1);
            }
        }
        return rank(sub.getSuperclass(), sup, rank + 1);
    }

    /**
     * @param sub the sub class
     * @param sup the sup class
     * @param <T> the type of sup class
     * @return the rank of sub to sup, return 0 if sub == sup
     */
    public static <T> int rank(Class<? extends T> sub, Class<T> sup) {


        ObjectsUtil.requireNotNull(sub, sup);

        return rank(sub, sup, 0);
    }

    /**
     * @param sub the sub class
     * @param sup the sup class
     * @return the rank of sub to sup, return 0 if sub == sup or sub,sup is no inheritance relationship
     */
    public static int rank0(Class<?> sub, Class<?> sup) {

        ObjectsUtil.requireNotNull(sub, sup);

        if (sup.isAssignableFrom(sub)) {
            return rank(sub, sup, 0);
        }
        if (sub.isAssignableFrom(sup)) {
            return -rank(sup, sub, 0);
        }
        return 0;

    }

    /**
     * Return  a proxy that add the interface to obj, the interface method will invoke the obj
     * same signature method.
     * <p>
     * when the interface is generic, will invoke generic-erasure  method
     *
     * @param _interface a interface
     * @param obj        the real instance that actually execute method
     * @param <T>        the type of interface
     * @return a instance  declare as interface
     * @throws io.leaderli.litool.core.exception.AssertException if _interface  is not interface
     * @see MethodUtil#getSameSignatureMethod(Object, Method)
     */
    public static <T> T addInterface(Class<T> _interface, Object obj) {

        LiAssertUtil.assertTrue(_interface.isInterface(), "only support interface");

        InvocationHandler invocationHandler = (proxy, method, params) ->
                MethodUtil.getSameSignatureMethod(obj, method)
                        .throwable_map(m -> m.invoke(obj, params), Throwable::printStackTrace)
                        .get();
        Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{_interface},
                invocationHandler);
        return _interface.cast(proxy);
    }

    /**
     * @param constructor the constructor
     * @param <T>         the type of constructor
     * @return the declare class of constructor
     */
    public static <T> Class<T> getDeclaringClass(Constructor<T> constructor) {

        if (constructor == null) {
            return null;
        }
        return constructor.getDeclaringClass();
    }

    /**
     * @param method the  method
     * @return the declare class of method
     */
    public static Class<?> getDeclaringClass(Method method) {
        if (method == null) {
            return null;
        }
        return method.getDeclaringClass();
    }

    /**
     * @param field the  field
     * @return the declare class of field
     */
    public static Class<?> getDeclaringClass(Field field) {
        if (field == null) {
            return null;
        }
        return field.getDeclaringClass();
    }

    /**
     * @param field the field
     * @return the type of field
     */
    public static Class<?> getType(Field field) {
        if (field == null) {
            return null;
        }
        return field.getType();
    }

    /**
     * @param method the method
     * @return the type of  method return
     */
    public static Class<?> getType(Method method) {
        if (method == null) {
            return null;
        }
        return method.getReturnType();
    }

    /**
     * @param cls the class
     * @return return the class jar file
     */
    public static Lino<File> getJarFile(Class<?> cls) {

        return Lino.of(cls)
                .map(Class::getProtectionDomain)
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .filter(l -> FileUtil.FILE_PROTOCOL.equals(l.getProtocol()) && l.toString().endsWith(FileNameUtil.EXT_JAR))
                .map(l -> {

                    try {
                        return new File(l.toURI());
                    } catch (URISyntaxException e) {
                        return new File(l.getPath());
                    }
                });
    }
}
