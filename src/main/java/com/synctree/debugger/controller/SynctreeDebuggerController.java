package com.synctree.debugger.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.synctree.debugger.vo.DebuggerVo;

import lombok.RequiredArgsConstructor;

import com.synctree.debugger.handler.RedisHandler;
import com.synctree.debugger.handler.WebSocketHttpHandler;

@RestController
@RequiredArgsConstructor
public class SynctreeDebuggerController {
	
	//private static EscapeUnescapeUtil escapeUtil;
	//private static WebSocketWsHandler webSockWs;
	private final WebSocketHttpHandler websock;
	private final RedisHandler redisHandler;
	
	@Autowired
	private static StringRedisTemplate stringRedisTemplate;

	@PostMapping(value="/debugger-test", produces="applicaion/json;charset=UTF-8")
	public void debuggerTest(@RequestBody DebuggerVo debuggerVo) throws Exception {
		
		//System.out.println("====================debugger controller called====================");
		//String extraId = escapeUtil.unescape(debuggerVo.getExtraId().toString());
		
		System.out.println("::: SpinkLockKey ::: "+ debuggerVo.getLockKey());
		System.out.println("::: ExtraID ::: "+ debuggerVo.getExtraId());
		System.out.println("::: SessionID ::: "+ debuggerVo.getSessionId());
		
		if(debuggerVo.getLockKey() != null && debuggerVo.getExtraId() != null && debuggerVo.getSessionId() != null) {
			boolean result = redisHandler.setRedisStringValue(debuggerVo.getLockKey(), "1"); //스핀락 잠금
			
			if(result == true) {
				websock.sendMessageToOne(debuggerVo);
				//webSockWs.sendMessageToAll(debuggerVo.getExtraId(), debuggerVo.getSessionId());
			} else {
				//TO-DO: getRedisStringValue로 값 확인
			}
		} else {
			//TO-DO: 인자값 Null 체크 및 에러 처리
		}
	
		//System.out.println("====================debugger controller finished====================");
	}
	
	@GetMapping(value="/test")
	public String serverTest() throws Exception {
		
		return "Hello";
	}
	
	
	public boolean setRedisStringValue(String key, String value) {
		ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		stringValueOperations.set(key, value);
		//System.out.println("Redis Set : key '" + key + "', " + "value : " + stringValueOperations.get(key));
		if(stringValueOperations.get(key).equals(value)) {
			return true;
		}
		return false;
	}
	
}
