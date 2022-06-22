/*
 * Copyright 2022 OPPO Goblin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oitstack.goblin.runtime.utils;

import java.util.Locale;

/**
 * @Author CuttleFish
 * @Date 2022/3/1 下午8:24
 */
public class PlatformUtils {

    public static final String OS_NAME = "os.name";
    public static final String WIN = "win";
    public static final String MAC = "mac os x";


    public static final boolean IS_WINDOWS = PropertyAndEnvUtils.get(OS_NAME, "")
            .toLowerCase(Locale.US)
            .contains(WIN);
    public static final boolean IS_MAC =
            PropertyAndEnvUtils.get(OS_NAME, "")
                    .toLowerCase(Locale.US)
                    .contains(MAC);

    public static final boolean IS_IBM_MACHINE = "OS/390".equals(PropertyAndEnvUtils.get(OS_NAME, "")) ||
            "z/OS".equals(PropertyAndEnvUtils.get(OS_NAME, "")) ||
            "zOS".equals(PropertyAndEnvUtils.get(OS_NAME, ""));


}
