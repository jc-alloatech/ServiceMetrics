/**
 * Proprietary and Confidential AlloaTech, LLC. This document contains material which is proprietary and confidential property of ThorCode. The right
 * to view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of ThorCode Copyright
 * &copy; 2017 Initial commit: Jul 7, 20178:27:38 AM User: thor
 */
package com.alloatech.metrics;

import java.time.LocalDateTime;
import com.alloatech.metrics.Node.NodeBuilder;
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
@JsonDeserialize(builder = NodeBuilder.class)
public class Node {

    private final String id;
    private final int size;
    private final Font font;
    private final String label;
    private final String title;
    private final String group;
    private final String shape;
    private final Color color;
    private final FixedCoordinateDescriptor fixed;
    private final Icon icon;
    private final int x;
    private final int y;

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

    public static Node generate(Font font, String id, int size, String title, String group, String shape, Color color, String label,
            FixedCoordinate coords, Icon icon) {
        Node result = null;
        if (coords == null) {
            result = Node.builder().font(font).id(id).size(size).title(title).group(group).shape(shape).color(color).label(label).icon(icon).build();
        } else {
            result = Node.builder().font(font).id(id).size(size).title(title).group(group).shape(shape).color(color).label(label).icon(icon)
                    .fixed(coords.fixed()).x(coords.x()).y(coords.y()).build();
        }
        return result;
    }
}
