package com.middleware.jms.core.destination.type;

import com.google.common.base.Supplier;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class DestinationTypeFunctionExecutor<P extends DestinationTypeFunctionParameters, R extends DestinationTypeFunctionResult> {

    private Logger logger = Logger.getLogger(DestinationTypeFunctionExecutor.class);

    public R execute(DestinationTypeFunctionType destinationTypeFunctionType, DestinationType destinationType, P consumerTypeFunctionParameters) throws JMSException {
        Supplier<Function<P, R>> supplierFunction = getSupplierFunction(destinationTypeFunctionType, destinationType);
        Function<P, R> destinationTypeFunction = supplierFunction.get();
        return destinationTypeFunction.apply(consumerTypeFunctionParameters);
    }

    private Supplier<Function<P, R>> getSupplierFunction(DestinationTypeFunctionType destinationTypeFunctionType, DestinationType destinationType)
            throws JMSException {
        return Arrays.stream(DestinationTypeFunctions.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(destinationType.getMethodAnnotation()))
                .findFirst().map(m -> {
                    Map<DestinationTypeFunctionType, Supplier<Function<P, R>>> supplierMap = null;
                    try {
                        supplierMap = (Map) m.invoke(null);
                    } catch (Exception ex) {
                        logger.error("Error invoking method " + m.getName() + " for consumerType " + destinationType);
                    }
                    return supplierMap;
                }).map(sm -> sm.get(destinationTypeFunctionType))
                .orElseThrow(() -> new JMSException("Can't execute function " + destinationTypeFunctionType + " for consumerType " + destinationType));
    }

}
