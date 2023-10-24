package org.manictime.plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    private List<Server> servers = new ArrayList<>();
    private ConcurrentHashMap<String, Integer> serverIndices = new ConcurrentHashMap<>();
    private String lastMessage;

    public ServerManager() {
        refreshServers();
        Timer timer = new Timer();
        long delay = 0; // Start immediately
        long period = 60000; // Repeat every 60,000 milliseconds (1 minute)

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                refreshServers();
            }
        }, delay, period);
    }

    public void refreshServers(){
        for (var index = 0; index < 10; index++) {
            this.refreshServer(String.valueOf(42870+index));
        }
    }

    public void refreshServer(String port) {
        var server = serverIndices.get(port);
        if (server != null) {
            return;
        }
        WebSocket webSocket = null;
        try {
            WebSocket.Builder builder = HttpClient.newHttpClient().newWebSocketBuilder();
            URI uri = URI.create("ws://127.0.0.1:" + port + "/manictime-document/");

            webSocket = builder
                    .buildAsync(uri, new WebSocketListener(port))
                    .join();

            if (lastMessage != null) {
                webSocket.sendText(lastMessage, true);
            }
        }catch (Exception ex){
            System.out.println(ex);
        }
        if(webSocket != null) {
            servers.add(new Server(port, webSocket));
            serverIndices.put(port, Integer.parseInt(port));
        }
    }

    public void send(Object processName, Object documentType, Object document, Object documentGroup) {
        String message = createMessage(processName, documentType, document, documentGroup);
        lastMessage = message;

        System.out.println(servers);
        for (Server server : servers) {
            server.webSocket.sendText(message, true);
            System.out.println("message sent " + message);
        }
    }

    public String getServerInfo() {
        String msg = "";
        if (servers.isEmpty()) {
            msg = "ManicTime client is not connected.";
        } else {
            StringBuilder ports = new StringBuilder();
            for (Server server : servers) {
                if (ports.length() > 0) {
                    ports.append(", ");
                }
                ports.append(server.port);
            }
            msg = "ManicTime client is connected on port(s): " + ports.toString() + ".";
        }
        return msg;
    }


    private String createMessage(Object processName, Object documentType, Object document, Object documentGroup) {
        String message = "{\"ProcessName\":\"" + processName + "\",\"DocumentType\":\"" + documentType +
                "\",\"Document\":\"" + document + "\",\"DocumentGroup\":\"" + documentGroup + "\"}";
        return message;
    }

    private void removePort(String port) {
        Integer index = serverIndices.get(port);
        if (index != null) {
            servers.remove(index);
            serverIndices.remove(port);
        }
    }

    private class WebSocketListener implements WebSocket.Listener {
        private String port;

        WebSocketListener(String port) {
            this.port = port;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            // Handle incoming text data
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            // Handle incoming binary data
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            removePort(port);
            return null;
        }
    }
}

class Server {
    public String port;
    public WebSocket webSocket;

    Server(String port, WebSocket webSocket) {
        this.port = port;
        this.webSocket = webSocket;
    }
}