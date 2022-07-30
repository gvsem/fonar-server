package ru.georgii.fonarserver.gateway;


import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.springframework.web.socket.*;

public final class SpringWebSocketHandler implements WebSocketHandler {

    static class FonarSocket extends EngineIoWebSocket {

        private WebSocketSession mSession;
        private Map<String, String> mQuery;
        private Map<String, List<String>> mHeaders;

        FonarSocket(WebSocketSession session) {
            this.mSession = session;
            this.mQuery = ParseQS.decode(session.getUri().toString());
            this.mHeaders = session.getHandshakeHeaders();
        }

        public Map<String, String> getQuery() {

            Map<String, String> query = this.mQuery;
            for (String m : query.keySet()) {
                if (m.endsWith("authorization")) {
                    query.put("authorization", query.get(m));
                    break;
                }
            }
            return query;

        }

        public Map<String, List<String>> getConnectionHeaders() {
            return mHeaders;
        }

        public synchronized void write(String message) throws IOException {
            assert this.mSession != null;
            this.mSession.sendMessage(new TextMessage(message));
        }

        public synchronized void write(byte[] message) throws IOException {
            assert this.mSession != null;

            ByteBuffer b = ByteBuffer.wrap(message);
            this.mSession.sendMessage(new BinaryMessage(b));

        }

        public void close() {
            if (this.mSession != null) {
                try {
                    this.mSession.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }
    private final EngineIoServer mServer;
    Map<String, FonarSocket> sockets;

    public SpringWebSocketHandler(EngineIoServer server) {
        this.mServer = server;
        this.sockets = new HashMap<>();
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established " + session.getId());
        sockets.put(session.getId(), new FonarSocket(session));
        this.mServer.handleWebSocket(sockets.get(session.getId()));
    }

    @Override
    public synchronized void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        sockets.get(session.getId()).emit("message", new Object[]{message.getPayload()});
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Transport error " + session.getId());
        sockets.get(session.getId()).emit("error", new Object[]{"write error", exception.getMessage()});
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("Connection closed " + session.getId() + " " + closeStatus.getReason());
        sockets.get(session.getId()).emit("close", new Object[0]);
        sockets.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

