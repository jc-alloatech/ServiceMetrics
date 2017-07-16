/**
 * Proprietary and Confidential AlloaTech, LLC. This document contains material which is proprietary and confidential property of ThorCode. The right
 * to view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of ThorCode Copyright
 * &copy; 2017 Initial commit: Jul 7, 20178:27:38 AM User: thor
 */
package com.alloatech.metrics;

import java.time.LocalDateTime;
import java.util.List;
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
@JsonDeserialize(builder = ServiceSummaryMetrics.ServiceSummaryMetricsBuilder.class)
public class ServiceSummaryMetrics {

    private final int callCount;
    private final LocalDateTime dateTimestampStart;
    private final LocalDateTime dateTimestampEnd;
    private final List<String> consumerIds;
    private final String serviceName;
    private final String capabilityGroupName;
    private final String capabilityName;
    private final String componentName;
    private final String interfaceName;
    private final String operationName;
    private final List<String> serviceVersions;

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
