/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.leaderli.litool.core.lang.lean;


import io.leaderli.litool.core.type.LiTypeToken;

/**
 * The interface Type adapter factory.
 */
public interface TypeAdapterFactory {

    /**
     * Returns a type adapter for {@code type}, or null if this factory doesn't
     * support {@code type}.
     *
     * @param <T>  the type parameter
     * @param lean the lean
     * @param type the type
     * @return the type adapter
     */
    <T> TypeAdapter<T> create(Lean lean, LiTypeToken<T> type);
}
