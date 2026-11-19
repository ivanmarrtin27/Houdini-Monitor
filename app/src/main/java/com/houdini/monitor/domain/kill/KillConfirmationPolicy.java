package com.houdini.monitor.domain.kill;

import com.houdini.monitor.domain.ProcessInfo;

import java.util.Locale;
import java.util.Set;

public final class KillConfirmationPolicy {

    private static final Set<String> WELL_KNOWN_SYSTEM_NAMES = Set.of(
            "system",
            "registry",
            "smss.exe",
            "csrss.exe",
            "wininit.exe",
            "winlogon.exe",
            "services.exe",
            "lsass.exe",
            "svchost.exe",
            "fontdrvhost.exe",
            "dwm.exe",
            "memory compression"
    );

    public boolean isSystemLooking(ProcessInfo process) {
        if (process == null) {
            return false;
        }
        String user = process.user() == null ? "" : process.user().toUpperCase(Locale.ROOT);
        if (user.contains("NT AUTHORITY") || user.equals("SYSTEM") || user.endsWith("\\SYSTEM")
                || user.contains("LOCAL SERVICE") || user.contains("NETWORK SERVICE")) {
            return true;
        }
        String name = process.name() == null ? "" : process.name().toLowerCase(Locale.ROOT).trim();
        return WELL_KNOWN_SYSTEM_NAMES.contains(name);
    }

    public boolean requiresExtraWarning(ProcessInfo process) {
        return process != null && (process.systemLooking() || isSystemLooking(process));
    }

    public boolean canProceed(ProcessInfo process, boolean userConfirmed, boolean userAcceptedExtraWarning) {
        if (process == null || !userConfirmed) {
            return false;
        }
        if (requiresExtraWarning(process)) {
            return userAcceptedExtraWarning;
        }
        return true;
    }
}
