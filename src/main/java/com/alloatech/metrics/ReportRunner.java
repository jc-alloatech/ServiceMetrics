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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author thor
 */
public class ReportRunner {

	private static Map<String, FixedCoordinate> fixedCoordinateMap;

	public static void main(String[] args) {
		long start = System.nanoTime();
		System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		String files[] = { // "c:/tmp/data/consumer_report_daily.201710.csv",
				"c:/tmp/data/consumer_report_daily.201801.csv", "c:/tmp/data/consumer_report_daily.201711.csv",
				"c:/tmp/data/consumer_report_daily.201712.csv" };
		FileParser parser = new FileParser();
		int callTracker = 0;
		List<ServiceMetrics> results = new ArrayList<>();
		Map<String, Integer> monthlyCallCount = new HashMap<>();
		for (String filePath : files) {
			List<ServiceMetrics> parseResults = parser.parseFile(filePath);
			String month = parseResults.get(0).dateTimestamp().getMonth().toString();
			int count = parseResults.stream().mapToInt(ServiceMetrics::callCount).sum();
			callTracker += count;
			monthlyCallCount.put(month, count);
			results.addAll(parseResults);

			System.out.println("Done with " + filePath + " - Record Count:" + results.size());
		}

		List<ServiceSummaryMetrics> serviceSummaryMetrics = new ArrayList<>();

		// COLLECT ALL THE SAME INTERFACES TO CONSUMERS WHILE ADDING UP THE
		// COUNTS

		Map<String, Set<String>> c2IMap = new HashMap<>();
		Map<String, Set<String>> i2CMap = new HashMap<>();
		Map<String, Set<String>> o2IMap = new HashMap<>();

		ConcurrentMap<String, List<ServiceMetrics>> opGroupedMetrics = results.parallelStream()
				.collect(Collectors.groupingByConcurrent(t -> t.operationName().trim().toLowerCase()));
		for (String opName : opGroupedMetrics.keySet()) {
			List<ServiceMetrics> metricsList = opGroupedMetrics.get(opName);
			// ADD C2I Mapping

			metricsList.parallelStream().forEach(m -> {
				String capComponentName = (m.capabilityName() + "|" + m.componentName()).trim().toLowerCase();
				Set intSet = (i2CMap.get(capComponentName) != null ? i2CMap.get(capComponentName) : new HashSet<>());
				intSet.add(m.interfaceName());
				i2CMap.put(capComponentName, intSet);
			});

			metricsList.parallelStream().forEach(m -> {
				String consumerName = m.consumerId().trim().toLowerCase();
				Set intSet = (c2IMap.get(consumerName) != null ? c2IMap.get(consumerName) : new HashSet<>());
				String intName = m.interfaceName();
				if (intName != null) {
					if (intSet == null) {
						intSet = new HashSet<>();
					}
					intSet.add(intName);
				}
				c2IMap.put(consumerName, intSet);
			});

			metricsList.parallelStream().forEach(m -> {
				String opNameCheck = m.operationName().trim().toLowerCase();
				Set opSet = (o2IMap.get(opNameCheck) != null ? o2IMap.get(opNameCheck) : new HashSet<>());
				String intName = m.interfaceName();
				if (intName != null) {
					if (opSet == null) {
						opSet = new HashSet<>();
					}
					opSet.add(intName);
				}
				o2IMap.put(opNameCheck, opSet);
			});

			Integer summedCalls = metricsList.parallelStream()
					.collect(Collectors.summingInt(ServiceMetrics::callCount));
			LocalDate startDate = metricsList.stream().map(m -> m.dateTimestamp()).min(LocalDate::compareTo).get();
			LocalDate endDate = metricsList.stream().map(m -> m.dateTimestamp()).max(LocalDate::compareTo).get();
			List<String> consumerIds = metricsList.stream().map(m -> m.consumerId().toLowerCase().trim()).distinct()
					.collect(Collectors.toList());
			List<String> serviceVersions = metricsList.stream().map(m -> m.serviceVersion()).distinct()
					.collect(Collectors.toList());
			ServiceMetrics existingMetric = metricsList.get(0);
			ServiceSummaryMetrics newSummaryMetric = ServiceSummaryMetrics.builder().callCount(summedCalls)
					.dateTimestampStart(startDate).dateTimestampEnd(endDate).consumerIds(consumerIds)
					.serviceVersions(serviceVersions).capabilityGroupName(existingMetric.capabilityGroupName())
					.capabilityName(existingMetric.capabilityName()).componentName(existingMetric.componentName())
					.serviceName(existingMetric.serviceName()).interfaceName(existingMetric.interfaceName())
					.consumerCount(consumerIds.size()).operationName(existingMetric.operationName()).build();
			serviceSummaryMetrics.add(newSummaryMetric);
		}
		serviceSummaryMetrics = serviceSummaryMetrics.stream().sorted(new Comparator<ServiceSummaryMetrics>() {

			@Override
			public int compare(ServiceSummaryMetrics o1, ServiceSummaryMetrics o2) {
				if (o1.consumerIds().size() > o2.consumerIds().size()) {
					return -1;
				} else if (o1.consumerIds().size() == o2.consumerIds().size()) {
					return 0;
				}
				return 1;
			}
		}).collect(Collectors.toList());

		// CAPABILITY METRICS
		ConcurrentMap<String, List<ServiceSummaryMetrics>> capGroupedMetrics = serviceSummaryMetrics.parallelStream()
				.collect(Collectors.groupingByConcurrent(t -> t.capabilityName().trim().toLowerCase()));

		List<CapabilityMetrics> capMetrics = new ArrayList<>();
		List<ConsumerGroupedCapabilityMetrics> cGCapMetrics = new ArrayList<>();

		for (String capName : capGroupedMetrics.keySet()) {
			List<ServiceSummaryMetrics> capMetricsList = capGroupedMetrics.get(capName);

			int capCallCount = 0;
			ServiceSummaryMetrics curMetric = capMetricsList.get(0);
			Set<String> capInterfaceNames = new HashSet<>();
			int capInterfaceCount = 0;

			Set<String> capOperationNames = new HashSet<>();
			int capOperationCount = 0;

			Set<String> capComponentNames = new HashSet<>();
			int capComponentCount = 0;

			Set<String> capConsumerNames = new HashSet<>();
			int capConsumerCount = 0;

			capCallCount = capMetricsList.stream().mapToInt(m -> m.callCount()).sum();

			capComponentNames.addAll(capMetricsList.stream().map(m -> m.componentName().toLowerCase()).distinct()
					.collect(Collectors.toList()));

			capInterfaceNames.addAll(capMetricsList.stream().map(m -> m.interfaceName().toLowerCase()).distinct()
					.collect(Collectors.toList()));

			capOperationNames.addAll(capMetricsList.stream().map(m -> m.operationName().toLowerCase()).distinct()
					.collect(Collectors.toList()));

			capConsumerNames.addAll(capMetricsList.stream().map(m -> m.consumerIds()).flatMap(List::stream)
					.map(s -> s.toLowerCase().trim()).distinct().collect(Collectors.toList()));

			capInterfaceCount = capInterfaceNames.size();
			capConsumerCount = capConsumerNames.size();
			capOperationCount = capOperationNames.size();
			capComponentCount = capComponentNames.size();

			capMetrics.add(CapabilityMetrics.builder().callCount(capCallCount).interfaces(capInterfaceNames)
					.interfaceCount(capInterfaceCount).capabilityGroupName(curMetric.capabilityGroupName())
					.capabilityName(capName).consumerCount(capConsumerCount).consumers(capConsumerNames)
					.components(capComponentNames).componentCount(capComponentCount).operations(capOperationNames)
					.operationCount(capOperationCount).build());
		}
		// FOR EACH CONSUMER GROUP - SPIN OVER EACH ServiceMetric
		ConcurrentMap<String, ConcurrentMap<Integer, List<ServiceSummaryMetrics>>> consumerCountGroupedCapabilityMetrics = serviceSummaryMetrics
				.parallelStream().collect(Collectors.groupingByConcurrent(c -> c.capabilityName().toString().trim(),
						Collectors.groupingByConcurrent(t -> t.consumerCount())));

		for (String capName : consumerCountGroupedCapabilityMetrics.keySet()) {
			ConcurrentMap<Integer, List<ServiceSummaryMetrics>> groupedConsumerMap = consumerCountGroupedCapabilityMetrics
					.get(capName);
			for (Integer consumerCountKey : groupedConsumerMap.keySet()) {
				int callCount = 0;
				ServiceSummaryMetrics curMetric = null;
				Set<String> interfaceNames = new HashSet<>();
				int interfaceCount = 0;

				Set<String> operationNames = new HashSet<>();
				int operationCount = 0;

				Set<String> componentNames = new HashSet<>();
				int componentCount = 0;

				Set<String> consumerNames = new HashSet<>();
				int consumerCount = 0;

				List<ServiceSummaryMetrics> curMetricList = consumerCountGroupedCapabilityMetrics.get(capName)
						.get(consumerCountKey);
				callCount = curMetricList.stream().mapToInt(m -> m.callCount()).sum();
				curMetric = curMetricList.get(0);

				componentNames.addAll(curMetricList.stream().map(m -> m.componentName().toLowerCase()).distinct()
						.collect(Collectors.toList()));

				interfaceNames.addAll(curMetricList.stream().map(m -> m.interfaceName().toLowerCase()).distinct()
						.collect(Collectors.toList()));

				operationNames.addAll(curMetricList.stream().map(m -> m.operationName().toLowerCase()).distinct()
						.collect(Collectors.toList()));

				consumerNames.addAll(curMetricList.stream().map(m -> m.consumerIds()).flatMap(List::stream)
						.map(s -> s.toLowerCase().trim()).distinct().collect(Collectors.toList()));

				interfaceCount = interfaceNames.size();
				consumerCount = consumerNames.size();
				operationCount = operationNames.size();
				componentCount = componentNames.size();

				cGCapMetrics.add(ConsumerGroupedCapabilityMetrics.builder().callCount(callCount).interfaces(interfaceNames)
						.interfaceCount(interfaceCount).capabilityGroupName(curMetric.capabilityGroupName())
						.capabilityName(curMetric.capabilityName()).groupedConsumerCount(consumerCountKey)
						.consumers(consumerNames).components(componentNames).componentCount(componentCount)
						.operations(operationNames).operationCount(operationCount).build());
			}
		}

		LocalDate firstDate = serviceSummaryMetrics.stream()
				.min(Comparator.comparing(ServiceSummaryMetrics::dateTimestampStart)).get().dateTimestampStart();
		LocalDate lastDate = serviceSummaryMetrics.stream()
				.max(Comparator.comparing(ServiceSummaryMetrics::dateTimestampEnd)).get().dateTimestampEnd();
		int uniqueInterfaceNames = (int) serviceSummaryMetrics.stream().map(s -> s.interfaceName().toLowerCase().trim())
				.distinct().count();

		System.out.println("Interfaces FROM serviceSummary Metrics::\n" + serviceSummaryMetrics.stream()
				.map(r -> r.interfaceName()).distinct().sorted().collect(Collectors.toList()));
		System.out.println("Interfaces FROM i2CMap::\n"
				+ i2CMap.values().stream().flatMap(Set::stream).distinct().sorted().collect(Collectors.toList()));

		System.out.println("OPNAMES FROM serviceSummary Metrics::\n" + serviceSummaryMetrics.stream()
				.map(r -> r.operationName()).distinct().sorted().collect(Collectors.toList()));
		System.out.println("OPNAMES FROM o2IMap::\n" + o2IMap.keySet().stream().sorted().collect(Collectors.toList()));

		System.out.println("Consumers FROM serviceSummary Metrics::\n"
				+ serviceSummaryMetrics.stream().map(r -> r.consumerIds()).flatMap(List::stream)
						.map(c -> c.toLowerCase().trim()).distinct().collect(Collectors.toList()));
		System.out.println("Consumers FROM c2IMap::\n"
				+ c2IMap.keySet().stream().map(c -> c.toLowerCase().trim()).collect(Collectors.toList()));

		int uniqueServiceNames = serviceSummaryMetrics.stream().map(r -> r.operationName()).distinct()
				.collect(Collectors.toList()).size();
		long uniqueConsumerCount = serviceSummaryMetrics.stream().map(r -> r.consumerIds()).flatMap(List::stream)
				.map(s -> s.toLowerCase().trim()).distinct().collect(Collectors.toList()).size();
		SummaryMetrics summaryMetrics = SummaryMetrics.builder().uniqueServiceCount(uniqueServiceNames)
				.uniqueInterfaceCount(uniqueInterfaceNames).uniqueConsumerCount((int) uniqueConsumerCount)
				.serviceSummaryMetrics(serviceSummaryMetrics).monthlyCallCount(monthlyCallCount).lastDate(lastDate)
				.interfaceToCapabilityMapping(i2CMap).capabilityLevelMetrics(capMetrics)
				.consumerGroupedCapabilityLevelMetrics(cGCapMetrics).consumerToInterfaceMapping(c2IMap)
				.firstDate(firstDate).build();

		try {
			Files.write(Paths.get("C:/workspace/ServiceMetrics/src/main/resources/results.json"),
					summaryMetrics.toJson().getBytes(), StandardOpenOption.CREATE);
			// Files.write(Paths.get("C:/Users/thor/workspace/service-metrics-web/src/assets/graph_data.json"),
			// generateDataSet(summaryResults).toJson().getBytes(),
			// StandardOpenOption.CREATE);
		} catch (IOException e) {
			System.err.println(e.toString());
		}

		// SUMMARY METRICS
		int cMConsumerCount = (int) capMetrics.stream().map(m -> m.consumers()).flatMap(Set::stream).distinct().count();
		int cMCallCount = capMetrics.stream().mapToInt(m -> m.callCount()).sum();
		int cMIntCount = (int) capMetrics.stream().map(m -> m.interfaces()).flatMap(Set::stream).distinct().count();
		int cMOpCount = (int) capMetrics.stream().mapToInt(m -> m.operationCount()).sum();

		int conMConsumerCount = (int) cGCapMetrics.stream().map(m -> m.consumers()).flatMap(Set::stream).distinct()
				.count();
		int conMCallCount = cGCapMetrics.stream().mapToInt(m -> m.callCount()).sum();
		int conMIntCount = (int) cGCapMetrics.stream().map(m -> m.interfaces()).flatMap(Set::stream).distinct().count();
		int conMOpCount = (int) cGCapMetrics.stream().mapToInt(m -> m.operationCount()).sum();

		System.out.println("TOTAL Calls Count Check: " + callTracker + "=="
				+ summaryMetrics.monthlyCallCount().values().stream().mapToInt(i -> i).sum() + "==" + cMCallCount + "=="
				+ conMCallCount);
		System.out.println(
				"TOTAL Interface Count Check: " + i2CMap.values().stream().flatMap(Set::stream).distinct().count()
						+ "==" + summaryMetrics.uniqueInterfaceCount() + "==" + cMIntCount + "==" + conMIntCount);
		System.out.println("TOTAL Consumer Count Check: " + c2IMap.keySet().size() + "=="
				+ summaryMetrics.uniqueConsumerCount() + "==" + cMConsumerCount + "==" + conMConsumerCount);
		System.out.println("TOTAL OP Count Check: " + o2IMap.keySet().size() + "=="
				+ summaryMetrics.uniqueServiceCount() + "==" + cMOpCount + "==" + conMOpCount);
		System.out.println("TOTAL TIME:" + ((System.nanoTime() - start) / 1000000000));
		System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
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
					"Capability Group", "Capability Group", "icon", Color.builder().color("red").build(), curCapGroup,
					coords, Icon.builder().code("\uf247").color("red").size(1500).build());
			nodeList.add(capGroup);
			List<ServiceSummaryMetrics> capGroupMetric = summaryResults.stream()
					.filter(sm -> sm.capabilityGroupName().equals(capGroup.label())).collect(Collectors.toList());
			for (ServiceSummaryMetrics capMetric : capGroupMetric) {
				boolean capabilitySentinel = false;
				Node capability = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(capMetric.capabilityName()))
						.findFirst()
						.orElse(Node.builder().font(Font.builder().size(400 * scale).build())
								.id(UUID.randomUUID().toString()).size(325).group("Capability").title("Capability")
								.shape("icon").color(Color.builder().color("yellow").build())
								.icon(Icon.builder().code("\uf096").color("yellow").size(1250).build())
								.label(capMetric.capabilityName()).build());
				if (!nodeList.contains(capability)) {
					nodeList.add(capability);
				}
				Node component = nodeList.stream().filter(n -> n.label().equalsIgnoreCase(capMetric.componentName()))
						.findFirst()
						.orElse(Node.builder().font(Font.builder().size(365 * scale).build())
								.id(UUID.randomUUID().toString()).size(300).shape("icon")
								.color(Color.builder().color("blue").build())
								.icon(Icon.builder().code("\uf1cb").color("blue").size(1250).build())
								.label(capMetric.componentName()).group("Component").title("Component").build());
				if (!nodeList.contains(component)) {
					nodeList.add(component);
				}
				Node interfaceNode = nodeList.stream()
						.filter(n -> n.label().equalsIgnoreCase(capMetric.interfaceName())).findFirst()
						.orElse(Node.builder().font(Font.builder().size(300 * scale).build())
								.id(UUID.randomUUID().toString()).size(275).shape("icon")
								.color(Color.builder().color("green").build())
								.icon(Icon.builder().code("\uf013").color("green").size(1250).build())
								.label(capMetric.interfaceName() + "\n[" + capMetric.callCount() + "]")
								.group("Interface").title("Interface").build());
				if (!nodeList.contains(interfaceNode)) {
					nodeList.add(interfaceNode);
				}
				for (String consumer : capMetric.consumerIds()) {
					Node consumerNode = nodeList
							.stream().filter(
									n -> n.label().equalsIgnoreCase(consumer))
							.findFirst()
							.orElse(Node.builder().font(Font.builder().size(280 * scale).build())
									.id(UUID.randomUUID().toString()).size(255).shape("icon")
									.color(Color.builder().color("purple").build())
									.icon(Icon.builder().code("\uf2c0").color("purple").size(1250).build())
									.label(consumer).group("Consumers").title("Consumers").build());
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
		// return fixedCoordinateMap.get(name);
	}
}
