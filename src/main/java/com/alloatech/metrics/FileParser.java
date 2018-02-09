/**
 * Proprietary and Confidential AlloaTech, LLC. This document contains material which is proprietary and confidential property of ThorCode. The right
 * to view, reproduce, modify, distribute, or in any way display this work is prohibited without the expressed written consent of ThorCode Copyright
 * &copy; 2017 Initial commit: Jul 7, 20178:35:05 AM User: thor
 */
package com.alloatech.metrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author thor
 */
public class FileParser {

	private static Map<String, String> opMap = new HashMap<>();
	private static Map<String, String> interfaceMap = new HashMap<>();
	private static Map<String, String> componentMap = new HashMap<>();
	private static Map<String, String> capabilityMap = new HashMap<>();
	private static Map<String, String> capabilityGroupMap = new HashMap<>();
	private static Set<String> consumerList = new HashSet<>();
	static {
		// OP ---- INTERFACE
		opMap.put("GET-TERRITORY", "getTerritory");
		opMap.put("GET-GEO-ASSESSMENT", "getGeoAssessment");
		opMap.put("GET-GEOCODE-TERRITORY", "getGeocodeTerritory");
		opMap.put("GET-GEOCODE", "getGeocode");
		opMap.put("SAVE-GEO-ASSESSMENT", "saveGeoAssessment");
		opMap.put("GET-TAXATION-AREA", "getTaxationArea");
		opMap.put("VALIDATE-TERRITORY", "validateTerritory");
		opMap.put("VALIDATE-TAXATION-AREA", "validateTaxationArea");
		opMap.put("GET-FIPS-COUNTY", "getFIPSCounty");
		opMap.put("genericPolicyEventNotification", "genericPolicyEventNotification");
		opMap.put("PolicyStatusChange", "policyStatusChange");
		// OP ---- INTERFACE
		interfaceMap.put("getGeocode", "GeoSpatial");
		interfaceMap.put("getTerritory", "GeoSpatial");
		interfaceMap.put("getFIPSCounty", "GeoSpatial");
		interfaceMap.put("getTaxationArea", "GeoSpatial");
		interfaceMap.put("validateTaxationArea", "GeoSpatial");
		interfaceMap.put("validateTerritory", "GeoSpatial");
		interfaceMap.put("getGeoAssessment", "GeoSpatial");
		interfaceMap.put("getGeocodeTerritory", "GeoSpatial");
		interfaceMap.put("saveGeoAssessment", "GeoSpatial");
		interfaceMap.put("genericPolicyEventNotification", "PolicyManagement");
		interfaceMap.put("policyStatusChange", "PolicyManagement");
		interfaceMap.put("newDocument", "CommunicationObjectManager");
		interfaceMap.put("newDocument", "CommunicationObjectManager");
		// INTERFACE ----- COMPONENT
		componentMap.put("GeoSpatial", "Location");
		componentMap.put("ActivityConditionPlaceManager", "ActivityConditionPlace");
		componentMap.put("ActivityConditionPlaceObjectManager", "ActivityConditionPlace");
		componentMap.put("CommunicationObjectManager", "Communication");
		componentMap.put("PolicyCommunication", "Communication");
		componentMap.put("CommunicationManager", "Communication");
		componentMap.put("CommunicationObjectManager", "Communication");
		componentMap.put("ClaimCommunication", "ClaimManagement");
		componentMap.put("LossEvaluation", "ClaimManagement");
		componentMap.put("LossNotification", "ClaimManagement");
		componentMap.put("PhysicalObjectManager", "Underwriting");
		componentMap.put("PolicyAdministration", "Risk Rating");
		componentMap.put("PolicyManagement", "PolicyManagement");
		componentMap.put("ClaimInquiry", "ClaimManagement");
		componentMap.put("ClaimSalvage", "ClaimManagement");
		componentMap.put("ClaimHandling", "ClaimManagement");
		componentMap.put("SeverityRating", "ClaimManagement");
		componentMap.put("ProviderAgreementAdministration", "ClaimManagement");
		componentMap.put("ProviderInformationManagement", "ClaimManagement");
		componentMap.put("PolicyInquiry", "PolicyManagement");
		componentMap.put("InsuranceAgreementObjectManager", "PolicyManagement");
		componentMap.put("RatingService", "Risk Rating");
		componentMap.put("RatabaseService", "Risk Rating");
		componentMap.put("InsuranceAgreementManager", "Risk Rating");
		componentMap.put("PaymentProcessing", "PaymentManagement");
		componentMap.put("PartyNotification", "PaymentManagement");
		componentMap.put("PartyManager", "Risk Rating");
		componentMap.put("Agency", "Agency");
		componentMap.put("Transformation", "Mediation");
		componentMap.put("AccountAgreementInquiry", "AccountAgreementManagement");
		// COMPONENT CAPABILITY
		capabilityMap.put("FinancialTransaction", "Billing & Payments");
		capabilityMap.put("BillingAndCollectionManagement", "Billing & Payments");
		capabilityMap.put("PaymentManagement", "Billing & Payments");
		capabilityMap.put("ClaimManagement", "Claim Management");
		capabilityMap.put("Claim", "Claim Management");
		capabilityMap.put("Mediation", "Transformation Services");
		capabilityMap.put("ClaimCenter", "Claim Management");
		capabilityMap.put("WCReferral", "Claim Management");
		capabilityMap.put("AccountAgreementManagement", "Customer Management");
		capabilityMap.put("Communication", "Electronic Content Management");
		capabilityMap.put("Majesco", "Distribution Management");
		capabilityMap.put("Agency", "Distribution Management");
		capabilityMap.put("FinancialServicesAgreement", "Financial Management");
		capabilityMap.put("Location", "Location Services");
		capabilityMap.put("PolicyManagement", "Policy Processing & Servicing");
		capabilityMap.put("Party", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("ActivityConditionPlace", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("Risk Rating", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("UnderwritingManagement", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("Underwriting", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("PhysicalObject", "Risk Analysis, Underwriting & Pricing");
		capabilityMap.put("Rating", "Risk Analysis, Underwriting & Pricing");
		// CAPABILITY CAPABILITY GROUP
		capabilityGroupMap.put("Distribution Management", "Sales, Distribution & Service");
		capabilityGroupMap.put("Sales", "Sales, Distribution & Service");
		capabilityGroupMap.put("Customer Management", "Sales, Distribution & Service");
		capabilityGroupMap.put("Marketing", "Sales, Distribution & Service");
		capabilityGroupMap.put("Claim Management", "Product Management, Underwriting & Processing");
		capabilityGroupMap.put("Product Development & Management", "Product Management, Underwriting & Processing");
		capabilityGroupMap.put("Risk Analysis, Underwriting & Pricing",
				"Product Management, Underwriting & Processing");
		capabilityGroupMap.put("Policy Processing & Servicing", "Product Management, Underwriting & Processing");
		capabilityGroupMap.put("Electronic Content Management", "Enterprise");
		capabilityGroupMap.put("Location Services", "Enterprise");
		capabilityGroupMap.put("Transformation Services", "Enterprise");
		capabilityGroupMap.put("Billing & Payments", "Risk, Compliance & Financial Management");
		capabilityGroupMap.put("Financial Management", "Risk, Compliance & Financial Management");
	}

	public List<ServiceMetrics> parseFile(String file) {
		long start = System.nanoTime();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		List<ServiceMetrics> result = new ArrayList<>();
		try {
			int count = 0;
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				count++;
				if(count == 1000) {
					//break;
				}
				// use comma as separator
				String[] metric = line.split(cvsSplitBy);
				if (!"Count".equals(metric[0])) {
					String curCapabilityGroup = null;
					String curCapability = null;
					String curComponent = null;
					String curInterface = null;
					String curOp = null;
					String serviceName = metric[5].trim();
					if (metric[4].trim().startsWith("Comm")) {
						serviceName = metric[4].trim();
					} else if (metric[6].trim().matches("^[A-Za-z].*$")) {
						serviceName = metric[6].trim();
					}
					String[] splitServiceName = serviceName.split("\\.");
					if (splitServiceName.length == 3) {
						curComponent = splitServiceName[0];
						curCapability = deriveCapability(curComponent);
						curCapabilityGroup = deriveCapabilityGroup(curCapability);
						curInterface = splitServiceName[1];
						curOp = splitServiceName[2];
					} else if (splitServiceName.length == 2) {
						curInterface = splitServiceName[0];
						curComponent = deriveComponent(curInterface);
						curCapability = deriveCapability(curComponent);
						curCapabilityGroup = deriveCapabilityGroup(curCapability);
						curOp = splitServiceName[1];
					} else if (splitServiceName.length == 1) {
						curOp = deriveOp(splitServiceName[0]);
						curInterface = deriveInterface(curOp);
						curComponent = deriveComponent(curInterface);
						curCapability = deriveCapability(curComponent);
						curCapabilityGroup = deriveCapabilityGroup(curCapability);
					}
					String consumerId = metric[3].trim();
					if (consumerId.startsWith("WFLD.MF.$")) {
						consumerId = "WFLD.MF.ONLINE.JOB";
					}
					if (!consumerId.toLowerCase().contains("test") && !consumerId.toLowerCase().contains("soap ui")) {
						ServiceMetrics curMetric = ServiceMetrics.builder()
								.callCount(Integer.parseInt(metric[0].trim()))
								.dateTimestamp(
										LocalDate.parse(metric[1].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
								.consumerId(consumerId).serviceName(serviceName).capabilityGroupName(curCapabilityGroup)
								.capabilityName(curCapability).componentName(curComponent).interfaceName(curInterface)
								.operationName(curOp).serviceVersion(metric[6].trim()).build();
						if (curMetric.consumerId() != null && curMetric.consumerId().startsWith("WFLD.MF")) {
							consumerList.add(curMetric.consumerId());
						}
						if (curMetric.capabilityName() == null || curMetric.capabilityGroupName() == null
								|| curMetric.componentName() == null || curMetric.interfaceName() == null) {
							System.out.println("Cap::" + curMetric.capabilityName());
							System.out.println("CapGroup::" + curMetric.capabilityGroupName());
							System.out.println("Comp::" + curMetric.componentName());
							System.out.println("Int::" + curMetric.interfaceName());
							System.out.println("FIX THIS::\n" + line + "\n" + curMetric.toJson());
							System.exit(1);
						}
						/*
						 * if(result.size() > 100000) { break; }
						 */
						result.add(curMetric);
						// System.out.println(curMetric.toJson());
						if (result.size() % 750000 == 0) {
							System.out.println(curMetric.toJson());
							System.out.println("Memory: "
									+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
							System.out.println("Time: " + ((System.nanoTime() - start) / 1000000000));
							System.out.println("Count: " + result.size());
						}
					}
				}
			}
			System.out.println("Done reading in file:" + file);
			//consumerList.forEach(c -> System.out.println(c));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private String deriveInterface(String curOp) {
		return interfaceMap.get(curOp);
	}

	private String deriveOp(String curOp) {
		return opMap.get(curOp);
	}

	private String deriveComponent(String curInterface) {
		return componentMap.get(curInterface);
	}

	private String deriveCapabilityGroup(String curCapability) {
		return capabilityGroupMap.get(curCapability);
	}

	private String deriveCapability(String curComponent) {
		return capabilityMap.get(curComponent);
	}
}
