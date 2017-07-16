/**
 * Proprietary and Confidential AlloaTech, LLC. This document contains material which is proprietary and confidential property of ThorCode. The right
 * to view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of ThorCode Copyright
 * &copy; 2017 Initial commit: Jul 7, 20178:27:38 AM User: thor
 */
package com.alloatech.metrics;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

/**
 * @author thor
 */
@Data
@Builder
@Accessors(fluent = true, chain = true)
@JsonDeserialize(builder = NodeDataSet.NodeDataSetBuilder.class)
public class NodeDataSet {

    @Singular private final List<Node> nodes;
    @Singular private final List<Edge> edges;

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

    public static NodeDataSet generate(List<Node> nodeList, List<Edge> edgeList) {
        return builder().nodes(nodeList).edges(edgeList).build();
    }

    public NodeDataSet addNodes(List<Node> newNodes) {
        newNodes.addAll(nodes);
        return NodeDataSet.builder().nodes(newNodes).edges(edges).build();
    }

    public NodeDataSet addEdges(List<Edge> newEdges) {
        newEdges.addAll(edges);
        return NodeDataSet.builder().nodes(nodes).edges(newEdges).build();
    }

    public NodeDataSet addDataSet(NodeDataSet dataSet) {
        List<Node> newNodes = new ArrayList<>(nodes);
        newNodes.addAll(dataSet.nodes());
        List<Edge> newEdges = new ArrayList<>( edges);
        newEdges.addAll(dataSet.edges());
        return NodeDataSet.builder().nodes(newNodes).edges(newEdges).build();
    }
}
