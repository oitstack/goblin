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
package io.github.oitstack.goblin.container.jdbc.util;

import io.github.oitstack.goblin.container.jdbc.DatabaseDelegate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public abstract class ScriptUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptUtils.class);

    /**
     * Default prefix for line comments in SQL scripts.
     */
    public static final String COMMENT_PREFIX = "--";

    /**
     * Default statement separator in SQL scripts.
     */
    public static final String STATEMENT_SEPARATOR = ";";

    /**
     * Default end delimiter for block comments in SQL scripts.
     */
    public static final String BLOCK_COMMENT_END_DELIMITER = "*/";

    /**
     * Fallback statement separator in SQL scripts.
     */
    public static final String STATEMENT_SEPARATOR_FALLBACK = "\n";

    /**
     * Default start delimiter for block comments in SQL scripts.
     */
    public static final String BLOCK_COMMENT_START_DELIMITER = "/*";

    private static final String KEY_WORD_BEGIN = "BEGIN";

    private static final String KEY_WORD_END = "END";


    private ScriptUtils() {
    }

    /**
     * Split an SQL script into separate statements delimited by the provided
     * separator string. Each individual statement will be added to the provided
     * List
     */
    public static void parseSqlScript(String scriptPath, String script, String separator, String commentPrefix,
                                      String blockCommentStartDelimiter, String blockCommentEndDelimiter, List<String> statements) {

        argumentCheck("script can not be blank", null!=script&&!"".equals(script));
        argumentCheck("commentPrefix must not be null or empty", null!=commentPrefix&&!"".equals(commentPrefix));
        argumentCheck("separator must not be null", separator != null);
        argumentCheck( "blockCommentEndDelimiter must not be null or empty",null!=blockCommentEndDelimiter&&!"".equals(blockCommentEndDelimiter));
        argumentCheck("blockCommentStartDelimiter must not be null or empty",null!=blockCommentStartDelimiter&&!"".equals(blockCommentStartDelimiter) );


        StringBuilder sb = parse(scriptPath, script, separator, commentPrefix, blockCommentStartDelimiter, blockCommentEndDelimiter, statements);

        flushStatement(sb, statements);
    }


    private static StringBuilder flushStatement(StringBuilder stringBuilder, List<String> statements) {
        if (stringBuilder.length() == 0) {
            return stringBuilder;
        }

        final String statement = stringBuilder.toString().trim();
        if (null!=statement&&!"".equals(statement)) {
            statements.add(statement);
        }

        return new StringBuilder();
    }

    private static boolean isSeperatorChar(char c,
                                           String separator,
                                           String commentPrefix,
                                           String commentStartDelimiter) {
        return c == ' '
                || c == '\r'
                || c == '\t'
                || c == '\n'
                || c == separator.charAt(separator.length() - 1)
                || c == commentPrefix.charAt(0)
                || c == separator.charAt(0)
                || c == commentStartDelimiter.charAt(commentStartDelimiter.length() - 1)
                || c == commentStartDelimiter.charAt(0);
    }

    private static boolean containsSubstring(String lowerCaseStr, String subStr, int offset) {
        String substringLowCase = subStr.toLowerCase();

        return lowerCaseStr.startsWith(substringLowCase, offset);
    }

    private static boolean containsKeywords(String lowerCaseStr, String keyWords,
                                            String separator, String commentPrefix,
                                            String commentStartDelimiter, int offset) {

        boolean frontSeparated = (offset >= (lowerCaseStr.length() - keyWords.length()))
                || isSeperatorChar(lowerCaseStr.charAt(offset + keyWords.length()),
                        separator, commentPrefix, commentStartDelimiter);

        boolean backSeparated = (offset == 0) || isSeperatorChar(lowerCaseStr.charAt(offset - 1),
                separator, commentPrefix, commentStartDelimiter);

        return backSeparated && frontSeparated && lowerCaseStr.startsWith(keyWords.toLowerCase(), offset);
    }

    private static void argumentCheck(String errorMsg, boolean valid) {
        if (valid) {
        } else {
            LOGGER.error("argument is invalid! {}", errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Parse database script.
     */
    private static StringBuilder parse(String scriptPath, String script,
                                       String separator, String commentPrefix,
                                       String commentStartDelimiter, String commentEndDelimiter,
                                       List<String> statements) {

        boolean inEscapes = false;
        boolean inLineComments = false;
        boolean inBlockComments = false;
        Character currentDelimiter = null;

        int compoundDepthOfStatement = 0;
        final String lowerCaseScript = script.toLowerCase();
        char[] scriptCharArray = script.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < script.length(); i++) {
            char ch = scriptCharArray[i];
            if (inEscapes) {
                inEscapes = false;
                sb.append(ch);
                continue;
            }
            // mysql style escapes
            if (ch == '\\') {
                inEscapes = true;
                sb.append(ch);
                continue;
            }

            if (!inBlockComments
                    && !inLineComments
                    && (ch == '\'' || ch == '"' || ch == '`')) {
                if (currentDelimiter == null) {
                    currentDelimiter = ch;
                } else if (currentDelimiter == ch) {
                    currentDelimiter = null;
                }
            }
            final boolean inLiteral = currentDelimiter != null;

            if (!inLiteral && containsSubstring(lowerCaseScript, commentPrefix, i)) {
                inLineComments = true;
            }
            if (inLineComments && ch == '\n') {
                inLineComments = false;
            }
            if (!inLiteral && containsSubstring(lowerCaseScript, commentStartDelimiter, i)) {
                inBlockComments = true;
            }
            if (!inLiteral && inBlockComments && containsSubstring(lowerCaseScript, commentEndDelimiter, i)) {
                inBlockComments = false;
            }
            final boolean inComment = inLineComments || inBlockComments;

            if (!inLiteral && !inComment
                    && containsKeywords(lowerCaseScript, KEY_WORD_BEGIN, separator, commentPrefix, commentStartDelimiter, i)) {
                compoundDepthOfStatement++;
            }
            if (!inLiteral && !inComment
                    && containsKeywords(lowerCaseScript, KEY_WORD_END, separator, commentPrefix, commentStartDelimiter, i)) {
                compoundDepthOfStatement--;
            }
            final boolean inCompoundStatement = compoundDepthOfStatement != 0;

            if (!inLiteral && !inCompoundStatement) {
                if (script.startsWith(separator, i)) {
                    sb = flushStatement(sb, statements);
                    i += separator.length() - 1;
                    continue;
                }
                else if (script.startsWith(commentPrefix, i)) {
                    int indexOfNextNewline = script.indexOf("\n", i);
                    if (indexOfNextNewline > i) {
                        i = indexOfNextNewline;
                        continue;
                    }
                    else {
                        break;
                    }
                }
                else if (script.startsWith(commentStartDelimiter, i)) {
                    int indexOfCommentEnd = script.indexOf(commentEndDelimiter, i);
                    if (indexOfCommentEnd > i) {
                        i = indexOfCommentEnd + commentEndDelimiter.length() - 1;
                        inBlockComments = false;
                        continue;
                    }
                    else {
                        throw new ParseScriptException(String.format("Missing comment end delimiter [%s].",
                                commentEndDelimiter), scriptPath);
                    }
                }
                else if (ch == ' '
                        || ch == '\r'
                        || ch == '\t'
                        || ch == '\n') {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                        ch = ' ';
                    }
                    else {
                        continue;
                    }
                }
            }
            sb.append(ch);
        }
        return sb;
    }

    /**
     * Whether the current script contains the delimiter.
     * @param sqlScript the SQL script
     * @param delimiter delimiter of each statement
     */
    public static boolean containsScriptDelimiters(String sqlScript, String delimiter) {
        boolean inIter = false;
        char[] scriptCharArray = sqlScript.toCharArray();
        for (int i = 0; i < sqlScript.length(); i++) {
            if (scriptCharArray[i] == '\'') {
                inIter = !inIter;
            }
            if (!inIter && sqlScript.startsWith(delimiter, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute database scripts.
     */
    public static void executeDatabaseScript(DatabaseDelegate databaseDelegate, String scriptPath, String script) throws ScriptException {
        executeDatabaseScript(scriptPath, script, false, false,
                COMMENT_PREFIX, STATEMENT_SEPARATOR, BLOCK_COMMENT_START_DELIMITER, BLOCK_COMMENT_END_DELIMITER, databaseDelegate);
    }

    /**
     * Loading database script
     *
     * @param scriptPath  the file to load the init script from
     * @param dbDelegate  database delegate for script execution
     */
    public static void runScript(String scriptPath, DatabaseDelegate dbDelegate) {
        try {
            ClassLoader classLoader = ScriptUtils.class.getClassLoader();
            URL resource = classLoader.getResource(scriptPath);
            if (resource == null) {
                LOGGER.warn("Could not load script: {}", scriptPath);
                throw new ScriptLoadingException("Could not load script: " + scriptPath + ". File not found.");
            }
            String scripts = IOUtils.toString(resource, StandardCharsets.UTF_8);
            executeDatabaseScript(dbDelegate, scriptPath, scripts);
        } catch (IOException e) {
            LOGGER.warn("Could not load script: {}", scriptPath);
            throw new ScriptLoadingException("Could not load script: " + scriptPath, e);
        } catch (ScriptException e) {
            LOGGER.error("Error while executing script: {}", scriptPath, e);
            throw new UnCategorizedScriptException("Error while executing script: " + scriptPath, e);
        }
    }

    /**
     * Execute the given database script.
     */
    public static void executeDatabaseScript(String scriptPath, String script, boolean continueWhenError,
                                             boolean ignoreFailedDrops, String commentPrefix, String customeSeperator, String blockCommentStartDelimiter,
                                             String blockCommentEndDelimiter, DatabaseDelegate dbDelegate) {

        try {
            long startTime = System.currentTimeMillis();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Execute sql script " + scriptPath);
            }

            if (customeSeperator == null) {
                customeSeperator = STATEMENT_SEPARATOR;
            }
            if (!containsScriptDelimiters(script, customeSeperator)) {
                customeSeperator = STATEMENT_SEPARATOR_FALLBACK;
            }

            List<String> statementList = new LinkedList<>();
            parseSqlScript(scriptPath, script, customeSeperator, commentPrefix, blockCommentStartDelimiter,
                    blockCommentEndDelimiter, statementList);

            try (DatabaseDelegate delegate = dbDelegate) {
                delegate.execute(statementList, scriptPath, continueWhenError, ignoreFailedDrops);
            }

            long costTime = System.currentTimeMillis() - startTime;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Executed sql script " + scriptPath + " in " + costTime + " ms.");
            }
        }catch (Exception e) {
            throw new UnCategorizedScriptException(
                    "Failed to execute sql script [" + script + "]", e);
        }
    }

    public static class ScriptLoadingException extends RuntimeException {
        public ScriptLoadingException(String message) {
            super(message);
        }

        public ScriptLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ParseScriptException extends RuntimeException {
        public ParseScriptException(String format, String scriptPath) {
            super(String.format(format, scriptPath));
        }
    }

    public static class StatementFailedException extends RuntimeException {
        public StatementFailedException(String statement, int lineNumber, String scriptPath) {
            this(statement, lineNumber, scriptPath, null);
        }

        public StatementFailedException(String statement, int lineNumber, String scriptPath, Exception ex) {
            super(String.format("Script execution failed (%s:%d): %s", scriptPath, lineNumber, statement), ex);
        }
    }

    public static class UnCategorizedScriptException extends RuntimeException {
        public UnCategorizedScriptException(String s, Exception ex) {
            super(s, ex);
        }
        
    }
}
