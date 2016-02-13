/*
 * Copyright (C) 2011 The Android Open Source Project
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
package org.telegram.messenger.Animation;

/**
 * Android 14 以上的 NoSuchPropertyException
 * 自定义 No Such Property Exception
 * 在ReflectiveProperty的实例方法内抛出
 */
public class NoSuchPropertyException extends RuntimeException {

    public NoSuchPropertyException(String s) {
        super(s);
    }

}
