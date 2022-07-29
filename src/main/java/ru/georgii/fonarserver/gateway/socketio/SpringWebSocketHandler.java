package ru.georgii.fonarserver.gateway.socketio;


import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.springframework.web.socket.*;

public final class SpringWebSocketHandler extends EngineIoWebSocket implements WebSocketHandler {
    private final EngineIoServer mServer;
    private WebSocketSession mSession;
    private Map<String, String> mQuery;
    private Map<String, List<String>> mHeaders;

    public SpringWebSocketHandler(EngineIoServer server) {
        this.mServer = server;
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

        //this.write(message.getBytes());
        this.mSession.sendMessage(new TextMessage(message));

//        this.mSession.sendMessage(new WebSocketMessage<String>() {
//            @Override
//            public String getPayload() {
//                return message;
//            }
//
//            @Override
//            public int getPayloadLength() {
//                return message.getBytes().length;
//            }
//
//            @Override
//            public boolean isLast() {
//                return false;
//            }
//        });
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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.mSession = session;
        this.mQuery = ParseQS.decode(session.getUri().toString());

        this.mHeaders = session.getHandshakeHeaders();
        this.mServer.handleWebSocket(this);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        this.emit("message", new Object[]{message.getPayload()});
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        this.emit("error", new Object[]{"write error", exception.getMessage()});
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        this.emit("close", new Object[0]);
        this.mSession = null;
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

