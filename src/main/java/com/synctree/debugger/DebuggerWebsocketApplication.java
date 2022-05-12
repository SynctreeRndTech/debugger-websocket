package com.synctree.debugger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import logger.DebuggerLogger;

@SpringBootApplication
public class DebuggerWebsocketApplication {

	private static final DebuggerLogger logger = new DebuggerLogger(DebuggerWebsocketApplication.class.getName());
	
	@Profile({"dev", "test"})
	@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
	
	public static void main(String[] args) {
		SpringApplication.run(DebuggerWebsocketApplication.class, args);
		logger.info(">>>>>>>>>>>>>>>>>> DebuggerWebsocketApplication Started >>>>>>>>>>>>>>>>>>");
	}

}
