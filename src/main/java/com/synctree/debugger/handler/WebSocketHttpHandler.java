package com.synctree.debugger.handler;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;
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
	private HashMap<String, String> lockKeyMap = new HashMap<String, String>();
	private JSONObject jsonObj = new JSONObject();
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.put(session.getId(), session);
        System.out.println("::: [Currently Connected Session List] ::: " + sessions.keySet().toString());
        System.out.println("::: [Client Connected] ::: "+ session.getRemoteAddress().toString());
        System.out.println("::: [Client SessionID] ::: "+session.getId());
        System.out.println("::: [Client HandshakeHeaders] ::: "+ session.getHandshakeHeaders().toString());
        
        jsonObj.put("msg_type", "text");
        jsonObj.put("msg_data", "send_session_id");
        jsonObj.put("session_id", session.getId());
        
        session.sendMessage(new TextMessage(jsonObj.toString()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	System.out.println("[Client Message Handled] ::: "+ session.getRemoteAddress().toString()+ ", " + message.getPayload().toString());
    	
    	String payload = message.getPayload();
    	String payloadSplit[] = payload.split(",");
    	String sessionId = payloadSplit[1];
    	//System.out.println("sessionId ==============>" + sessionId);
    	//System.out.println("lockKey ==============>" + lockKeyMap.get(sessionId));
    	
    	if(payloadSplit[0].equals("request_for_unlock")){
    		boolean result = unlockRedisSpinLock(lockKeyMap.get(sessionId), "0");
    		jsonObj.put("msg_type", "boolean");
    		jsonObj.put("msg_type", "unlock_result");
            jsonObj.put("msg_data", result);
            
            session.sendMessage(new TextMessage(jsonObj.toString()));
            
    	} else if (payloadSplit[0].equals("GET_SESSION_ID")){
    		jsonObj.put("msg_type", "text");
            jsonObj.put("msg_data", "send_session_id");
            jsonObj.put("session_id", session.getId());
            
            session.sendMessage(new TextMessage(jsonObj.toString()));
            
    	} else {
    		jsonObj.put("msg_type", "text");
            jsonObj.put("msg_data", "send_message_string");
            jsonObj.put("session_id", message.getPayload().toString());
            
            session.sendMessage(new TextMessage(jsonObj.toString()));
    	}

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        System.out.println("::: [Client ::: "+ session.getRemoteAddress().toString() + ", SessionID ::: " + session.getId() + " ::: Disconnected]");
        sessions.remove(session.getId());
    }
    
    public boolean sendMessageToOne(DebuggerVo debuggerVo) {
        if (sessions == null) {
            return false;
        }
        
        WebSocketSession singleSession = sessions.get(debuggerVo.getSessionId());
        lockKeyMap.put(debuggerVo.getSessionId(), debuggerVo.getLockKey());
        String message = debuggerVo.getExtraId();
        
        try {
        	
        	singleSession.sendMessage(new TextMessage(message));
        	
		} catch (IOException e) {
			e.printStackTrace();
		}

        return true;
    }
    
    private boolean unlockRedisSpinLock (String key, String value) {
    	ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		stringValueOperations.set(key, value);
		//System.out.println("Redis Set : key '" + key + "', " + "value : " + stringValueOperations.get(key));
		if(stringValueOperations.get(key).equals(value)) {
			return true;
		}
		return false;
    }
}