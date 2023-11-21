package org.manictime.plugin;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.fileEditor.FileEditorManager;
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
import java.util.Timer;
import java.util.TimerTask;


public class ManicTimeEventListener implements FileEditorManagerListener, StatusBarWidget, StatusBarWidget.TextPresentation {
    private StatusBar myStatusBar;
    private final ServerManager serverManager;
    private  int processId;
    private final String applicationName;

    public ManicTimeEventListener(final Project project) {
        MessageBus bus = project.getMessageBus();
        serverManager = new ServerManager();

        processId = getProcessId();
        applicationName = ApplicationInfo.getInstance().getVersionName();
        bus.connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                this
        );
        Timer timer = new Timer();
        long delay = 0; // Start immediately
        long period = 30000; // Repeat every 30,000 milliseconds (30 seconds)
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                GetFileFromProject(project);
            }
        }, delay, period);

    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
        var file = event.getNewFile();
        if (processId == 0)
            processId = getProcessId();
        if (file != null) {
            var filePath = file.getPath();
            var fileName = file.getName();
            serverManager.send(applicationName, processId,"ManicTime/Files", filePath, fileName);
        }
        if (myStatusBar != null)
            myStatusBar.updateWidget("ManicTimeStatusBar");
    }


    public void GetFileFromProject(Project project) {
        var selectedFileEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        if (processId == 0)
            processId = getProcessId();
        if (selectedFileEditor != null) {
            var file = selectedFileEditor.getFile();
            if (file != null) {
                var filePath = file.getPath();
                var fileName = file.getName();
                serverManager.send(applicationName, processId,"ManicTime/Files", filePath, fileName);
            }
        }
        if (myStatusBar != null)
            myStatusBar.updateWidget("ManicTimeStatusBar");
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        myStatusBar = statusBar;
    }

    public static int getProcessId() {
            return (int) ProcessHandle.current().pid();
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
    public @NlsContexts.Label @NotNull String getText() {
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
