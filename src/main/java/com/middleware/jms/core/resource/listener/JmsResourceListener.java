package com.middleware.jms.core.resource.listener;

import java.util.Properties;

public interface JmsResourceListener {

    void onBeforeProcessingMessage(Properties properties);

}
