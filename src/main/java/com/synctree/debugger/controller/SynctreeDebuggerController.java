package com.synctree.debugger.controller;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.synctree.debugger.vo.DebuggerVo;
import logger.DebuggerLogger;
import lombok.RequiredArgsConstructor;
import com.synctree.debugger.handler.WebSocketHttpHandler;
import com.synctree.debugger.util.redis.RedisUtil;

@RestController
@RequiredArgsConstructor
public class SynctreeDebuggerController {
	
	private static final DebuggerLogger logger = new DebuggerLogger(SynctreeDebuggerController.class.getName());
	
	private final WebSocketHttpHandler websock;
	private final RedisUtil redisUtil;
	private JSONObject jsonObj = new JSONObject();

	@PostMapping(value="/debugger-websock", produces="application/json;charset=UTF-8")
	public void debuggerController(@RequestBody DebuggerVo debuggerVo) throws Exception {

		logger.info("[debugger websocket called] / spin_lock_key: "+ debuggerVo.getLockKey() + ", extra_id: " + debuggerVo.getExtraId() + ", session_id: " + debuggerVo.getSessionId());
		
		if(debuggerVo.getLockKey() != null && debuggerVo.getExtraId() != null && debuggerVo.getSessionId() != null) {
			try {
				boolean lockResult = redisUtil.setRedisStringValue(debuggerVo.getLockKey(), "1"); //Lock spin lock (unlock:"0", lock:"1")
				if (lockResult == true) {
					boolean sendResult = websock.sendMessageToSession(debuggerVo);
					if (sendResult == true) {
						jsonObj.put("result", true);
						jsonObj.put("result_msg", "delivery_succeed");
					} else {
						jsonObj.put("result", false);
						jsonObj.put("result_msg", "delivery_failed");
					}
				} else {
					jsonObj.put("result", false);
					jsonObj.put("result_msg", "lock_failed");
				}
			} catch (NullPointerException e) {
				logger.error("[exception_message] / " + e.getMessage());
				logger.error("[exception_cause] / " + e.getCause());
			} catch (IOException e) {
				logger.error("[exception_message] / " + e.getMessage());
				logger.error("[exception_cause] / " + e.getCause());
			} catch (Exception e) {
				logger.error("[exception_message] / " + e.getMessage());
				logger.error("[exception_cause] / " + e.getCause());
				//e.printStackTrace(); //CWE-497
			}
		} else {
			logger.info("[null check] / lock_key: " +debuggerVo.getLockKey() + ", extra_id: " + debuggerVo.getExtraId() + ", session_id: " + debuggerVo.getSessionId());
			jsonObj.put("result", false);
			jsonObj.put("result_msg", "get_params_failed");
		}
		//return jsonObj;
	}
	
	@GetMapping(value="/echo")
	public String serverEchoTest() throws Exception {
		
		return "echo ";
	}
}
