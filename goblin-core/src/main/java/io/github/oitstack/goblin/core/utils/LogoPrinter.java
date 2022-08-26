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
package io.github.oitstack.goblin.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print project info.
 */
public class LogoPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoPrinter.class);

    public static void print() {
        StringBuilder str = new StringBuilder("\n");
        str.append("  _____         _      _  _          _______          _ \n");
        str.append(" / ____|       | |    | |(_)        |__   __|        | |\n");
        str.append("| |  __   ___  | |__  | | _  _ __      | |  ___  ___ | |_ \n");
        str.append("| | |_ | / _ \\ | '_ \\ | || || '_ \\     | | / _ \\/ __|| __|\n");
        str.append("| |__| || (_) || |_) || || || | | |    | ||  __/\\__ \\| |_ \n");
        str.append(" \\_____| \\___/ |_.__/ |_||_||_| |_|    |_| \\___||___/ \\__|\n\n");
        LOGGER.info(str.toString());
    }
}
