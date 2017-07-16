/**
 * Proprietary and Confidential AlloaTech This document contains material which is proprietary and confidential property of AlloaTech. The right to
 * view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of AlloaTech Copyright
 * &copy; 2017 Initial commit: Feb 6, 20175:04:03 PM
 */
package com.alloatech.metrics;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * @author thor
 */
public class Mapper {

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MainModule", new Version(1, 0, 0, "Snapshot", "com.alloa", "test-loader"));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeSerializer());
        mapper.registerModule(module).setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                .enable(SerializationFeature.INDENT_OUTPUT).setSerializationInclusion(Include.NON_NULL)
                .registerModule(new Jdk8Module());
        // .registerModule(new JavaTimeModule());
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
