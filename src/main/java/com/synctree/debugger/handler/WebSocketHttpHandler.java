package com.synctree.debugger.handler;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.synctree.debugger.logger.DebuggerLogger;
import com.synctree.debugger.util.redis.RedisUtil;
import com.synctree.debugger.vo.DebuggerVo;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class WebSocketHttpHandler extends TextWebSocketHandler {

	private static final DebuggerLogger logger = new DebuggerLogger(WebSocketHttpHandler.class.getName());
	
	//private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();
	private static HashMap<String, WebSocketSession> sessions = new HashMap<String, WebSocketSession>();
	private HashMap<String, String> lockKeyMap = new HashMap<String, String>();
	private HashMap<String, Object> hashmap = new HashMap<>();
	//private final StringRedisTemplate stringRedisTemplate;
	private final RedisUtil redisUtil;
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.put(session.getId(), session);
        logger.info("[new connection established] / session_id: "+session.getId() + ", session_list: " + sessions.keySet().toString());
        //logger.info("::: connected client ::: "+ session.getRemoteAddress().toString());
        //logger.info("::: handshake headers ::: "+ session.getHandshakeHeaders().toString());
        
    	hashmap.put("msg_type", "text");
    	hashmap.put("msg_data", "session_id");
    	hashmap.put("id", session.getId());
        
    	JSONObject jsonObj = new JSONObject(hashmap);
        session.sendMessage(new TextMessage(jsonObj.toString()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	
    	String payload = message.getPayload();
    	String payloadSplit[] = payload.split(",");
    	String sessionID = session.getId();
    	JSONObject jsonObj = new JSONObject();
    	
    	logger.info("[message handled] / session_id: " + sessionID + ", payload_message: " + payloadSplit[0]); 
    	
    	if(payloadSplit[0].equals("request_for_unlock")){
        	String lockKey = lockKeyMap.get(sessionID);
        	hashmap.put("msg_type", "text");
        	hashmap.put("msg_data", "unlock_result");
    		if (lockKey != null ) {
        		boolean result = redisUtil.setRedisStringValue(lockKey, "0"); //Release spin lock (unlock:"0", lock:"1")
        		if(result == true ) {
        			hashmap.put("result_msg", "Spinlock released");
        		} else {
        			hashmap.put("result_msg", "Release spinlock failed");
        		}
        		jsonObj.putAll(hashmap);
                session.sendMessage(new TextMessage(jsonObj.toString()));
    		} else {
                logger.info("[message handled] / session_id '" + sessionID + "''s lock_key is null");
                hashmap.put("result_msg", "LockKey is null" );
    			session.sendMessage(new TextMessage(jsonObj.toString()));
    		}
    	} else if (payloadSplit[0].equals("GET_SESSION_ID")){
    		hashmap.put("msg_type", "text");
    		hashmap.put("msg_data", "session_id");
    		hashmap.put("id", session.getId());
            
    		jsonObj.putAll(hashmap);
            session.sendMessage(new TextMessage(jsonObj.toString()));
            
    	} else if(payloadSplit[0].equals("request_for_close")){
    		session.close();
    	} else {
    		hashmap.put("msg_type", "text");
    		hashmap.put("msg_data", "echo_message");
    		hashmap.put("payload", message.getPayload());
            
    		jsonObj.putAll(hashmap);
            session.sendMessage(new TextMessage(jsonObj.toString()));
    	}

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        logger.info("[client '"+ session.getId() + "' session closed]");
        sessions.remove(session.getId());
    }
    
    public String sendMessageToSession(DebuggerVo debuggerVo) throws Exception {
    	
    	WebSocketSession singleSession = null;
    	JSONObject jsonObj = new JSONObject();
    	
        if (sessions == null) {
        	logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: session_list is null");
        	return "sessions_all_null";
        }
        
        if(debuggerVo.getSessionId() != null) {
            singleSession = sessions.get(debuggerVo.getSessionId());
        } else {
        	logger.info("[send_message_to_session failed] / session_id: null, message: session_id is null");
        	return "session_id_null";
        }
        
        try {    	
        	
	        if(singleSession != null) {
	        	if(debuggerVo.getLockKey() != null) {
	        		if(debuggerVo.getExtraId() != null) {
			        	lockKeyMap.put(debuggerVo.getSessionId(), debuggerVo.getLockKey());
			        	hashmap.put("msg_type", "json");
			        	hashmap.put("msg_data", "extra_id");
			        	hashmap.put("id", debuggerVo.getExtraId());
			            
			        	jsonObj.putAll(hashmap);
			            singleSession.sendMessage(new TextMessage(jsonObj.toString()));
	        		} else {
			        	lockKeyMap.put(debuggerVo.getSessionId(), debuggerVo.getLockKey());
			        	hashmap.put("msg_type", "json");
			        	hashmap.put("msg_data", "error_msg");
			        	hashmap.put("msg", "ExtraId is null");
			            
			        	jsonObj.putAll(hashmap);
			            singleSession.sendMessage(new TextMessage(jsonObj.toString()));
			            
	        			logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: extra_id is null");
	        			return "ExtraId is null";
	        		}
	        	} else {
		        	logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: lock_key is null");
		        	return "LockKey is null";
	        	}
	        } else {
	        	logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: single_session is null");
	        	return "Failed to find session";
	        }
		} catch (IOException e) {
			logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
			logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
		} catch (Exception e) {
			logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
			logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
		}
        
        return "Delivery Succeed";
    }
}