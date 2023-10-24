package org.manictime.plugin;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

public class ManicTimeEventListener implements FileEditorManagerListener, StatusBarWidget, StatusBarWidget.TextPresentation {
    private StatusBar myStatusBar;
    private final ServerManager serverManager;

    public ManicTimeEventListener(final Project project) {
        MessageBus bus = project.getMessageBus();
        serverManager = new ServerManager();
        bus.connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                this
        );
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
       var filePath = event.getNewFile().getPath();
       var file = event.getNewFile().getName();
       serverManager.send("idea64", "ManicTime/Files", filePath, file);

        if(myStatusBar != null)
            myStatusBar.updateWidget("ManicTimeStatusBar");
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        myStatusBar = statusBar;
    }

    @Override
    public @NotNull @NonNls String ID() {
        return "ManicTimeStatusBar";
    }

    @Override
    public @NotNull WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void dispose() {

    }

    @Override
    public @NotNull @NlsContexts.Label String getText() {
        return "ManicTime";
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return serverManager.getServerInfo();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }
}
