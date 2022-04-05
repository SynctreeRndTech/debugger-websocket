package com.synctree.debugger.handler;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.synctree.debugger.vo.DebuggerVo;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class WebSocketHttpHandler extends TextWebSocketHandler {

	//private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();
	private static HashMap<String, WebSocketSession> sessions = new HashMap<String, WebSocketSession>();
	//private static RedisHandler redisHandler;
	private static HashMap<String, String> lockKeyMap = new HashMap<String, String>();
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.put(session.getId(), session);
        System.out.println("[Currently Connected Session List] ::: " + sessions.keySet().toString());
        System.out.println("[Client Connected] ::: "+ session.getRemoteAddress().toString());
        System.out.println("[Client SessionID] ::: "+session.getId());
        System.out.println("[Client HandshakeHeaders] ::: "+ session.getHandshakeHeaders().toString());
        
        session.sendMessage(new TextMessage("{\r\n"
        		+ "    \"msg_type\" : \"text\",\r\n"
        		+ "    \"msg_data\" : {\"send_session_id\"\r\n"
        		+ "    \"session_id\" :\"" + session.getId() + "\" }\r\n"
        	    + "}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	System.out.println("[Client Message Handled] ::: "+ session.getRemoteAddress().toString()+ ", " + message.getPayload().toString());
    	
    	String payload = message.getPayload();
    	String payloadSplit[] = payload.split(",");
    	String sessionId = payloadSplit[1];
    	System.out.println("sessionId 찍어보기==============>" + sessionId);
    	System.out.println("lockKey 가져온거 찍어보기==============>" + lockKeyMap.get(sessionId));
    	
    	if(payloadSplit[0].equals("request_for_unlock")){
    		boolean result = unlockRedisSpinLock(lockKeyMap.get(sessionId), "0");
    		session.sendMessage(new TextMessage("{\r\n"
             		+ "    \"msg_type\" : \"unlock_result\",\r\n"
             		+ "    \"msg_data\" : {\r\n"
             		+ "    \"result\" : " + result + "}\r\n"
             	    + "}"));
    	} else if (payloadSplit[0].equals("GET_SESSION_ID")){
    		session.sendMessage(new TextMessage("{\r\n"
             		+ "    \"msg_type\" : \"session_id\",\r\n"
             		+ "    \"msg_data\" : {\r\n"
             		+ "    \"result\" : " + session.getId() + "}\r\n"
             	    + "}"));
    	} else {
            session.sendMessage(new TextMessage("{\r\n"
            		+ "    \"msg_type\" : \"send_message_string\",\r\n"
            		+ "    \"msg_data\" : {\r\n"
            		+ "    \"message\" : " + message.getPayload().toString() + "}\r\n"
            	    + "}"));
    	}

    	/*
    	for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession) continue;
            session.sendMessage(message);
        }
        */

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session.getId());

        System.out.println("[Client Disconnected] ::: "+ session.getRemoteAddress().toString());
    }
    
    public boolean sendMessageToOne(DebuggerVo debuggerVo) {
        if (sessions == null) {
            return false;
        }
        
        WebSocketSession singleSession = sessions.get(debuggerVo.getSessionId());
        System.out.println("map에 lock key 셋팅하는거 찍어보기==============>");
        lockKeyMap.put(debuggerVo.getSessionId(), debuggerVo.getLockKey());
        System.out.println("map에 lock key 셋팅 결과 찍어보기==============>" + lockKeyMap.toString());
        String message = debuggerVo.getExtraId();
        
        try {
        	/*
			singleSession.sendMessage(new TextMessage("{\r\n"
	        		+ "    \"msg_type\" : \"send_debug_data\",\r\n"
	        		+ "    \"msg_data\" : {\r\n"
	        		+ "        \"extra_id\" :\"" + debuggerVo.getExtraId() + "\",\r\n"
	        		+ "        \"debug_data\" :\""+ "{DEBUG_JSON_DATA(TO-BE)}" + "\",\r\n"
	        		+ "        \"next\" : BOOLEAN\r\n"
	        		+ "    }\r\n"
	        	    + "}"));
	        */
        	singleSession.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			e.printStackTrace();
		}

        return true;
    }
    
    private boolean unlockRedisSpinLock (String key, String value) {
    	//return redisHandler.setRedisStringValue(lockKey, "0");
    	ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		stringValueOperations.set(key, value);
		System.out.println("Redis Set : key '" + key + "', " + "value : " + stringValueOperations.get(key));
		if(stringValueOperations.get(key).equals(value)) {
			return true;
		}
		return false;
    }
}