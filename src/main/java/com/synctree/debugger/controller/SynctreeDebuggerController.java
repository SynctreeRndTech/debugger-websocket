package com.synctree.debugger.controller;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.synctree.debugger.vo.DebuggerVo;

import lombok.RequiredArgsConstructor;
import com.synctree.debugger.handler.WebSocketHttpHandler;
import com.synctree.debugger.logger.DebuggerLogger;
import com.synctree.debugger.util.redis.RedisUtil;

@RestController
@RequiredArgsConstructor
public class SynctreeDebuggerController {
	
	private static final DebuggerLogger logger = new DebuggerLogger(SynctreeDebuggerController.class.getName());
	
	private final WebSocketHttpHandler websock;
	private final RedisUtil redisUtil;
	private HashMap<String, Object> hashmap = new HashMap<>();

	@PostMapping(value="/debugger-websock", produces="application/json;charset=UTF-8")
	public JSONObject debuggerController(@RequestBody DebuggerVo debuggerVo) throws Exception {

		logger.info("[debugger websocket called] / session_id: " + debuggerVo.getSessionId() + ", spin_lock_key: "+ debuggerVo.getLockKey() + ", extra_id: " + debuggerVo.getExtraId());
		
		if(debuggerVo.getLockKey() != null && debuggerVo.getExtraId() != null && debuggerVo.getSessionId() != null) {
			try {
				boolean lockResult = redisUtil.setRedisStringValue(debuggerVo.getLockKey(), "1"); //Lock spin lock (unlock:"0", lock:"1")
				if (lockResult == true) {
					String resultMsg = websock.sendMessageToSession(debuggerVo);
					if (resultMsg != null) {
						hashmap.put("result", true);
						hashmap.put("result_code", "S0005"); //ex)S0001(성공), E0001~E0002(프론트에 리턴되는 에러메세지), E0005(백엔드에서 파싱되는 에러메세지)
						hashmap.put("result_msg", resultMsg);
					} else {
						hashmap.put("result", false);
						hashmap.put("result_code", "E0001");
						hashmap.put("result_msg", resultMsg);
					}
				} else {
					hashmap.put("result", false);
					hashmap.put("result_code", "E0002");
					hashmap.put("result_msg", "Lock failed");
				}
			} catch (NullPointerException e) {
				logger.error("[NullPointerException] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
				logger.error("[NullPointerException] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
				hashmap.put("result", false);
				hashmap.put("result_code", "E0005");
				hashmap.put("result_msg", "WebSock NullPointerException");
			} catch (IOException e) {
				logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
				logger.error("[IOException] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
				hashmap.put("result", false);
				hashmap.put("result_code", "E0005");
				hashmap.put("result_msg", "WebSock IOException_occured");
			} catch (Exception e) {
				logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", message: " + e.getMessage());
				logger.error("[Exception] / session_id:" + debuggerVo.getSessionId() + ", cause: " + e.getCause());
				hashmap.put("result", false);
				hashmap.put("result_code", "E0005");
				hashmap.put("result_msg", "WebSock Exception_occured");
				//e.printStackTrace(); //CWE-497
			} 
		} else {
			logger.info("[null check] / session_id: " + debuggerVo.getSessionId() + ", lock_key: " +debuggerVo.getLockKey() + ", extra_id: " + debuggerVo.getExtraId());
			websock.sendMessageToSession(debuggerVo);
			hashmap.put("result", false);
			hashmap.put("result_code", "E0005");
			hashmap.put("result_msg", "Received params were null"); //lockKey, sessionId 없는 경우 return false -> 디버깅 중단
		}
		
		JSONObject jsonObj = new JSONObject(hashmap);
		return jsonObj;
	}
	
	@PostMapping(value="/echo")
	public String serverEchoTest(String echoMsg) throws Exception {
		
		return "echo " + echoMsg;
	}
}
