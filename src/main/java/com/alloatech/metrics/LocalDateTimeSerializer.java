/**
 * Proprietary and Confidential
 * AlloaTech
 *   
 * 	This document contains material which is proprietary and confidential 
 *  property of AlloaTech.
 *   
 *  The right to view, reproduce, modify, distribute, or in any way display
 *  this work is prohibited without the expressed written consent of 
 *  AlloaTech
 *
 *  Copyright &copy; 2017
 *  Initial commit:  Feb 5, 201711:58:22 AM
 *   
 */
package com.alloatech.metrics;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime dateTime, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}