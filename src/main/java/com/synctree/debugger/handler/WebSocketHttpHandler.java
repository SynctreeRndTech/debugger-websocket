package com.synctree.debugger.handler;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;
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
	
	//private final StringRedisTemplate stringRedisTemplate;
	private final RedisUtil redisUtil;
	
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.put(session.getId(), session);
        logger.info("[new connection established] / session_id: "+session.getId() + ", session_list: " + sessions.keySet().toString());
        //logger.info("::: connected client ::: "+ session.getRemoteAddress().toString());
        //logger.info("::: handshake headers ::: "+ session.getHandshakeHeaders().toString());
        
    	JSONObject jsonObj = new JSONObject();
        jsonObj.put("msg_type", "text");
        jsonObj.put("msg_data", "session_id");
        jsonObj.put("id", session.getId());
        
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
    		jsonObj.put("msg_type", "text");
    		jsonObj.put("msg_data", "unlock_result");
    		if (lockKey != null ) {
        		boolean result = redisUtil.setRedisStringValue(lockKey, "0"); //Release spin lock (unlock:"0", lock:"1")
        		if(result == true ) {
                    jsonObj.put("result_msg", "spin_lock_released");
        		} else {
        			jsonObj.put("result_msg", "release_spin_lock_failed");
        		}
                session.sendMessage(new TextMessage(jsonObj.toString()));
    		} else {
                logger.info("[message handled] / session_id '" + sessionID + "''s lock_key is null");
    			jsonObj.put("result_msg", "lock_key_is_null" );
    			session.sendMessage(new TextMessage(jsonObj.toString()));
    		}
    	} else if (payloadSplit[0].equals("GET_SESSION_ID")){
    		jsonObj.put("msg_type", "text");
            jsonObj.put("msg_data", "session_id");
            jsonObj.put("id", session.getId());
            
            session.sendMessage(new TextMessage(jsonObj.toString()));
            
    	} else {
    		jsonObj.put("msg_type", "text");
            jsonObj.put("msg_data", "echo_message");
            jsonObj.put("payload", message.getPayload());
            
            session.sendMessage(new TextMessage(jsonObj.toString()));
    	}

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        logger.info("[client '"+ session.getId() + "' session closed]");
        sessions.remove(session.getId());
    }
    
    public boolean sendMessageToSession(DebuggerVo debuggerVo) throws Exception {
        if (sessions == null) {
        	logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: sessionList is null");
            return false; //To-Do: 에러 처리
        }
        
        WebSocketSession singleSession = sessions.get(debuggerVo.getSessionId());
    	JSONObject jsonObj = new JSONObject();
    	
        try {    	
        	
	        if(singleSession != null) {
	        	lockKeyMap.put(debuggerVo.getSessionId(), debuggerVo.getLockKey());
	            String extraId = debuggerVo.getExtraId();
	    		jsonObj.put("msg_type", "json");
	            jsonObj.put("msg_data", "extra_id");
	            jsonObj.put("id", extraId);
	            
	            logger.info("======testtest=======" + jsonObj.toString());
	            singleSession.sendMessage(new TextMessage(jsonObj.toString()));
	
	        } else {
	        	logger.info("[send_message_to_session failed] / session_id: " + debuggerVo.getSessionId() + ", message: single_session is null");
	        	return false;
	        }
		} catch (IOException e) {
			logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
			logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
		} catch (Exception e) {
			logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
			logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
		}
        
        return true;
    }
}