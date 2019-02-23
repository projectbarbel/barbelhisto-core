package org.projectbarbel.histo.event;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSubscriberExceptionHandler implements SubscriberExceptionHandler {

	@Override
	public void handleException(Throwable exception, SubscriberExceptionContext context) {
		log.error("an exception was thrown while executing event handler: "
				+ context.getSubscriberMethod().getDeclaringClass().getName() + "."
				+ context.getSubscriberMethod().getName(), exception);
	}
	
}
