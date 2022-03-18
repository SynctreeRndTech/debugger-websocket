package com.synctree.debugger.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisHandler {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();

	public String getRedisStringValue(String key) {
		ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		return stringValueOperations.get(key);
	}

	public boolean setRedisStringValue(String key, String value) {
		ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
		stringValueOperations.set(key, value);
		System.out.println("Redis Set : key '" + key + "', " + "value : " + stringValueOperations.get(key));
		if(stringValueOperations.get(key).equals(value)) {
			return true;
		}
		return false;
	}

}

	