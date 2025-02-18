package com.middleware.jms.connection;

import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;
import org.apache.qpid.transport.Connection;
import org.apache.qpid.transport.ConnectionException;
import org.apache.qpid.transport.ConnectionListener;

import java.util.List;

public class JmsConnectionListener implements ConnectionListener {

    private final ObjectPool<Connection> connectionPool;
    private Logger logger = Logger.getLogger(JmsConnectionListener.class);

    public JmsConnectionListener(ObjectPool<Connection> connectionPool) {
        this.connectionPool = connectionPool;
    }


    @Override
    public void opened(Connection connection) {
        logger.info("Connection open with UserID: " + connection.getUserID());
    }

    @Override
    public void exception(Connection connection, ConnectionException e) {
        try {
            logger.error("Connection exception, " + connection.getUserID(), e);
            connectionPool.invalidateObject(connection);
        } catch (Exception ex) {
            logger.error("Error invalidating connection " + connection.getUserID());
        }
    }

    @Override
    public void closed(Connection connection) {
        logger.info("Closed connnection with UserID " + connection.getUserID());
    }

    @Override
    public boolean redirect(String s, List<Object> list) {
        return false;
    }
}
