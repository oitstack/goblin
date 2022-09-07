package io.github.oitstack.goblin.runtime.health;

import io.github.oitstack.goblin.runtime.Runtime;

import java.util.Set;

import static java.lang.String.format;

public class LinuxInternalCommandPortListeningCheck extends InternalCommandPortListeningCheck {

    public LinuxInternalCommandPortListeningCheck(Runtime runtime, Set<Integer> internalPorts) {
        super(runtime, internalPorts);
    }

    protected String generateCheckPortCmd() {
        StringBuilder command = new StringBuilder("true");

        for (int internalPort : getInternalPorts()) {
            command.append(" && ");
            command.append(" (");
            command.append(format("cat /proc/net/tcp* | awk '{print $2}' | grep -i ':0*%x'", internalPort));
            command.append(" || ");
            command.append(format("nc -vz -w 1 localhost %d", internalPort));
            command.append(" || ");
            command.append(format("/bin/bash -c '</dev/tcp/localhost/%d'", internalPort));
            command.append(")");
        }
        return command.toString();
    }

}

