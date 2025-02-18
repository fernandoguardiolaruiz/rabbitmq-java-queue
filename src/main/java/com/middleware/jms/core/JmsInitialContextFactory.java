package com.middleware.jms.core;

import org.apache.log4j.Logger;
import org.apache.qpid.client.AMQTopic;

import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class JmsInitialContextFactory extends org.apache.qpid.jndi.PropertiesFileInitialContextFactory {

    private String QUEUE_PREFIX = "queue.";
    private String TOPIC_PREFIX = "topic.";

    private Logger logger = Logger.getLogger(JmsInitialContextFactory.class);

    protected void createQueues(Map data, Hashtable environment) {
        Iterator iter = environment.entrySet().iterator();

        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = entry.getKey().toString();
            if (key.startsWith(this.QUEUE_PREFIX)) {
                String jndiName = key.substring(this.QUEUE_PREFIX.length());
                Queue q = this.createQueue(entry.getValue());
                if (q != null) {
                    data.put(jndiName, q);
                }
            }
        }
    }


    protected void createTopics(Map data, Hashtable environment) {
        Iterator iter = environment.entrySet().iterator();

        while(true) {
            String jndiName;
            Topic t;
            do {
                Map.Entry entry;
                String key;
                do {
                    if (!iter.hasNext()) {
                        return;
                    }

                    entry = (Map.Entry)iter.next();
                    key = entry.getKey().toString();
                } while(!key.startsWith(this.TOPIC_PREFIX));

                jndiName = key.substring(this.TOPIC_PREFIX.length());
                t = this.createTopic(entry.getValue());
            } while(t == null);

            if (logger.isDebugEnabled()) {
                StringBuffer b = new StringBuffer();
                b.append("Creating the topic: " + jndiName + " with the following binding keys ");
                String[] var9 = ((AMQTopic)t).getBindingKeys();
                int var10 = var9.length;

                for(int var11 = 0; var11 < var10; ++var11) {
                    String binding = var9[var11];
                    b.append(binding).append(",");
                }

                this.logger.debug(b.toString());
            }
            data.put(jndiName, t);
        }
    }

}
