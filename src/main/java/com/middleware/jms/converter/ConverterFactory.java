package com.middleware.jms.converter;

import javax.ws.rs.core.MediaType;

public class ConverterFactory {



    public static <T> Converter createConverter(String mediaType, Class<T> clazz, Class<?>... generalizaedType) throws ConverterException {
        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            return new JsonConverter(clazz, generalizaedType);
        } else if (mediaType.equals(MediaType.APPLICATION_XML)) {
            return new XmlConverter(clazz);
        } else {
            throw new ConverterException("Invalid MediaType");
        }
    }

}
