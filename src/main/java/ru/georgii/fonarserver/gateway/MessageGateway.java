package ru.georgii.fonarserver.gateway;

//import com.corundumstudio.socketio.*;
//import com.corundumstudio.socketio.listener.ConnectListener;
//import com.corundumstudio.socketio.listener.DataListener;
//import com.corundumstudio.socketio.listener.DisconnectListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.socket.engineio.server.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.georgii.fonarserver.auth.AuthService;
import ru.georgii.fonarserver.dialog.Message;
import ru.georgii.fonarserver.dialog.MessageService;
import ru.georgii.fonarserver.gateway.socketio.ServerWrapper;
import ru.georgii.fonarserver.server.FonarConfiguration;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Component
public class MessageGateway {

    private static final Logger log = LoggerFactory.getLogger(MessageGateway.class);
    @Autowired
    FonarConfiguration fonar;

    //private SocketIONamespace namespace;
    @Autowired
    AuthService authService;

    @Autowired
    @Lazy
    MessageService messageService;

    @Autowired
    UserService userService;
    SocketIoServer server;
    SocketIoNamespace ns;


    //private SocketIOServer server;
    //ServerWrapper serverWrapper;
    private Map<Long, Set<SocketIoSocket>> connections;


    private EngineIoServerOptions eioOptions;
    public EngineIoServer mEngineIoServer;
    private SocketIoServer mSocketIoServer;



    public MessageGateway() {

    }

    @PreDestroy
    public void stop() throws Exception {

        for (Set<SocketIoSocket> k : connections.values()) {
            for (SocketIoSocket l : k) {
                l.disconnect(true);
            }
        }

        //serverWrapper.stopServer();
    }

    @PostConstruct
    public void postConstructInit() {

        eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowedCorsOrigins(null);

        mEngineIoServer = new EngineIoServer(eioOptions);
        mSocketIoServer = new SocketIoServer(mEngineIoServer);

//        serverWrapper = new ServerWrapper("::", fonar.busPort, null);
//        // null means "allow all" as stated in https://github.com/socketio/engine.io-server-java/blob/f8cd8fc96f5ee1a027d9b8d9748523e2f9a14d2a/engine.io-server/src/main/java/io/socket/engineio/server/EngineIoServerOptions.java#L26
//        try {
//            serverWrapper.startServer();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//        server = serverWrapper.getSocketIoServer();

        server = mSocketIoServer;
        ns = server.namespace("/");

        connections = new HashMap<>();

        ns.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                SocketIoSocket client = (SocketIoSocket) args[0];
                String saltedGuid = client.getInitialQuery().getOrDefault("authorization", null);
                try {
                    User u = authService.authenticateBySaltedGuid(saltedGuid);
                    if (!connections.containsKey(u.getId())) {
                        connections.put(u.getId(), new HashSet<>());
                    }
                    connections.get(u.getId()).add(client);
                    System.out.println("Client[{" + client.getId() + "}] - Authorized user id" + u.getId() + " '{}'");
                } catch (Exception e) {
                    client.disconnect(true);
                    System.out.println("Client[{" + client.getId() + "}] - Authorization denied '{}'");
                }

                client.on("disconnect", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        String saltedGuid = client.getInitialQuery().getOrDefault("authorization", null);
                        try {
                            User u = authService.authenticateBySaltedGuid(saltedGuid);
                            if (connections.containsKey(u.getId())) {
                                connections.get(u.getId()).remove(client);
                            }
                            System.out.println("Client[{}] - Disconnected from chat module.");
                        } catch (Exception e) {
                            System.out.println("Client[{}] - Disconnected from chat module (ERR).");
                        }


                    }
                });

                client.on("startedTyping", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        String saltedGuid = client.getInitialQuery().getOrDefault("authorization", null);
                        try {
                            User u = authService.authenticateBySaltedGuid(saltedGuid);
                            if (connections.containsKey(u.getId())) {
                                long uidTo = (Integer) args[0];
                                if (connections.containsKey(uidTo)) {
                                    for (SocketIoSocket conn : connections.get(uidTo)) {
                                        conn.send("startedTyping", u.getId());
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                client.on("stoppedTyping", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        String saltedGuid = client.getInitialQuery().getOrDefault("authorization", null);
                        try {
                            User u = authService.authenticateBySaltedGuid(saltedGuid);
                            if (connections.containsKey(u.getId())) {
                                long uidTo = (Integer) args[0];
                                if (connections.containsKey(uidTo)) {
                                    for (SocketIoSocket conn : connections.get(uidTo)) {
                                        conn.send("stoppedTyping", u.getId());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                client.on("seenMessage", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        String saltedGuid = client.getInitialQuery().getOrDefault("authorization", null);
                        try {
                            User u = authService.authenticateBySaltedGuid(saltedGuid);
                            long messageId = (Integer) args[0];
                            long uId = (Integer) args[1];
                            Optional<User> to = userService.getUser(uId);
                            to.ifPresent(user -> messageService.markAsSeen(user, u.getId(), messageId));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });


    }

    public void notifyAboutNewMessage(Message m) {
        if (connections.containsKey(m.getToUserId())) {
            try {
                for (SocketIoSocket c : connections.get(m.getToUserId())) {
                    c.send("message", new ObjectMapper().writeValueAsString(m));
                }
            } catch (Exception e) {
                System.out.println("Failed to send notification");
            }
        }
        System.out.println("Tried to send notifications");
    }

    public void notifyAboutSeen(Message m) {
        if (connections.containsKey(m.getToUserId())) {
            try {
                for (SocketIoSocket c : connections.get(m.getToUserId())) {
                    c.send("messageSeen", m.getId(), m.getFromUserId());
                }
            } catch (Exception e) {
                System.out.println("Failed to send notification");
            }
        }
    }


//
//    private ConnectListener onConnected() {
//        return client -> {
//            System.out.println(client.isChannelOpen());
//            System.out.println(client.getSessionId());
//            //System.out.println(client.);
//            HandshakeData handshakeData = client.getHandshakeData();
//            String saltedGuid = handshakeData.getSingleUrlParam("authorization");
//            try {
//                User u = authService.authenticateBySaltedGuid(saltedGuid);
//                if (!connections.containsKey(u.getId())) {
//                    connections.put(u.getId(), new HashSet<SocketIOClient>());
//                }
//                connections.get(u.getId()).add(client);
//                System.out.println("Client[{"+ client.getRemoteAddress() +"}] - Authorized user id" + u.getId() + " '{}'" );
//            } catch (Exception e) {
//                client.disconnect();
//                System.out.println("Client[{" + client.getRemoteAddress() + "}] - Authorization denied '{}'" );
//            }
//
//        };
//    }

//    private DisconnectListener onDisconnected() {
//        return client -> {
//            HandshakeData handshakeData = client.getHandshakeData();
//            String saltedGuid = handshakeData.getSingleUrlParam("authorization");
//            try {
//                User u = authService.authenticateBySaltedGuid(saltedGuid);
//                if (connections.containsKey(u.getId())) {
//                    connections.get(u.getId()).remove(client);
//                }
//            } catch (Exception e) {
//            }
//            System.out.println("Client[{}] - Disconnected from chat module." );
//        };
//    }

}