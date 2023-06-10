package com.swrobotics.robot.autotest;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.lib.net.NTString;

public final class HumanConfirmation extends Test {
    private static final NTEntry<String> PROMPT = new NTString("Confirm/Prompt", "").setTemporary();
    private static final NTEntry<Boolean> CONFIRM = new NTBoolean("Confirm/Confirm", false).setTemporary();

    private final String prompt;

    public HumanConfirmation(String name, String prompt) {
        super(name);
        this.prompt = prompt;
    }

    @Override
    public void initialize() {
        PROMPT.set(prompt);
        CONFIRM.set(false);
    }

    @Override
    public void execute() {
        if (CONFIRM.get()) {
            System.out.println("User confirmed: " + prompt);
            pass();
        }
    }
}
