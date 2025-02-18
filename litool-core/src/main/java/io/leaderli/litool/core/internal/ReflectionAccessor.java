/*
 * Copyright (C) 2017 The Gson authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.leaderli.litool.core.internal;

import java.lang.reflect.AccessibleObject;

/**
 * Provides a replacement for {@link AccessibleObject#setAccessible(boolean)}, which may be used to
 * avoid reflective access issues appeared in Java 9
 * thrown or warnings like
 * <pre>
 *   WARNING: An illegal reflective access operation has occurred
 *   WARNING: Illegal reflective access by ...
 * </pre>
 * Works both for Java 9 and earlier Java versions.
 */
public abstract class ReflectionAccessor {

    // the singleton instance, use getInstance() to obtain

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    private static final ReflectionAccessor instance = JavaVersion.getMajorJavaVersion() < 9 ?
            new PreJava9ReflectionAccessor() : new UnsafeReflectionAccessor();

    /**
     * Obtains a {@link ReflectionAccessor} instance suitable for the current Java version.
     * <p>
     * In such a case, use {@link ReflectionAccessor#makeAccessible(AccessibleObject)} on a field, method or constructor
     * (instead of basic {@link AccessibleObject#setAccessible(boolean)}).
     *
     * @return reflect accessor
     */
    public static ReflectionAccessor getInstance() {
        return instance;
    }

    /**
     * Does the same as {@code ao.setAccessible(true)}
     *
     * @param ao accessible obj
     */
    public abstract void makeAccessible(AccessibleObject ao);
}
