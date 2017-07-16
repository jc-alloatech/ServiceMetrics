/**
 * Proprietary and Confidential
 *           AlloaTech, LLC.
 *   
 * 	This document contains material which is proprietary and confidential 
 *  property of ThorCode.
 *   
 *  The right to view, reproduce, modify, distribute, or in any way display
 *  this work is prohibited without the expressed written consent of 
 *  ThorCode
 *
 *  Copyright &copy; 2017
 *  Initial commit:  Jul 7, 20178:27:38 AM
 *  User:  thor 
 */
package com.alloatech.metrics;

import com.alloatech.metrics.Edge.EdgeBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author thor
 */
@Data
@Builder
@Accessors(fluent = true, chain = true)
@JsonDeserialize(builder = EdgeBuilder.class)
public class Edge {
    
    private final String from;
    private final String to;
    private final int width = 100;
    private final Color color = Color.builder().build();
    private final String label;
    
    public String toJson() {
        String result = "<null>";
        try {
            result = Mapper.getMapper().writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            System.out.println(e.toString());
        }
        return result;
    }
    
}
