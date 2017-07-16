/**
 * Proprietary and Confidential AlloaTech, LLC. This document contains material which is proprietary and confidential property of ThorCode. The right
 * to view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of ThorCode Copyright
 * &copy; 2017 Initial commit: Jul 7, 20178:39:36 AM User: thor
 */
package com.alloatech.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author thor
 */
public class ReportRunner {

    private static Map<String, FixedCoordinate> fixedCoordinateMap;

    public static void main(String[] args) {
        String file = "c:/tmp/data/consumer_hr_summary.csv";
        FileParser parser = new FileParser();
        List<ServiceMetrics> results = parser.parseFile(file);
        List<ServiceSummaryMetrics> summaryResults = new ArrayList<>();
        file = "c:/tmp/data/consumer_hr_summary2.csv";
        System.out.println("Done with " + file + " - Record Count:" + results.size());
        results.addAll(parser.parseFile(file));
        System.out.println("Done with " + file + " - Record Count:" + results.size());
        // COLLECT ALL THE SAME INTERFACES TO CONSUMERS WHILE ADDING UP THE COUNTS
        ConcurrentMap<String, List<ServiceMetrics>> groupedMetrics = results.parallelStream()
                .collect(Collectors.groupingByConcurrent(ServiceMetrics::interfaceName));
        for (String interfaceName : groupedMetrics.keySet()) {
            List<ServiceMetrics> metricsList = groupedMetrics.get(interfaceName);
            Integer summedCalls = metricsList.parallelStream().collect(Collectors.summingInt(ServiceMetrics::callCount));
            LocalDateTime startDate = metricsList.stream().map(m -> m.dateTimestamp()).min(LocalDateTime::compareTo).get();
            LocalDateTime endDate = metricsList.stream().map(m -> m.dateTimestamp()).max(LocalDateTime::compareTo).get();
            List<String> consumerIds = metricsList.stream().map(m -> m.consumerId()).distinct().collect(Collectors.toList());
            List<String> serviceVersions = metricsList.stream().map(m -> m.serviceVersion()).distinct()
                    .collect(Collectors.toList());
            ServiceMetrics existingMetric = metricsList.get(0);
            ServiceSummaryMetrics newSummaryMetric = ServiceSummaryMetrics.builder().callCount(summedCalls)
                    .dateTimestampStart(startDate).dateTimestampEnd(endDate).consumerIds(consumerIds)
                    .serviceVersions(serviceVersions).capabilityGroupName(existingMetric.capabilityGroupName())
                    .capabilityName(existingMetric.capabilityName()).componentName(existingMetric.componentName())
                    .serviceName(existingMetric.serviceName()).interfaceName(existingMetric.interfaceName())
                    .operationName(existingMetric.operationName()).build();
            summaryResults.add(newSummaryMetric);
        }
        summaryResults.stream().sorted(new Comparator<ServiceSummaryMetrics>() {

            @Override
            public int compare(ServiceSummaryMetrics o1, ServiceSummaryMetrics o2) {
                if (o1.consumerIds().size() > o2.consumerIds().size()) {
                    return -1;
                } else if (o1.consumerIds().size() == o2.consumerIds().size()) { return 0; }
                return 1;
            }
        }).forEach(m -> System.out.println(m.toJson()));
        try {
            Files.write(Paths.get("C:/Users/thor/workspace/service-metrics-web/src/assets/graph_data.json"),
                    generateDataSet(summaryResults).toJson().getBytes(), StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Done.");
    }

    private static NodeDataSet generateDataSet(List<ServiceSummaryMetrics> summaryResults) {
        int scale = 2;
        List<String> capabilityGroups = summaryResults.stream().map(r -> r.capabilityGroupName()).distinct()
                .collect(Collectors.toList());
        List<Node> nodeList = new ArrayList<>();
        List<Edge> edgeList = new ArrayList<>();
        for (String curCapGroup : capabilityGroups) {
            FixedCoordinate coords = getFixedPosition(curCapGroup);
            Node capGroup = Node.generate(Font.builder().size(480 * scale).build(), UUID.randomUUID().toString(), 350,
                    "Capability Group", "Capability Group", "icon", Color.builder().color("red").build(), curCapGroup, coords,
                    Icon.builder().code("\uf247").color("red").size(1500).build());
            nodeList.add(capGroup);
            List<ServiceSummaryMetrics> capGroupMetric = summaryResults.stream()
                    .filter(sm -> sm.capabilityGroupName().equals(capGroup.label())).collect(Collectors.toList());
            for (ServiceSummaryMetrics capMetric : capGroupMetric) {
                boolean capabilitySentinel = false;
                Node capability = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(capMetric.capabilityName()))
                        .findFirst()
                        .orElse(Node.builder().font(Font.builder().size(400 * scale).build()).id(UUID.randomUUID().toString())
                                .size(325).group("Capability").title("Capability").shape("icon").color(Color.builder().color("yellow").build())
                                .icon(Icon.builder().code("\uf096").color("yellow").size(1250).build())
                                .label(capMetric.capabilityName()).build());
                if (!nodeList.contains(capability)) {
                    nodeList.add(capability);
                }
                Node component = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(capMetric.componentName())).findFirst()
                        .orElse(Node.builder().font(Font.builder().size(365 * scale).build()).id(UUID.randomUUID().toString())
                                .size(300).shape("icon").color(Color.builder().color("blue").build())
                                .icon(Icon.builder().code("\uf1cb").color("blue").size(1250).build())
                                .label(capMetric.componentName()).group("Component").title("Component").build());
                if (!nodeList.contains(component)) {
                    nodeList.add(component);
                }
                Node interfaceNode = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(capMetric.interfaceName()))
                        .findFirst()
                        .orElse(Node.builder().font(Font.builder().size(300 * scale).build()).id(UUID.randomUUID().toString())
                                .size(275).shape("icon").color(Color.builder().color("green").build())
                                .icon(Icon.builder().code("\uf013").color("green").size(1250).build())
                                .label(capMetric.interfaceName() + "\n[" + capMetric.callCount() + "]").group("Interface").title("Interface").build());
                if (!nodeList.contains(interfaceNode)) {
                    nodeList.add(interfaceNode);
                }
                 for (String consumer : capMetric.consumerIds()) {
                    Node consumerNode = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(consumer)).findFirst()
                            .orElse(Node.builder().font(Font.builder().size(280 * scale).build()).id(UUID.randomUUID().toString())
                                    .size(255).shape("icon").color(Color.builder().color("purple").build())
                                    .icon(Icon.builder().code("\uf2c0").color("purple").size(1250).build()).label(consumer).group("Consumers")
                                    .title("Consumers").build());
                    if (!nodeList.contains(consumerNode)) {
                        nodeList.add(consumerNode);
                    }
                    Edge c2I = Edge.builder().from(consumerNode.id()).to(interfaceNode.id()).build();
                    edgeList.add(c2I);
                }
                Edge cG2Cap = Edge.builder().from(capGroup.id()).to(capability.id()).build();
                edgeList.add(cG2Cap);
                Edge cap2Comp = Edge.builder().from(capability.id()).to(component.id()).build();
                edgeList.add(cap2Comp);
                Edge comp2Int = Edge.builder().from(component.id()).to(interfaceNode.id()).build();
                edgeList.add(comp2Int);
            }
        }
        return NodeDataSet.generate(nodeList, edgeList);
    }
    static {
        fixedCoordinateMap = new HashMap<>();
        fixedCoordinateMap.put("Enterprise", FixedCoordinate.builder()
                .fixed(FixedCoordinateDescriptor.builder().x(true).y(true).build()).x(45000).y(25000).build());
        fixedCoordinateMap.put("Risk, Compliance & Financial Management", FixedCoordinate.builder()
                .fixed(FixedCoordinateDescriptor.builder().x(true).y(true).build()).x(45000).y(-25000).build());
        fixedCoordinateMap.put("Sales, Distribution & Service", FixedCoordinate.builder()
                .fixed(FixedCoordinateDescriptor.builder().x(true).y(true).build()).x(-45000).y(-25000).build());
        fixedCoordinateMap.put("Product Management, Underwriting & Processing", FixedCoordinate.builder()
                .fixed(FixedCoordinateDescriptor.builder().x(true).y(true).build()).x(-45000).y(25000).build());
    }

    private static FixedCoordinate getFixedPosition(String name) {
        return null;
        //return fixedCoordinateMap.get(name);
    }
}
