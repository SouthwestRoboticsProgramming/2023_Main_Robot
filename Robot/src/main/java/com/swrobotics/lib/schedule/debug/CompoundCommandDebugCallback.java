package com.swrobotics.lib.schedule.debug;

import java.util.List;

public interface CompoundCommandDebugCallback {
    void onChildrenInfoChanged(List<CommandDebugDesc> childrenDesc);
}
