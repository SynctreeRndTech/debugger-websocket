package com.synctree.debugger.util.redis;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.synctree.debugger.logger.DebuggerLogger;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisUtil {
	
	private static final DebuggerLogger logger = new DebuggerLogger(RedisUtil.class.getName());
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Resource(name = "redisTemplate")
	private ValueOperations<String, String> stringValueOperations;
	
	@Resource(name = "stringRedisTemplate")
	private HashOperations<String, Object, Object> hashValueOperations;
	
	
	public String getRedisStringValue(String key) {
		return stringValueOperations.get(key);
	}
	
	public Object getRedisHashValue(String key, String field) {
		return hashValueOperations.get(key, field);
	}

	public boolean setRedisStringValue(String key, String value) {
		stringValueOperations.set(key, value);
		//logger.info("Redis Set : key '" + key + "', " + "value : " + stringValueOperations.get(key));

		if(stringValueOperations.get(key).equals(value)) {
			
			if(value == "0") {
				logger.info("[spin_lock_status_changed] / unlocked");
			} else if (value == "1"){
				logger.info("[spin_lock_status_changed] / locked");
			} else {
				logger.info("[spin_lock_status_changed] / " + value);
			}
			
			return true;
		}
		logger.info("[spin_lock_changing_status_failed]");
		return false;
	}

	public boolean setRedisHashValue(String key, String field, String value) {
		hashValueOperations.put(key, field, value);
	   
		if(hashValueOperations.get(key, field).equals(value)) {
			if(value == "0") {
				logger.info("[spin_lock_status_changed] / unlocked");
			} else if (value == "1"){
				logger.info("[spin_lock_status_changed] / locked");
			} else {
				logger.info("[spin_lock_status_changed] / " + value);
			}
			return true;
		}
		logger.info("[spin_lock_changing_status_failed]"); //jianna
		return false;
	}
}

	