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
        String str = generateLogo();
        LOGGER.info(str);
    }

    private static String generateLogo() {
        StringBuilder str = new StringBuilder("\n");
        str.append(
            " _______       _     _  _          _______                   ______       _             \n");
        str.append(
            "(_______)     | |   | |(_)        (_______)          _      / _____)     (_)  _         \n");
        str.append(
            " _   ___  ___ | |__ | | _ ____        _ _____  ___ _| |_   ( (____  _   _ _ _| |_ _____ \n");
        str.append(
            "| | (_  |/ _ \\|  _ \\| || |  _ \\      | | ___ |/___|_   _)   \\____ \\| | | | (_   _) ___ |\n");
        str.append(
            "| |___) | |_| | |_) ) || | | | |     | | ____|___ | | |_    _____) ) |_| | | | |_| ____|\n");
        str.append(
            " \\_____/ \\___/|____/ \\_)_|_| |_|     |_|_____|___/   \\__)  (______/|____/|_|  \\__)_____)\n");
        str.append("                   [Produced by OPPO Internet Technology Team]");
        return str.toString();
    }

    public static void main(String[] args) {
    //
        System.out.print(generateLogo());
  }
}
