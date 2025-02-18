package io.leaderli.litool.core.type;


import io.leaderli.litool.core.proxy.DynamicDelegation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * used for delegate un-strict method, when the method with strict-type cannot found,
 * the method with this annotation and  returnType, parameterTypes can casted from origin method
 * will used to delegate origin method
 *
 * @see ReflectUtil#newInterfaceImpl(LiTypeToken, DynamicDelegation)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RuntimeType {
}
