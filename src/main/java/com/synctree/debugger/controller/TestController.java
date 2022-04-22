package com.synctree.debugger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.synctree.debugger.util.logging.DebuggerLogger;

@RestController
public class TestController {
	private static final DebuggerLogger logger = new DebuggerLogger(TestController.class.getName());

	@GetMapping("/")
	public String main(String[] args) {
		logger.info("Call succeed");
		return "Hello";
	}
}
