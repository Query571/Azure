package com.azureAccelerator.service.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.resources.fluent.models.PolicyAssignmentInner;
import com.azure.resourcemanager.resources.fluent.models.PolicyDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.implementation.PolicyClientBuilder;
import com.azure.resourcemanager.resources.implementation.PolicyClientImpl;
import com.azure.resourcemanager.resources.models.*;
import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.*;
import com.azureAccelerator.util.GetToken;
import com.google.gson.Gson;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AKSPolicyServiceImpl implements AKSPolicyService {


    private static final Logger logger = LogManager.getLogger(AKSPolicyServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final AzureVNetService vNetService;
    private final AKSDeployService aksDeployService;
    private final AzureKeyVaultService azureKeyVaultService;
    private final AzureStrorageService azureStrorageService;
    private final AzureSqlDBService azureSqlDBService;
    private final AKSMonitoringService aksMonitoringService;
    private final UserService userService;


    @Autowired
    ResourceLoader resourceLoader;

    public AKSPolicyServiceImpl(VaultService vaultService,
                                ApplicationProperties applicationProperties,
                                AzureVNetService vNetService,
                                AKSDeployService aksDeployService,
                                AzureKeyVaultService azureKeyVaultService,
                                AzureStrorageService azureStrorageService,
                                AzureSqlDBService azureSqlDBService, AKSMonitoringService aksMonitoringService, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.vNetService = vNetService;
        this.aksDeployService = aksDeployService;
        this.azureKeyVaultService = azureKeyVaultService;
        this.azureStrorageService = azureStrorageService;
        this.azureSqlDBService = azureSqlDBService;
        this.aksMonitoringService = aksMonitoringService;
        this.userService = userService;
    }


    @Override
    public String createPolicy(HttpServletRequest request) throws JSONException {
        try {
            logger.info("Policies are creating...");
            String str = "Policies are created successfully";
            polallowedlocations(request);
            polauditdenynsg1024addressprefix(request);
            polauditdenynsg1024addressprefixes(request);
            poldenynsgsourcesany(request);
            poldenypipassignment(request);
            poldenyresourcesnotallowed(request);
            polinherittagapplication(request);
            polinherittagengagement(request);
            polinherittagenvironment(request);
            polinherittaglob(request);
            polinherittagowner(request);
            pollimitregion(request);
            polpipspecificsubnet(request);
            polrequirergtagapplication(request);
            polrequirergtagengagement(request);
            polrequirergtagenvironment(request);
            polrequirergtaglob(request);
            polrequirergtagowner(request);
            logger.info(str);
            return str;
        } catch (CloudException e) {
            logger.error("Error occurred while createPolicy:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<AKSPolicyResponseDto> getPolicy(HttpServletRequest request) throws JSONException {
        try {

            List<AKSPolicyResponseDto> aksPolicyResponseDtoList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("getting policy...");

            azure.policyDefinitions()
                    .list()
                    .stream()
                    .filter(policyDefinition -> policyDefinition.policyType()
                            .equals(com.microsoft.azure.management.resources.PolicyType.CUSTOM))
                    .forEach(policyDefinition -> {
                        AKSPolicyResponseDto aksPolicyResponseDto = AKSPolicyResponseDto.builder()
                                .id(policyDefinition.id())
                                .name(policyDefinition.displayName())
                                .definitionLocation("Microsoft Azure Sponsorship")
                                .definitionType("Policy")
                                .description(policyDefinition.description())
                                .type(com.microsoft.azure.management.resources.PolicyType.CUSTOM.toString())
                                .build();
                        aksPolicyResponseDtoList.add(aksPolicyResponseDto);

                    });
            logger.debug(aksPolicyResponseDtoList);
            logger.info("getting policy ended...");
            return aksPolicyResponseDtoList;
        } catch (CloudException e) {
            logger.error("Error occurred while getPolicy:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    public Map<String,String> deleteInitiativePolicies(HttpServletRequest request,String initiativeID) throws JSONException {
      PolicyAssignmentInner pi= policyClientImpl(request.getHeader("userName")).getPolicyAssignments().deleteById(initiativeID);
        logger.info("deleting Initiative Policies ...");
        logger.debug("description----->"+pi.description());
        logger.debug("name----->"+pi.name());
        logger.debug("displayName----->"+pi.displayName());
        logger.info("deleted Initiative Policies ...");
        return  null;
    }

    @Override
    public ArrayList<InitiativesResponseDto> getListOfAssignPolicies(HttpServletRequest request) throws JSONException {
        ArrayList<InitiativesResponseDto> res=new ArrayList<>();
        logger.info("getting List of Assign Policies ...");

        policyClientImpl(request.getHeader("userName")).getPolicyAssignments().list().iterableByPage().forEach(resp -> {
            resp.getElements().stream().forEach(value ->
            {

                String policyType;
                if(value.policyDefinitionId().contains("policySetDefinitions")){
                    policyType="Initiatives";
                }else{
                    policyType="Policy";
                }
                String policyScope=null;
                int ind=value.scope().lastIndexOf("resourceGroups");
                if(ind>=0){
                     int newInd=value.scope().lastIndexOf("/");
                    policyScope=value.scope().substring(newInd+1);
                }

                InitiativesResponseDto obj = InitiativesResponseDto
                        .builder()
                        .id(value.id())
                        .policyDefinitionId(value.policyDefinitionId())
                        .displayName(value.displayName())
                        .policyDescription(value.description())
                        .name(value.name())
                        .policyType(policyType)
                        .policyScope(policyScope)
                        .build();
                res.add(obj);

            });

        });
        logger.debug(res);
        logger.info("getting List of Assign Policies ended ...");


        return res;

    }

    @Override
    public ArrayList<InitiativesResponseDto> getInitiativeVersions(HttpServletRequest request,String groupType) throws JSONException {
        ArrayList <InitiativesResponseDto> res=new ArrayList();
        HashMap<String,ArrayList <String>> mp=new HashMap<>();
        logger.info("get Initiative Versions ...");
        policyClientImpl(request.getHeader("userName")).getPolicyAssignments().list().forEach(assignRes -> {
            String policyScope=null;
            int ind=assignRes.scope().lastIndexOf("resourceGroups");
            if(ind>=0){
                int newInd=assignRes.scope().lastIndexOf("/");
                policyScope=assignRes.scope().substring(newInd+1);
                logger.debug("policyScope :"+policyScope);
            }
            ArrayList <String> rsList=new ArrayList<>();
            if(mp.containsKey(assignRes.policyDefinitionId())){
                 rsList=mp.get(assignRes.policyDefinitionId());
                rsList.add(policyScope);
                mp.put(assignRes.policyDefinitionId(),rsList);
                logger.debug(mp);
            }else{
                rsList.add(policyScope);
                mp.put(assignRes.policyDefinitionId(),rsList);
                logger.debug(mp);
            }
        });



        policyClientImpl(request.getHeader("userName")).getPolicySetDefinitions().listBuiltIn().iterableByPage().forEach(resp -> {
            resp.getElements().stream().filter(e-> e.displayName().startsWith(groupType)).forEach(value ->
            {

                InitiativesResponseDto obj = InitiativesResponseDto
                        .builder()
                        .id(value.id())
                        .displayName(value.displayName())
                        .policyDescription(value.description())
                        .name(value. name())
                        .assignStatus(mp.containsKey(value.id()))
                        .resourceGroup(mp.containsKey(value.id())?mp.get(value.id()):null)
                        .build();
                if(!obj.getDisplayName().equals("NIST SP 800-171 Rev. 2")){
                    res.add(obj);
                }
            }
            );
        });
        logger.debug(res);
        logger.info("get Initiative Versions ended ...");
        return res;
    }

    @Override
    public ArrayList<InitiativesResponseDto> getInitiativePolicies(HttpServletRequest request,String initiativeID) throws JSONException {
        ArrayList <InitiativesResponseDto> res=new ArrayList();
        logger.info("get Initiative policies ...");

        policyClientImpl(request.getHeader("userName")).getPolicySetDefinitions().listBuiltIn().iterableByPage().forEach(resp -> {
            resp.getElements().stream().parallel().filter(e-> e.displayName().startsWith(initiativeID)).forEach(value ->
                    {
                        value.policyDefinitions().stream().limit(25).forEach(pol ->
                        {
                            String policyName =  pol.policyDefinitionId().substring(pol.policyDefinitionId().lastIndexOf("/") + 1);

                            PolicyDefinitionInner pd= null;
                            try {
                                pd = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().getBuiltIn(policyName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            InitiativesResponseDto obj = InitiativesResponseDto
                                    .builder()
                                    .id(pol.policyDefinitionId())
                                    .policyDescription(pd.description())
                                    .displayName(pd.displayName())
                                    .name(policyName)
                                    .policyType(String.valueOf(pd.policyType()))
                                    .build();

                            res.add(obj);
                        });

                    }
            );
        });
        logger.debug(res);
        logger.info("get Initiative policies ended ...");
        return res;
    }

    private ArrayList<PolicyDefinitionDto> getPolicyDetails(HttpServletRequest request,List<String> ids) throws JSONException {
        ArrayList<PolicyDefinitionDto> as=new ArrayList<>();

        for (int i=0;i<ids.size();i++){
            String id = String.valueOf(ids.get(i));
            String policyId =  id.substring(id.lastIndexOf("/") + 1);

            PolicyDefinitionInner df=policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                    .getBuiltIn(policyId);

            PolicyDefinitionDto policyDefinitionDto = PolicyDefinitionDto
                    .builder()
                    .id(id)
                    .policyType(String.valueOf(df.policyType()))
                    .policyDescription(df.description())
                    .name(df.displayName())
                    .build();

            as.add(policyDefinitionDto);

        }

//         ids.parallelStream().map( id ->
//                {
//                  //  return "abc";
//                    return "null";
//                }
//        ).collect(Collectors.toList());
        return as;
    }

    @Override
    public AKSPolicyAssignResponseDto assignPolicy(HttpServletRequest request,AKSPolicyAssignRequestDto aksPolicyAssignRequestDto) throws JSONException {
        try{
            logger.info("Assign Policy is started...");
            Map<String, ParameterDefinitionsValue> parameter=null;
            if(aksPolicyAssignRequestDto.getPolicyType().equals("Custom")){
                logger.debug("policy type :"+aksPolicyAssignRequestDto.getPolicyType());
                parameter = policyClientImpl(request.getHeader("userName"))
                        .getPolicyDefinitions()
                        .get(aksPolicyAssignRequestDto.getName())
                        .parameters();
                logger.debug(parameter);
            }else{
                parameter = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                        .getBuiltIn(aksPolicyAssignRequestDto.getName())
                        .parameters();
                logger.debug(parameter);

            }

            String parameterKey=null;

            if(parameter!=null){

                parameterKey=new ArrayList<>(parameter.keySet()).get(0);
            }
            for (Map.Entry<String, ParameterDefinitionsValue> e:parameter.entrySet()) {
                logger.debug("Key : " + e.getKey());
                logger.debug("Allowed Values : " + e.getValue().allowedValues());
                logger.debug("Default Values : " + e.getValue().defaultValue());
            }

//            System.out.println("Parameter Key --" +new ParameterValuesValue().withValue(aksPolicyAssignRequestDto.getParameters()));

            //parameterValuesValue = new ParameterValuesValue().withValue(aksPolicyAssignRequestDto.getParameters().get(0));

            String subscriptionId=policyClientImpl(request.getHeader("userName")).getSubscriptionId();

            Map<String, ParameterValuesValue> parameterValuesValueMap= new HashMap<>();
            ParameterValuesValue parameterValuesValue;
            if(!aksPolicyAssignRequestDto.getParameters().contains(null)) {
                if (aksPolicyAssignRequestDto.getParameterType().equalsIgnoreCase("String")) {
                    parameterValuesValue = new ParameterValuesValue()
                            .withValue(aksPolicyAssignRequestDto.getParameters().get(0));
                } else {
                    parameterValuesValue = new ParameterValuesValue()
                            .withValue(aksPolicyAssignRequestDto.getParameters());
                }
            }else{
                parameterValuesValue = new ParameterValuesValue()
                        .withValue("");
            }
            parameterValuesValueMap.put(parameterKey,parameterValuesValue);
            //parameterValuesValueMap.put(parameterKey,new ParameterValuesValue().withValue(12));

            PolicyAssignmentInner policyAssignmentInner;

            String scope="/subscriptions/"+subscriptionId+"/resourceGroups/"+aksPolicyAssignRequestDto.getResourceGroupName();
            logger.debug("scope :"+scope);
            if(!aksPolicyAssignRequestDto.getExcludedScopes().contains(null)){
//                for (String excludeSCope:aksPolicyAssignRequestDto.getExcludedScopes()) {
//                    excludeSCope
//                }

                aksPolicyAssignRequestDto.setExcludedScopes( aksPolicyAssignRequestDto.getExcludedScopes()
                        .stream().map(s->(scope+"/"+s)).collect(Collectors.toList()));
            }

            logger.debug("policy imple ---->"+ policyClientImpl(request.getHeader("userName")).getPolicyAssignments());

            if(aksPolicyAssignRequestDto.getExcludedScopes().contains(null)
                    && aksPolicyAssignRequestDto.getParameters().contains(null) ){
                policyAssignmentInner=policyClientImpl(request.getHeader("userName"))
                        .getPolicyAssignments()
                        .create(scope,
                                aksPolicyAssignRequestDto.getName(),
                                new PolicyAssignmentInner()
                                        .withPolicyDefinitionId(aksPolicyAssignRequestDto.getId())
                                        .withDisplayName(aksPolicyAssignRequestDto.getName())
                                        .withDescription(aksPolicyAssignRequestDto.getDescription())
                                        .withScope(scope)
                                        .withEnforcementMode(EnforcementMode.DEFAULT));

                policyAssignmentInner.validate();

            }else if(aksPolicyAssignRequestDto.getExcludedScopes().contains(null)
                    && !aksPolicyAssignRequestDto.getParameters().contains(null)){
                policyAssignmentInner = policyClientImpl(request.getHeader("userName"))
                        .getPolicyAssignments()
                        .create(scope,
                                aksPolicyAssignRequestDto.getName(),
                                new PolicyAssignmentInner()
                                        .withPolicyDefinitionId(aksPolicyAssignRequestDto.getId())
                                        .withDisplayName(aksPolicyAssignRequestDto.getName())
                                        .withDescription(aksPolicyAssignRequestDto.getDescription())
                                        .withScope(scope)
                                        .withEnforcementMode(EnforcementMode.DEFAULT)
                                        .withParameters(parameterValuesValueMap));
                policyAssignmentInner.validate();

            }else if(!aksPolicyAssignRequestDto.getExcludedScopes().contains(null)
                    && aksPolicyAssignRequestDto.getParameters().contains(null)) {
                policyAssignmentInner = policyClientImpl(request.getHeader("userName"))
                        .getPolicyAssignments()
                        .create(scope,
                                aksPolicyAssignRequestDto.getName(),
                                new PolicyAssignmentInner()
                                        .withPolicyDefinitionId(aksPolicyAssignRequestDto.getId())
                                        .withDisplayName(aksPolicyAssignRequestDto.getName())
                                        .withDescription(aksPolicyAssignRequestDto.getDescription())
                                        .withScope(scope)
                                        .withEnforcementMode(EnforcementMode.DEFAULT)
                                        .withNotScopes(aksPolicyAssignRequestDto.getExcludedScopes()));
                policyAssignmentInner.validate();

            }else{
                policyAssignmentInner = policyClientImpl(request.getHeader("userName"))
                        .getPolicyAssignments()
                        .create(scope,
                                aksPolicyAssignRequestDto.getName(),
                                new PolicyAssignmentInner()
                                        .withPolicyDefinitionId(aksPolicyAssignRequestDto.getId())
                                        .withDisplayName(aksPolicyAssignRequestDto.getName())
                                        .withDescription(aksPolicyAssignRequestDto.getDescription())
                                        .withScope(scope)
                                        .withEnforcementMode(EnforcementMode.DEFAULT)
                                        .withParameters(parameterValuesValueMap)
                                        .withNotScopes(aksPolicyAssignRequestDto.getExcludedScopes()));
                policyAssignmentInner.validate();
            }
            logger.debug("policyAssignmentInner :"+policyAssignmentInner.id());
            logger.info("policy is assigned");

            return AKSPolicyAssignResponseDto.builder()
                    .id(policyAssignmentInner.id())
                    .scope(policyAssignmentInner.scope())
                    .name(policyAssignmentInner.displayName())
                    .description(policyAssignmentInner.description())
                    .excludedScopes(policyAssignmentInner.notScopes())
                    .parameters(policyAssignmentInner.parameters())
                    .build();

        }catch (CloudException e) {
            logger.error("Error occurred while assignPolicy:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
public Map<String,String> assignPolicyInitiative (HttpServletRequest request,InitiativeAssignRequestDto initiativeAssignRequestDto){
        Map<String,String> map=new HashMap<>();
        logger.info("assigning PolicyInitiative ...");

        try {
            StringBuffer response = new StringBuffer();

            org.json.simple.JSONObject json = null;
            org.json.simple.JSONObject json3;

            String inputLine = null;

            URL url =null;
            ArrayList<PolicySetDto> policySeList =new ArrayList<> ();

            try{
                String initiativeNameForUrl=initiativeAssignRequestDto.getDisplayName();
                if(initiativeAssignRequestDto.getDisplayName()!=null){
                    if(initiativeAssignRequestDto.getDisplayName().contains(" "))
                        initiativeNameForUrl = initiativeAssignRequestDto.getDisplayName().replaceAll(" ", "_");

                    if(initiativeAssignRequestDto.getDisplayName().contains(":"))
                        initiativeNameForUrl =initiativeNameForUrl.replaceAll(":", "_");
                }
                AzureCredentials credentials=applicationTokenCredentials(request.getHeader("userName"));
                url = new URL("https://management.azure.com/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+initiativeAssignRequestDto.getResourceGroup()+"/providers/Microsoft.Authorization/policyAssignments/"+initiativeNameForUrl+"?api-version=2021-06-01");
                GetToken getToken = new GetToken(vaultService, applicationProperties, userService);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));
                conn.setDoOutput(true);

                Map prop = new HashMap<String,String>();
                prop.put("displayName", initiativeAssignRequestDto.getDisplayName());
                prop.put("description", initiativeAssignRequestDto.getDescription());
                prop.put("policyDefinitionId",initiativeAssignRequestDto.getPolicyDefinitionId());
                prop.put("enforcementMode","DoNotEnforce");
                prop.put("notScopes",initiativeAssignRequestDto.getExcludedScopes());
                logger.debug(prop);

                String properties = "{\n" +
                        "  \"location\":" + new Gson().toJson("centralindia") + " ,\n" +
                        "  \"identity\": { \n \"type\" : " + new Gson().toJson("SystemAssigned") + "\n }," +
                        "  \"properties\": " + new Gson().toJson(prop) + "\n" +
                        "}";
                logger.debug(properties);

                byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

                OutputStream stream = conn.getOutputStream();

                stream.write(properties1);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) { //success

                    map.put("code",String.valueOf(HttpURLConnection.HTTP_CREATED));
                    BufferedReader in1 = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((inputLine = in1.readLine()) != null) {
                        response.append(inputLine);
                    }
                    logger.info(response);

                    Object obj = JSONValue.parse(response.toString());
                    json = (org.json.simple.JSONObject) obj;
                    map.put("status","Created successfully");
                    logger.info(map);
                }

            }
            catch(Exception e){
                logger.error("Error occurred while assignPolicyInitiative::::"+e.getMessage());
                logger.error(e);
            }

            return map;

        } catch (CloudException e) {
            logger.error("Error occured while assignPolicyInitiative::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

}


    private PolicyParamDto getPolicyParam(HttpServletRequest request,String policyName) throws JSONException {
        PolicyParamDto policyParamDto=new PolicyParamDto();
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());

        if(policyName.equalsIgnoreCase("azx-pol-allowed-locations")) {
            List<String> locationList = azure.getCurrentSubscription()
                    .listLocations()
                    .stream()
                    .map(com.microsoft.azure.management.resources.Location::displayName)
                    .collect(Collectors.toList());
            policyParamDto.setType("Array");
            policyParamDto.setParamList(locationList);
            return policyParamDto;
        }
        if(policyName.equalsIgnoreCase("azx-pol-deny-resourcesnotallowed")){

            List<String> resourceTypeList = azure.providers()
                    .list()
                    .stream()
                    .map(com.microsoft.azure.management.resources.Provider::resourceTypes)
                    .collect(Collectors.toList())
                    .get(0)
                    .stream()
                    .map(com.microsoft.azure.management.resources.ProviderResourceType::resourceType)
                    .collect(Collectors.toList());
            policyParamDto.setParamList(resourceTypeList);
            policyParamDto.setType("Array");
            return policyParamDto;
        }
        if(policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                .get(policyName)
                .parameters()!=null){
            List<Object> objectList = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                    .get(policyName)
                    .parameters()
                    .values().stream()
                    .map(ParameterDefinitionsValue::allowedValues)
                    .collect(Collectors.toList()).get(0);
            policyParamDto.setType("String");
            policyParamDto.setParamList(objectList);
            return policyParamDto;
        }else{
            policyParamDto.setType("null");
            policyParamDto.setParamList(Collections.singletonList("null"));
            return policyParamDto;
        }
    }

    @Override
    public PolicyParamDto getPolicyParamTypeWise(HttpServletRequest request,String policyName,String policyType) throws JSONException {

        if(policyType.equals("Custom")){
            logger.info("getPolicyParamTypeWise");
            return getPolicyParam(request,policyName);
        }else{
            policyName =  policyName.substring(policyName.lastIndexOf("/") + 1);
            logger.info(policyName);
            return getPolicyParamBuiltIn(request,policyName);
        }
    }




    private PolicyParamDto getPolicyParamBuiltIn(HttpServletRequest request,String policyName) throws JSONException {
        PolicyParamDto policyParamDto=new PolicyParamDto();
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());


        if(policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                .getBuiltIn(policyName)
                .parameters()!=null){
            List<Object> objectList =policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().getBuiltIn(policyName)
                    .parameters()
                    .values().stream()
                    .map(ParameterDefinitionsValue::allowedValues)
                    .collect(Collectors.toList()).get(0);
            policyParamDto.setType("String");

            policyParamDto.setParamList(objectList);
            return policyParamDto;
        }


        if(policyClientImpl(request.getHeader("userName")).getPolicyDefinitions()
                .getBuiltIn(policyName)
                .parameters()!=null){
            List<Object> objectList =policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().getBuiltIn(policyName)
                    .parameters()
                    .values().stream()
                    .map(ParameterDefinitionsValue::allowedValues)
                    .collect(Collectors.toList()).get(0);
            policyParamDto.setType("String");
            policyParamDto.setParamList(objectList);
            return policyParamDto;
        }


        else{
            policyParamDto.setType("null");
            policyParamDto.setParamList(Collections.singletonList("null"));
            return policyParamDto;
        }
    }


    @Override
    public List<AzureResourceDto> getResource (HttpServletRequest request, String resourceGroupName) throws JSONException {
        List<AzureResourceDto> azureResourceDtoList = new ArrayList<>();
        logger.info("get resources...");
        aksDeployService
                .getAKSClusters(request,resourceGroupName)
                .forEach(aksDeployResponse -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(aksDeployResponse.getAksId())
                            .name(aksDeployResponse.getAksName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);
                });
        logger.debug(azureResourceDtoList);

        vNetService.getVnets(request,resourceGroupName,null)
                .forEach(vNetResponseDto -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(vNetResponseDto.getId())
                            .name(vNetResponseDto.getName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);
                });
        logger.debug(azureResourceDtoList);

        azureKeyVaultService.getKeyVault(request,resourceGroupName)
                .forEach(keyVaultResponseDto -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(keyVaultResponseDto.getId())
                            .name(keyVaultResponseDto.getName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);
                });
        logger.debug(azureResourceDtoList);
        azureSqlDBService.sqlServers(request,resourceGroupName)
                .forEach(sqlServerResponseDto -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(sqlServerResponseDto.getId())
                            .name(sqlServerResponseDto.getName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);
                });
        logger.debug(azureResourceDtoList);
        azureStrorageService.storage(request,resourceGroupName)
                .forEach(strorageResponseDto -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(strorageResponseDto.getId())
                            .name(strorageResponseDto.getName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);

                });
        logger.debug(azureResourceDtoList);
        aksMonitoringService.actionGroups(request,resourceGroupName)
                .forEach(actionGroupResponse -> {
                    AzureResourceDto azureResourceDto = AzureResourceDto
                            .builder()
                            .id(actionGroupResponse.getId())
                            .name(actionGroupResponse.getName())
                            .build();
                    azureResourceDtoList.add(azureResourceDto);

                });
        logger.debug(azureResourceDtoList);
        logger.info("get resources ended...");
        return azureResourceDtoList;
    }

    private AzureCredentials applicationTokenCredentials (String userName) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(userName);

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        return
                new AzureCredentials(
                        new ApplicationTokenCredentials(
                                secret.getClientId(), secret.getTenantId(), secret.getClientSecret(),
                                AzureEnvironment.AZURE),
                        secret.getSubscriptionId());
    }

    private PolicyClientImpl policyClientImpl (String userName) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(userName);

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        AzureProfile profile = new AzureProfile(
                secret.getTenantId(),
                secret.getSubscriptionId(),
                com.azure.core.management.AzureEnvironment.AZURE);

        TokenCredential credential = new ClientSecretCredentialBuilder()
                .clientId(secret.getClientId())
                .clientSecret(secret.getClientSecret())
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .tenantId(secret.getTenantId())
                .build();

        return new PolicyClientBuilder()
                .pipeline(HttpPipelineProvider.buildHttpPipeline(credential, profile))
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient();


    }

    private void polallowedlocations (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-allowed-locations-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();
            Map<String, Object> additionalProperties = new HashMap<>();
            additionalProperties.put("strongType", "location");

            parameterDefinitionsValueMap.put(
                    "listOfAllowedLocations",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.ARRAY)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withAdditionalProperties(additionalProperties)
                                    .withDisplayName("Allowed locations")
                                    .withDescription("The list of allowed locations for resources.")));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-allowed-locations",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("The list of locations that can be specified when deploying resources")
                            .withDisplayName("azx-pol-allowed-locations"));

            policyDefinitionInner.validate();

            System.out.println("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polallowedlocations:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polauditdenynsg1024addressprefixes (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-audit-deny-nsg-1024-addressprefixes-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

            List<Object> effect = new ArrayList<>();
            effect.add("deny");
            effect.add("audit");

            parameterDefinitionsValueMap.put(
                    "effect",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withDisplayName("Effect required to apply in this policy")
                                    .withDescription("Effect required to apply in this policy."))
                            .withAllowedValues(effect));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-sourceaddressprefixes-1024ip",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("This policy restrict creation of network security rules with adddress prefixes-array-type sources with more than 1024 endpoints")
                            .withDisplayName("azx-pol-deny-sourceaddressprefixes-1024ip"));

            policyDefinitionInner.validate();

            System.out.println("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polauditdenynsg1024addressprefixes::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polauditdenynsg1024addressprefix (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-audit-deny-nsg-1024-addressprefix-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

            List<Object> effect = new ArrayList<>();
            effect.add("deny");
            effect.add("audit");

            parameterDefinitionsValueMap.put(
                    "effect",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withDisplayName("Effect required to apply in this policy")
                                    .withDescription("Effect required to apply in this policy."))
                            .withAllowedValues(effect));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-sourceaddressprefix-1024ip",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("This policy restrict creation of network security rules with adddress prefixes-array-type sources with more than 1024 endpoints")
                            .withDisplayName("azx-pol-deny-sourceaddressprefix-1024ip"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polauditdenynsg1024addressprefix:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void poldenynsgsourcesany (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-deny-nsg-sources-any-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

            List<Object> effect = new ArrayList<>();
            effect.add("deny");
            effect.add("audit");

            parameterDefinitionsValueMap.put(
                    "effect",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withDisplayName("Effect required to apply in this policy")
                                    .withDescription("Effect required to apply in this policy."))
                            .withAllowedValues(effect));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-any-source",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("This policy restrict creation of network security rules sources from sources -any- value as endpoint")
                            .withDisplayName("azx-pol-deny-any-source"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while poldenynsgsourcesany::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void poldenypeeringsassignment (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-deny-peerings-assignment-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

           /* List<String> allowValues= new ArrayList<>();
            allowValues.add("deny");
            allowValues.add("audit");*/

            parameterDefinitionsValueMap.put(
                    "effect",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withDisplayName("Effect required to apply in this policy")
                                    .withDescription("Effect required to apply in this policy.")));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-peerings-assignment",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("This policy restrict creation of peering to any network and it can not be associated.")
                            .withDisplayName("azx-pol-deny-peerings-assignment"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while poldenypeeringsassignment:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    private void poldenypipassignment (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-deny-pip-assignment-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-pip-assignment",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("This policy prevents the creation of public ip resources")
                            .withDisplayName("azx-pol-deny-pip-assignment"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while poldenypipassignment:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void poldenyresourcesnotallowed (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-deny-resourcesnotallowed-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

           /* List<String> listresourcesnotallowed= new ArrayList<>();
            listresourcesnotallowed.add("Microsoft.Network/virtualNetworkGateways");
            listresourcesnotallowed.add ("Microsoft.Network/vpnSites");
            listresourcesnotallowed.add ("Microsoft.Network/expressRouteCircuits");
            listresourcesnotallowed.add ( "Microsoft.Network/expressRouteGateways");
            listresourcesnotallowed.add ("Microsoft.Network/connections");
            listresourcesnotallowed.add ("Microsoft.Network/virtualHaz_solutions");
            listresourcesnotallowed.add ("Microsoft.Network/vpnGateways");*/

            Map<String, Object> additionalProperties = new HashMap<>();
            additionalProperties.put("strongType", "resourceTypes");

            parameterDefinitionsValueMap.put(
                    "listOfResourceTypesNotAllowed",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.ARRAY)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withAdditionalProperties(additionalProperties)
                                    .withDisplayName("Not allowed resource types")
                                    .withDescription("The list of resource types that cannot be deployed.")));
            //.withAllowedValues(Collections.singletonList(listresourcesnotallowed)));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-deny-resourcesnotallowed",
                    new PolicyDefinitionInner()
                            .withParameters(parameterDefinitionsValueMap)
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("All")
                            .withDescription("The list of locations that can be specified when deploying resources")
                            .withDisplayName("azx-pol-deny-resourcesnotallowed"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while poldenyresourcesnotallowed::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polinherittagapplication (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-inherit-tag-application-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-inherit-tag-application",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Require Application tag on resource groups")
                            .withDisplayName("azx-pol-inherit-tag-application"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polinherittagapplication:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polinherittagengagement (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-inherit-tag-engagement-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-inherit-tag-engagement",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Require Engagement tag on resource groups")
                            .withDisplayName("azx-pol-inherit-tag-engagement"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polinherittagengagement::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polinherittagenvironment (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-inherit-tag-environment-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-inherit-tag-environment",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Require Engagement tag on resource groups")
                            .withDisplayName("azx-pol-inherit-tag-environment"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polinherittagenvironment:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polinherittaglob (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-inherit-tag-lob-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-inherit-tag-lob",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Require LOB tag on resource groups")
                            .withDisplayName("azx-pol-inherit-tag-lob"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polinherittaglob:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polinherittagowner (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-inherit-tag-owner-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-inherit-tag-owner",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Require Owner tag on resource groups")
                            .withDisplayName("azx-pol-inherit-tag-owner"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polinherittagowner::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void pollimitregion (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-limit-region-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-limit-region",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("This policy limits deployment to specific regions")
                            .withDisplayName("azx-pol-limit-region"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while pollimitregion:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polpipspecificsubnet (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-pip-specific-subnet-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            Map<String, ParameterDefinitionsValue> parameterDefinitionsValueMap = new HashMap<>();

            parameterDefinitionsValueMap.put(
                    "listOfSubnets",
                    new ParameterDefinitionsValue()
                            .withType(ParameterType.ARRAY)
                            .withMetadata(new ParameterDefinitionsValueMetadata()
                                    .withDisplayName("List of Subnets that can use a public IP")
                                    .withDescription("The subnetIds parameter must be provided with a list of subnets in format of resource ID (e.g /saz_solutionscriptions/{saz_solutionscription_id}/resourcegroups/{resource_group}/providers/microsoft.network/virtualnetworks/{vnet}/subnets/{subnet-name})")));

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-pip-specific-subnet",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withParameters(parameterDefinitionsValueMap)
                            .withDescription("Allow Public IP from Specific Subnet")
                            .withDisplayName("azx-pol-pip-specific-subnet"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polpipspecificsubnet:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polrequirergtagapplication (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-require-rgtag-application-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-require-rgtag-application",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Append Application tag from resource group")
                            .withDisplayName("azx-pol-require-rgtag-application"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polrequirergtagapplication:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polrequirergtagengagement (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-require-rgtag-engagement-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-require-rgtag-engagement",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Append Engagement tag from resource group")
                            .withDisplayName("azx-pol-require-rgtag-engagement"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polrequirergtagengagement:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polrequirergtagenvironment (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-require-rgtag-environment-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-require-rgtag-environment",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Append Environment tag from resource group")
                            .withDisplayName("azx-pol-require-rgtag-environment"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polrequirergtagenvironment::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polrequirergtaglob (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-require-rgtag-lob-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-require-rgtag-lob",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Append LOB tag from resource group")
                            .withDisplayName("azx-pol-require-rgtag-lob"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polrequirergtaglob:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void polrequirergtagowner (HttpServletRequest request) throws JSONException {
        Resource resource = resourceLoader.getResource("classpath:policyrule/pol-require-rgtag-owner-rule.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object policyRuleObj = parser.parse(inputStreamReader);

            PolicyDefinitionInner policyDefinitionInner = policyClientImpl(request.getHeader("userName")).getPolicyDefinitions().createOrUpdate(
                    "azx-pol-require-rgtag-owner",
                    new PolicyDefinitionInner()
                            .withPolicyRule(policyRuleObj)
                            .withPolicyType(PolicyType.CUSTOM)
                            .withMode("Indexed")
                            .withDescription("Append Owner tag from resource group")
                            .withDisplayName("azx-pol-require-rgtag-owner"));

            policyDefinitionInner.validate();

            logger.info("Policy " + policyDefinitionInner.displayName() + " Created Successfully");

        } catch (CloudException | IOException | ParseException e) {
            logger.error("Error occured while polrequirergtagowner :::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}