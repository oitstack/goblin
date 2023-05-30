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
package io.github.oitstack.goblin.unit.db;

import io.github.oitstack.goblin.core.Goblin;

import java.io.*;
import java.util.Map;

public final class IOUtils {

    private IOUtils() {
        super();
    }

    /**
     * read data stream as string.
     * @param dataStream file stream
     * @return
     * @throws IOException
     */
    public static String readFullStream(InputStream dataStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream, "UTF-8"));

        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
    public static InputStream getStreamFromClasspathBaseResource(Class<?> resourceBase, String dataLocation) {

        if(isFileAvailableOnClasspath(resourceBase, dataLocation)) {
            return resourceBase.getResourceAsStream(dataLocation);
        } else {
            return null;
        }

    }
    public static boolean isFileAvailableOnClasspath(Class<?> resourceBase, String dataLocation) {
        return resourceBase.getResourceAsStream(dataLocation) != null;
    }

    /**
     * Get specified file on the classpath.
     * @param resourceBaseClass class
     * @param fileLocation file location
     * @return
     */
    public static InputStream getStreamFromClassPathBaseResource(Class<?> resourceBaseClass, String fileLocation) {
        if(isFileAvailableOnClassPath(resourceBaseClass, fileLocation)) {
            return resourceBaseClass.getResourceAsStream(fileLocation);
        } else {
            return null;
        }
    }

    /**
     * Check if a file exists on the classpath.
     * @param resourceBaseClass class
     * @param location  file location
     * @return
     */
    public static boolean isFileAvailableOnClassPath(Class<?> resourceBaseClass, String location) {
        return resourceBaseClass.getResourceAsStream(location) != null;
    }


    public static InputStream getTextStreamFromClasspathBaseResourceWithPlaceHold(Class<?> resourceBase, String dataLocation) {

        InputStream is= doGetStreamFromClasspathBaseResource(resourceBase,dataLocation);
        if(null!=is){
            return replacePlaceHoldForTextInputStream(is);
        }else{
            return null;
        }

    }

    public static InputStream replacePlaceHoldForTextInputStream(InputStream is){
        if(null==is){
            return null;
        }
        byte[] bytes = new byte[0];
        try {
            bytes = new byte[is.available()];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            is.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String str = new String(bytes);

        for(Map.Entry<String,String> entry: Goblin.getInstance().getPlaceHolders().entrySet()){
            str=str.replace("${"+entry.getKey()+"}",entry.getValue());
        }
        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(str.getBytes());
    }
    public static InputStream doGetStreamFromClasspathBaseResource(Class<?> resourceBase, String dataLocation) {

        if(isFileAvailableOnClasspath(resourceBase, dataLocation)) {
            return resourceBase.getResourceAsStream(dataLocation);
        } else {
            return null;
        }

    }
}
