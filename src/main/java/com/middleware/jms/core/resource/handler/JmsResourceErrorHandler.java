package com.middleware.jms.core.resource.handler;

public interface JmsResourceErrorHandler<T> {

    void handleError(ErrorHandlerContext<T> errorHandlerContext);

}
