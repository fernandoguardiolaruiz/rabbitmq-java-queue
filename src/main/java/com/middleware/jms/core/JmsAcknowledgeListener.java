package com.middleware.jms.core;

import javax.jms.Message;

public interface JmsAcknowledgeListener {

    void acknowledge(Message message);

}
