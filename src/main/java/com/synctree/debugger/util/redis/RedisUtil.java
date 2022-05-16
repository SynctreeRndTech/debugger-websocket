package com.synctree.debugger.util.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.synctree.debugger.logger.DebuggerLogger;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisUtil {
	
	private static final DebuggerLogger logger = new DebuggerLogger(RedisUtil.class.getName());
	private final StringRedisTemplate stringRedisTemplate;

	public String getRedisStringValue(String key) {
		ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		return stringValueOperations.get(key);
	}

	public boolean setRedisStringValue(String key, String value) {
		ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		stringValueOperations.set(key, value); //value string type으로만 간단하게.
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

}

	