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
package io.github.oitstack.goblin.runtime.docker.utils;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import lombok.Data;

/**
 * @Author CuttleFish
 * @Date 2022/3/9 下午4:00
 */
@Data
public class ExecResultCallback extends ResultCallbackTemplate<ExecResultCallback, Frame> {
    private String stdout;
    private String stderr;


    @Override
    public void onNext(Frame frame) {

        if (frame.getStreamType() == StreamType.STDOUT) {
            stdout = new String(frame.getPayload());
        } else if (frame.getStreamType() == StreamType.STDERR) {
            stderr = new String(frame.getPayload());
        }

    }


}
