package com.houdini.monitor.metrics;

import com.houdini.monitor.domain.kill.KillOutcome;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class ProcessTerminator {

    public KillOutcome terminate(long pid) {
        if (pid <= 0) {
            return KillOutcome.NOT_FOUND;
        }
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        if (handle.isEmpty()) {
            return KillOutcome.NOT_FOUND;
        }
        ProcessHandle process = handle.get();
        try {
            boolean requested = process.destroy();
            if (!requested) {
                return mapFailure(process);
            }
            boolean exited = process.onExit().toCompletableFuture().get(2, TimeUnit.SECONDS) != null
                    || !process.isAlive();
            if (!process.isAlive() || exited) {
                return KillOutcome.SUCCESS;
            }
            process.destroyForcibly();
            process.onExit().toCompletableFuture().get(2, TimeUnit.SECONDS);
            return process.isAlive() ? KillOutcome.FAILED : KillOutcome.SUCCESS;
        } catch (SecurityException ex) {
            return KillOutcome.ACCESS_DENIED;
        } catch (Exception ex) {
            if (isAccessDenied(ex)) {
                return KillOutcome.ACCESS_DENIED;
            }
            if (!ProcessHandle.of(pid).isPresent() || ProcessHandle.of(pid).map(h -> !h.isAlive()).orElse(true)) {
                return KillOutcome.NOT_FOUND;
            }
            return KillOutcome.FAILED;
        }
    }

    private static KillOutcome mapFailure(ProcessHandle process) {
        if (!process.isAlive()) {
            return KillOutcome.NOT_FOUND;
        }
        try {
            process.destroyForcibly();
            return process.isAlive() ? KillOutcome.ACCESS_DENIED : KillOutcome.SUCCESS;
        } catch (SecurityException ex) {
            return KillOutcome.ACCESS_DENIED;
        } catch (Exception ex) {
            return isAccessDenied(ex) ? KillOutcome.ACCESS_DENIED : KillOutcome.FAILED;
        }
    }

    private static boolean isAccessDenied(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (current instanceof SecurityException) {
                return true;
            }
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("access is denied")
                        || lower.contains("access denied")
                        || lower.contains("permission")
                        || lower.contains("privileged")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
