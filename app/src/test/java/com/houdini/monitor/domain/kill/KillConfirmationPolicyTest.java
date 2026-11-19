package com.houdini.monitor.domain.kill;

import com.houdini.monitor.domain.ProcessInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KillConfirmationPolicyTest {

    private final KillConfirmationPolicy policy = new KillConfirmationPolicy();

    @Test
    void marksSystemUserAsSystemLooking() {
        assertTrue(policy.isSystemLooking(process("notepad.exe", "NT AUTHORITY\\SYSTEM")));
        assertTrue(policy.isSystemLooking(process("svchost.exe", "SYSTEM")));
        assertTrue(policy.isSystemLooking(process("app.exe", "NT AUTHORITY\\LOCAL SERVICE")));
    }

    @Test
    void marksWellKnownSystemNames() {
        assertTrue(policy.isSystemLooking(process("csrss.exe", "user")));
        assertTrue(policy.isSystemLooking(process("lsass.exe", "DESKTOP\\me")));
        assertTrue(policy.isSystemLooking(process("winlogon.exe", "me")));
    }

    @Test
    void normalUserProcessIsNotSystemLooking() {
        assertFalse(policy.isSystemLooking(process("chrome.exe", "DESKTOP\\alice")));
    }

    @Test
    void requiresExtraWarningWhenSystemLooking() {
        ProcessInfo info = process("csrss.exe", "SYSTEM");
        assertTrue(policy.requiresExtraWarning(info));
        assertFalse(policy.requiresExtraWarning(process("code.exe", "DESKTOP\\alice")));
    }

    @Test
    void canProceedOnlyWithFullConfirmation() {
        ProcessInfo normal = process("app.exe", "me");
        assertFalse(policy.canProceed(normal, false, false));
        assertTrue(policy.canProceed(normal, true, false));

        ProcessInfo system = process("lsass.exe", "SYSTEM");
        assertFalse(policy.canProceed(system, true, false));
        assertTrue(policy.canProceed(system, true, true));
    }

    private static ProcessInfo process(String name, String user) {
        boolean systemLooking = new KillConfirmationPolicy().isSystemLooking(
                new ProcessInfo(name, 1L, 0, 0, user, false));
        return new ProcessInfo(name, 42L, 1.0, 1024, user, systemLooking);
    }
}
