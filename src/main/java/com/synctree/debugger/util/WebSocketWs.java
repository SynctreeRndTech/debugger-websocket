package com.synctree.debugger.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;


@Component
@ServerEndpoint("/api/socket")
public class WebSocketWs {

	
    /**
     * 웹소켓 세션을 담는 ArrayList
     */
    private static HashMap<String, Session> sessionMap = new HashMap<String, Session>();

    
    /**
     * 웹소켓 사용자 연결 성립하는 경우 호출
     */
    @OnOpen 
    public void handleOpen(Session session) {
        if (session != null) {
            String sessionId = session.getId();
            
            System.out.println("client is connected. sessionId == [" + sessionId + "]");
            sessionMap.put(sessionId, session);
            
            sendMessageToOne("***** [USER-" + sessionId + "] is connected. *****", sessionId);
        }
    }
    

    /**
     * 웹소켓 메시지(From Client) 수신하는 경우 호출
     */
    @OnMessage
    public String handleMessage(String message, Session session) {
        if (session != null) {
            String sessionId = session.getId();
            System.out.println("message is arrived. sessionId == [" + sessionId + "] / message == [" + message + "]");

            sendMessageToOne("[USER-" + sessionId + "] " + message, sessionId);
        }
        
        return null;
    }
    

    /**
     * 웹소켓 사용자 연결 해제하는 경우 호출
     */
    @OnClose
    public void handleClose(Session session) {
        if (session != null) {
            String sessionId = session.getId();
            System.out.println("client is disconnected. sessionId == [" + sessionId + "]");
            
            sendMessageToOne("***** [USER-" + sessionId + "] is disconnected. *****", sessionId);
        }
    }

    
    /**
     * 웹소켓 에러 발생하는 경우 호출
     */
    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
    }
    
    
    /**
     * 웹소켓 연결 성립되어 있는 모든 사용자에게 메시지 전송
     */
    public boolean sendMessageToOne(String message, String sessionId) {
        if (sessionMap == null) {
            return false;
        }

        int sessionCount = sessionMap.size();
        if (sessionCount < 1) {
            return false;
        }

        Session singleSession = null;
        singleSession = sessionMap.get(sessionId);

        singleSession.getAsyncRemote().sendText(message);

        return true;
    }
}