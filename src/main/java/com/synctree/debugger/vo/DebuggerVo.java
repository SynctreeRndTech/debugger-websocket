package com.synctree.debugger.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebuggerVo {

	private String extraId;
	private String sessionId;
	private String lockKey;
	
	@Override
	public String toString() {
		return "DebuggerVo [extraId=" + extraId + ", sessionId=" + sessionId + ", lockKey=" + lockKey + "]";
	}

}
