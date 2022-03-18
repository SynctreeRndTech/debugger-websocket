package com.synctree.stomp;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController {


	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
		Thread.sleep(1000); // simulated delay
		System.out.println("message 찍어보기 : " + message.getName());
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
		/*
		 * Internally, the implementation of the method simulates a processing delay by
		 * causing the thread to sleep for one second. This is to demonstrate that,
		 * after the client sends a message, the server can take as long as it needs to
		 * asynchronously process the message. The client can continue with whatever
		 * work it needs to do without waiting for the response.
		 */
	}
}
