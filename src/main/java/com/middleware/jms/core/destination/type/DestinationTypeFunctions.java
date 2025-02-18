package com.middleware.jms.core.destination.type;

import com.google.common.base.Supplier;
import com.middleware.jms.core.resource.JmsResource;
import com.middleware.jms.core.resource.consumer.creator.DurableSubscriberCreator;
import com.middleware.jms.core.resource.consumer.creator.QueueDestinationCreator;
import com.middleware.jms.core.resource.consumer.creator.ValidTopicID;
import com.middleware.jms.annotations.JmsTopic;
import com.middleware.jms.annotations.JmsQueue;
import com.middleware.jms.core.destination.type.params.DurableSubscriberCreationParameters;
import com.middleware.jms.core.destination.type.params.QueueDestinationCreationParameters;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DestinationTypeFunctions {

    @JmsTopic
    static Map<DestinationTypeFunctionType, Supplier<Function<? extends DestinationTypeFunctionParameters, ? extends DestinationTypeFunctionResult>>> getDurableFunctions() {
        Map<DestinationTypeFunctionType, Supplier<Function<? extends DestinationTypeFunctionParameters, ? extends DestinationTypeFunctionResult>>> suppliersMap = new HashMap<>();
        suppliersMap.put(DestinationTypeFunctionType.VALIDATE_JMS_DESTINATION, () -> validateConsumerDurableJmsDestination());
        suppliersMap.put(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER_PARAMETERS, () -> destinationSessionDurableSubscriberCreationParametersBiFunction());
        suppliersMap.put(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER, () -> durableSubscriberCreator());
        return suppliersMap;
    }

    @JmsQueue
    static Map<DestinationTypeFunctionType, Supplier<Function<? extends DestinationTypeFunctionParameters, ? extends DestinationTypeFunctionResult>>> getTransientFunctions() {
        Map<DestinationTypeFunctionType, Supplier<Function<? extends DestinationTypeFunctionParameters, ? extends DestinationTypeFunctionResult>>> suppliersMap = new HashMap<>();
        suppliersMap.put(DestinationTypeFunctionType.VALIDATE_JMS_DESTINATION, () -> validateConsumerTransientJmsDestination());
        suppliersMap.put(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER_PARAMETERS, () -> queueConsumerCreationParametersBiFunction());
        suppliersMap.put(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER, () -> queueConsumerCreator());
        return suppliersMap;
    }

    private static Function<DestinationTypeMessageDestinationCreatorFunctionParameters, DurableSubscriberCreationParameters> destinationSessionDurableSubscriberCreationParametersBiFunction() {
        return parameters -> new DurableSubscriberCreationParameters(parameters.getJmsResourceDestination(), parameters.getSession());
    }

    private static Function<DestinationTypeMessageDestinationCreatorFunctionParameters, QueueDestinationCreationParameters> queueConsumerCreationParametersBiFunction() {
        return parameters -> new QueueDestinationCreationParameters(parameters.getJmsResourceDestination(), parameters.getSession());
    }

    private static <R extends JmsResource> Function<ValidateJmsDestinationParameters, ValidTopicID> validateConsumerDurableJmsDestination() {
        return parameters -> new ValidTopicID(!parameters.getJmsDestination().id().isEmpty());
    }

    private static <R extends JmsResource> Function<ValidateJmsDestinationParameters, ValidTopicID> validateConsumerTransientJmsDestination() {
        return parameters -> new ValidTopicID(Boolean.TRUE);
    }

    private static Function<VoidParameters, DurableSubscriberCreator> durableSubscriberCreator() {
        return v -> new DurableSubscriberCreator();
    }

    private static Function<VoidParameters, QueueDestinationCreator> queueConsumerCreator() {
        return v -> new QueueDestinationCreator();
    }

}
