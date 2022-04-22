package com.synctree.debugger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import com.synctree.debugger.util.logging.DebuggerLogger;

@SpringBootApplication
public class DebuggerWebsocketApplication {

	private static final DebuggerLogger logger = new DebuggerLogger(DebuggerWebsocketApplication.class.getName());
	
	@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
	
	public static void main(String[] args) {
		SpringApplication.run(DebuggerWebsocketApplication.class, args);
		logger.info("================== DebuggerWebsocketApplication Started >>>>>>>>>>>>>>>>>>");
	}

}
