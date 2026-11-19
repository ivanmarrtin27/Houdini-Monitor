package com.houdini.monitor.ui;

import com.houdini.monitor.domain.ProcessInfo;
import com.houdini.monitor.domain.kill.KillConfirmationPolicy;
import com.houdini.monitor.domain.kill.KillOutcome;
import com.houdini.monitor.i18n.Messages;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class KillConfirmDialog {

    private final Messages messages;
    private final KillConfirmationPolicy policy = new KillConfirmationPolicy();

    public KillConfirmDialog(Messages messages) {
        this.messages = messages;
    }

    /**
     * @return SUCCESS if the user confirmed (caller should terminate), CANCELLED otherwise
     */
    public KillOutcome confirm(ProcessInfo process) {
        boolean extraWarning = policy.requiresExtraWarning(process);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(messages.get("kill.title"));
        alert.setHeaderText(messages.format("kill.message", process.name(), process.pid()));
        alert.setContentText(extraWarning ? messages.get("kill.systemWarning") : null);

        ButtonType confirm = new ButtonType(messages.get("kill.confirm"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(messages.get("kill.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirm, cancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != confirm) {
            return KillOutcome.CANCELLED;
        }
        // Confirming a dialog that already showed the system warning accepts the extra warning.
        if (!policy.canProceed(process, true, extraWarning)) {
            return KillOutcome.CANCELLED;
        }
        return KillOutcome.SUCCESS;
    }

    public void showOutcome(KillOutcome outcome) {
        if (outcome == KillOutcome.CANCELLED) {
            return;
        }
        Alert alert = new Alert(outcome == KillOutcome.SUCCESS ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(messages.get("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(switch (outcome) {
            case SUCCESS -> messages.get("kill.success");
            case NOT_FOUND -> messages.get("kill.notFound");
            case ACCESS_DENIED -> messages.get("kill.accessDenied");
            case FAILED -> messages.get("kill.failed");
            case CANCELLED -> messages.get("kill.cancelled");
        });
        alert.showAndWait();
    }
}
