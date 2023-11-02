package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AKSMonitoringService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AKSMonitoringServiceImpl implements AKSMonitoringService {
  private static final Logger logger = LogManager.getLogger(AKSMonitoringServiceImpl.class);
  private final VaultService vaultService;
  private final ApplicationProperties applicationProperties;
  private static final String TAGKEY="Application";
  private final UserService userService;

  public AKSMonitoringServiceImpl(VaultService vaultService,
                                  ApplicationProperties applicationProperties, UserService userService) {
    this.vaultService = vaultService;
    this.applicationProperties = applicationProperties;
    this.userService = userService;
  }


  @Override
  public List<AlertRuleResponse> createAlertRule(HttpServletRequest request, AlertRuleDto alertRuleDto) throws JSONException {
    try {

      Map<String,String> tags=new HashMap<>();
      if(null != alertRuleDto.getResourceGroup())
        tags.put("resourceGroupName",alertRuleDto.getResourceGroup());
      tags.put("Application","AZX");
      tags.put("Environment","dev");
      tags.put("Project","cloud");

      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
              .authenticate(credentials.getApplicationTokenCredentials())
              .withSubscription(credentials.getSubscriptionId());
      final String MANAGED_CLUSTER="Microsoft.ContainerService/managedClusters";

      logger.info("alert rule creation is starting...");
      MetricAlert aksMemory = azure
              .alertRules()
              .metricAlerts()
              .define("azx_aks_allocatable_memory_bytes"+"_"+alertRuleDto.getTargetResourceName())
              .withExistingResourceGroup(alertRuleDto.getResourceGroup())
              .withTargetResource(alertRuleDto.getResourceId())
              .withPeriod(Period.minutes(5).toPeriod())
              .withFrequency(Period.minutes(1))
              .withAlertDetails(0, "AKS memory not available")
              .withActionGroups(alertRuleDto.getActionGroupId())
              .defineAlertCriteria(MANAGED_CLUSTER)
              .withMetricName("kube_node_status_allocatable_memory_bytes")
              .withCondition(MetricAlertRuleTimeAggregation.AVERAGE,
                      MetricAlertRuleCondition.LESS_THAN_OR_EQUAL, 1024000000)
              .attach()
              .withTags(tags)
              .create();
      logger.debug(aksMemory);

      logger.debug("aks Memory alert " + aksMemory.name() + " rule is created.");

      MetricAlert aksCPU = azure
              .alertRules()
              .metricAlerts()
              .define("azx_aks_allocatable_cpu_cores"+"_"+alertRuleDto.getTargetResourceName())
              .withExistingResourceGroup(alertRuleDto.getResourceGroup())
              .withTargetResource(alertRuleDto.getResourceId())
              .withPeriod(Period.minutes(5).toPeriod())
              .withFrequency(Period.minutes(1))
              .withAlertDetails(0, "AKS CPU Core not available")
              .withActionGroups(alertRuleDto.getActionGroupId())
              .defineAlertCriteria(MANAGED_CLUSTER)
              .withMetricName("kube_node_status_allocatable_cpu_cores")
              .withCondition(MetricAlertRuleTimeAggregation.AVERAGE, MetricAlertRuleCondition.LESS_THAN_OR_EQUAL, 1)
              .attach()
              .withTags(tags)
              .create();

      logger.debug("aks CPU core alert " + aksCPU.name() + " rule is created.");

      MetricAlert podRule = azure
              .alertRules()
              .metricAlerts()
              .define("azx_aks_pod_status_ready"+"_"+alertRuleDto.getTargetResourceName())
              .withExistingResourceGroup(alertRuleDto.getResourceGroup())
              .withTargetResource(alertRuleDto.getResourceId())
              .withPeriod(Period.minutes(5).toPeriod())
              .withFrequency(Period.minutes(1))
              .withAlertDetails(0, "AKS has 0 pods in ready state")
              .withActionGroups(alertRuleDto.getActionGroupId())
              .defineAlertCriteria(MANAGED_CLUSTER)
              .withMetricName("kube_pod_status_ready")
              .withCondition(MetricAlertRuleTimeAggregation.TOTAL, MetricAlertRuleCondition.LESS_THAN, 1)
              .withDimension("namespace", "Include", "*")
              .withDimension("pod", "Include", "*")
              .attach()
              .withTags(tags)
              .create();

      logger.debug("Metric alert " + podRule.name() + " rule is created.");

      ActivityLogAlert aksDelete = azure
              .alertRules()
              .activityLogAlerts()
              .define("azx_aks_delete"+"_"+alertRuleDto.getTargetResourceName())
              .withExistingResourceGroup(alertRuleDto.getResourceGroup())
              .withTargetResource(alertRuleDto.getResourceId())
              .withDescription("AKS cluster deleted")
              .withRuleEnabled()
              .withActionGroups(alertRuleDto.getActionGroupId())
              .withEqualsConditions(
                      Map.of(
                              "category", "Administrative",
                              "operationName", "Microsoft.ContainerService/managedClusters/delete",
                              "status", "Started"))
              .withTags(tags)
              .create();

      logger.debug("Activity log alert " + aksDelete.name() + " rule is created.");

      logger.info("alert rule creation is ended...");


    } catch (CloudException e) {
      logger.error("Error occurred while createAlertRule::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.body().message(),
              null,
              e.body().message(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return getAlertRuleList(request,alertRuleDto.getResourceId());
  }

  @Override
  public ActionGroupResponse createActionGroup(HttpServletRequest request,ActionGroupDto actionGroupDto) throws JSONException {

    ActionGroupResponse actionGroupResponse;

    try {

      Map<String,String> tags=new HashMap<>();
      if(null != actionGroupDto.getResourceGroup())
        tags.put("resourceGroupName",actionGroupDto.getResourceGroup());
      tags.put("name",actionGroupDto.getName());
      tags.put("Application","AZX");
      tags.put("Environment","dev");
      tags.put("Project","cloud");

      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
              .authenticate(credentials.getApplicationTokenCredentials())
              .withSubscription(credentials.getSubscriptionId());
      logger.info("ActionGroup creation is starting...");

      ActionGroup actionGroup;

      if(!actionGroupDto.getCountyCode().isEmpty() && actionGroupDto.getCountyCode()!= null
              && !actionGroupDto.getPhoneNumber().isEmpty() && actionGroupDto.getPhoneNumber()!=null ){

        actionGroup = azure
                .actionGroups()
                .define(actionGroupDto.getName())
                .withExistingResourceGroup(actionGroupDto.getResourceGroup())
                .withShortName("AZX")
                .defineReceiver(actionGroupDto.getReceiver())
                .withEmail(actionGroupDto.getEmailId())
                .withSms(actionGroupDto.getCountyCode(), actionGroupDto.getPhoneNumber())
                .attach()
                .withTag(TAGKEY, "AZX")
                .withTags(tags)
                .create();
        logger.debug("actionGroup :"+actionGroup.name());
      } else {
        actionGroup = azure
                .actionGroups()
                .define(actionGroupDto.getName())
                .withExistingResourceGroup(actionGroupDto.getResourceGroup())
                .withShortName("AZX")
                .defineReceiver(actionGroupDto.getReceiver())
                .withEmail(actionGroupDto.getEmailId())
                .attach()
                .withTag(TAGKEY, "AZX")
                .withTags(tags)
                .create();
        logger.debug("actionGroup :"+actionGroup.name());
      }
      List<String> phoneNumber = new ArrayList<>();
      actionGroup.smsReceivers().forEach(smsReceiver -> {
        phoneNumber.add(smsReceiver.countryCode()+" "+smsReceiver.phoneNumber());
      });

      actionGroupResponse =
              ActionGroupResponse
                      .builder()
                      .id(actionGroup.id())
                      .resourceGroup(actionGroup.resourceGroupName())
                      .name(actionGroup.name())
                      .emailId(actionGroup.emailReceivers().stream()
                              .map(EmailReceiver::emailAddress).collect(Collectors.toList()))
                      .phoneNumber(phoneNumber)
                      .tags(actionGroup.tags())
                      .build();
      logger.debug("actionGroupResponse :"+actionGroupResponse);
      logger.info("ActionGroup creation is Ended...");
    } catch (CloudException e) {
      logger.error("Error occurred while createActionGroup::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.body().message(),
              null,
              e.body().message(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return actionGroupResponse;
  }

  @Override
  public List<ActionGroupResponse> actionGroups(HttpServletRequest request,String resourceGroupName) throws JSONException {

    ArrayList<ActionGroupResponse> actionGroupResponseList = new ArrayList<>();

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
              .authenticate(credentials.getApplicationTokenCredentials())
              .withSubscription(credentials.getSubscriptionId());
      logger.info("listing all actionGroups starting...");
      PagedList<ActionGroup> actionGroups = azure.actionGroups().listByResourceGroup(resourceGroupName);
      logger.debug("actionGroups :"+actionGroups);
      actionGroups.forEach(actionGroup -> {
        List<String> phoneNumber = new ArrayList<>();
        actionGroup.smsReceivers().forEach(smsReceiver -> {
          phoneNumber.add(smsReceiver.countryCode()+" "+smsReceiver.phoneNumber());
        });
        ActionGroupResponse actionGroupResponse =
                ActionGroupResponse
                        .builder()
                        .id(actionGroup.id())
                        .resourceGroup(actionGroup.resourceGroupName())
                        .name(actionGroup.name())
                        .emailId(actionGroup.emailReceivers().stream()
                                .map(EmailReceiver::emailAddress).collect(Collectors.toList()))
                        .phoneNumber(phoneNumber)
                        .tags(actionGroup.tags())
                        .build();
        actionGroupResponseList.add(actionGroupResponse);
      });
      logger.debug("actionGroupResponseList :"+actionGroupResponseList);
      logger.info("listing all actionGroups Ended...");
    } catch (CloudException e) {
      logger.error("Error occurred while actionGroups::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.body().message(),
              null,
              e.body().message(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return actionGroupResponseList;
  }

  @Override
  public List<AlertRuleResponse> getAlertRules(HttpServletRequest request,String aksId) throws JSONException {
    logger.debug("getAlertRules :"+getAlertRuleList(request,aksId));
    return getAlertRuleList(request,aksId);
  }

  private List<AlertRuleResponse> getAlertRuleList(HttpServletRequest request,String aksId) throws JSONException {
    List<AlertRuleResponse> alertRuleResponseList = new ArrayList<>();

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
              .authenticate(credentials.getApplicationTokenCredentials())
              .withSubscription(credentials.getSubscriptionId());
      logger.info("Alert Rule List is starting...");
      PagedList<MetricAlert> alertRules = azure.alertRules().metricAlerts().list();
      logger.debug("alertRules :"+alertRules);
      alertRules.stream()
              .filter(metricAlert -> metricAlert.scopes().contains(aksId)).forEach(metricAlert -> {
                metricAlert.alertCriterias().forEach((s, metricAlertCondition) -> {
                  String actionGroup = azure.actionGroups()
                          .getById(String.join("", metricAlert.actionGroupIds())).name();

                  String threshold = String.valueOf(metricAlertCondition.threshold());
                  logger.debug("threshold ::"+threshold);

                  AlertRuleResponse alertRuleResponse =
                          AlertRuleResponse
                                  .builder()
                                  .alertName(metricAlert.name()).description(metricAlert.description())
                                  .condition("Whenever the "
                                          + metricAlertCondition.timeAggregation().toString() + " "
                                          + metricAlertCondition.metricName() + " is "
                                          + metricAlertCondition.condition().toString() + " to " + metricAlertCondition
                                          .threshold())
                                  .targetResource(azure.kubernetesClusters().getById(aksId).name())
                                  .actionGroupName(actionGroup)
                                  .tags(metricAlert.tags())
                                  .build();

                  double value = metricAlertCondition
                          .threshold();
                  logger.debug("value :"+value);

                  alertRuleResponseList.add(alertRuleResponse);
                });
              });
      logger.debug("Alert Rule ResponseList :"+alertRuleResponseList);

      PagedList<ActivityLogAlert> activityLogAlerts =
              azure
                      .alertRules()
                      .activityLogAlerts()
                      .list();
      logger.debug("Activity Log Alerts :"+activityLogAlerts);

      StringBuffer equalCondition = new StringBuffer();
      activityLogAlerts
              .stream()
              .filter(activityLogAlert -> activityLogAlert.scopes().contains(aksId))
              .forEach(activityLogAlert -> {
                activityLogAlert.equalsConditions()
                        .forEach((key, value) -> {
                          equalCondition.append(key).append(" equals ").append(value).append(". ");
                        });
                String actionGroup = azure.actionGroups()
                        .getById(String.join("", activityLogAlert.actionGroupIds())).name();

                AlertRuleResponse alertRuleResponse =
                        AlertRuleResponse
                                .builder()
                                .alertName(activityLogAlert.name())
                                .description(activityLogAlert.description())
                                .condition(equalCondition.toString())
                                .targetResource(azure.kubernetesClusters().getById(aksId).name())
                                .actionGroupName(actionGroup)
                                .tags(activityLogAlert.tags())
                                .build();
                logger.info("Alert Rule Response :"+alertRuleResponse);
                alertRuleResponseList.add(alertRuleResponse);
              });
      logger.info("alertRuleResponseList :"+alertRuleResponseList);
      logger.info("Alert Rule List is ended...");
    } catch (CloudException e) {
      logger.error("Error occured while getAlertRules:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.body().message(),
              null,
              e.body().message(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return alertRuleResponseList;
  }

  private AzureCredentials applicationTokenCredentials(String userName) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(userName);

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
    System.out.println(secret.getClientId());

    return
            new AzureCredentials(
                    new ApplicationTokenCredentials(
                            secret.getClientId(), secret.getTenantId(), secret.getClientSecret(),
                            AzureEnvironment.AZURE),
                    secret.getSubscriptionId());
  }
}
