/*
 * Copyright (C) 2014 Alexey Illarionov
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
package com.SecUpwN.AIMSICD.rilexecutor;

public class DetectResult {

    public final boolean available;

    public final String error;

    static final DetectResult AVAILABLE = new DetectResult(true, null);

    private DetectResult(boolean available, String error) {
        this.available = available;
        this.error = error;
    }

    static DetectResult Unavailable(String error) {
        return new DetectResult(false, error);
    }
}
