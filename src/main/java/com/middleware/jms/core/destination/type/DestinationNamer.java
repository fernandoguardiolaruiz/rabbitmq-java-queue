package com.middleware.jms.core.destination.type;

import com.middleware.jms.annotations.JmsDestination;
import org.apache.log4j.Logger;

import java.util.Optional;

public class DestinationNamer {

    private static Logger logger = Logger.getLogger(DestinationNamer.class);

    public static String getDestinationSuffixName(JmsDestination jmsDestination) {

        DestinationSuffix destinationSuffix = Optional.ofNullable(jmsDestination.clazzSuffix()).map(c -> {
            DestinationSuffix desSuf = null;
            try {
                desSuf = c.newInstance();
            } catch (Exception ex) {
                logger.error(ex);
            }
            return desSuf;
        }).orElse(null);
        return jmsDestination.name() + (destinationSuffix.version() == null ? "" : "-" + destinationSuffix.version());
    }

    public static String getExchangeSuffixName(JmsDestination jmsDestination) {

        DestinationSuffix destinationSuffix = Optional.ofNullable(jmsDestination.clazzSuffix()).map(c -> {
            DestinationSuffix desSuf = null;
            try {
                desSuf = c.newInstance();
            } catch (Exception ex) {
                logger.error(ex);
            }
            return desSuf;
        }).orElse(null);
        return jmsDestination.exchange() +
                (destinationSuffix.version() == null ? "" : "-" + destinationSuffix.version());
    }

}
