/**
 * Proprietary and Confidential AlloaTech This document contains material which is proprietary and confidential property of AlloaTech. The right to
 * view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of AlloaTech Copyright
 * &copy; 2017 Initial commit: Feb 5, 201712:06:24 PM
 */
package com.alloatech.metrics;

import java.io.IOException;
import java.time.LocalDate;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author thor
 */
public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            String s = jp.getText().trim();
            if (s.length() == 0)
                return null;
            return LocalDate.parse(s);
        }
        throw context.wrongTokenException(jp, JsonToken.NOT_AVAILABLE, "expected JSON Array, String or Number");
    }
}