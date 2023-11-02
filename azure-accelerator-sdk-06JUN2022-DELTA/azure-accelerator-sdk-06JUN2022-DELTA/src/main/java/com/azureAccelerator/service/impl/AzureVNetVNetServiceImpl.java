package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureVNetService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.google.gson.Gson;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkPeering;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AzureVNetVNetServiceImpl implements AzureVNetService {

  private static final Logger logger = LogManager.getLogger(AzureVNetVNetServiceImpl.class);

  private final VaultService vaultService;
  private final ApplicationProperties applicationProperties;
    private final UserService userService;


    public AzureVNetVNetServiceImpl(VaultService vaultService,
                                    ApplicationProperties applicationProperties, UserService userService) {

    this.vaultService = vaultService;
    this.applicationProperties = applicationProperties;
        this.userService = userService;
    }

  @Override
  public VNetResponseDto createVNet(HttpServletRequest request, VNetDto vNetDto) throws Exception {

      /*String output=null;
      String inputLine=null;
      StringBuffer response = new StringBuffer();
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
              .authenticate(credentials.getApplicationTokenCredentials())
              .withSubscription(credentials.getSubscriptionId());

      //AzureResourceManager azureResourceManager=AzureResourceManager.configure();
      System.out.println("{"+credentials.getSubscriptionId()+"}"+credentials.getApplicationTokenCredentials());

      URL url=new URL("https://management.azure.com/" +
              "subscriptions/"+credentials.getSubscriptionId()+"/" +
              "resourceGroups/" +vNetDto.getResourceGroupName()+"/providers/Microsoft.Network" +
              "/virtualNetworks/"+vNetDto.getName()+"?api-version=2021-08-01");

      GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
      logger.info("url : " + url.toString());


      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("PUT");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization","bearer "+getToken.gettingToken(request.getHeader("userName")));

      System.out.println(new Gson().toJson(vNetDto.getProperties()));
      String properties = "{\n" +
              "  \"properties\": "+new Gson().toJson(vNetDto.getProperties())+"," +
              "  \"location\": \""+vNetDto.getLocation()+"\"\n" +
              "}";



      byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

      OutputStream stream = conn.getOutputStream();

      stream.write(properties1);

      logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());
      System.out.println(conn.getResponseCode() + " ----- " + conn.getResponseMessage());

      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_CREATED ||
              conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) { //success


          BufferedReader in1 = new BufferedReader(new InputStreamReader(
                  conn.getInputStream()));

          while ((inputLine = in1.readLine()) != null) {
              response.append(inputLine);
          }
          in1.close();
      }else{
          throw new Exception("You are given wrong details of vnet or vnet Ip address");
      }

      return response.toString();
*/
   Network network = null;
    Map<String,String> subnetAddressMap = new HashMap<>();
      Map<String,String> tags=new HashMap<>();
      tags.put("ResourceGroupName",vNetDto.getResourceGroupName());
      tags.put("name",vNetDto.getName());
      tags.put("application","azx");
      tags.put("Environment","dev");
      tags.put("project","cloud");

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
        logger.info(credentials.getSubscriptionId());
        for (SubnetDto subnetDto:
             vNetDto.getSubnetDtoList()) {
            subnetAddressMap.put(subnetDto.getName(),subnetDto.getAddressPrefix());
        }
      logger.info("Creating virtual network...");
      network = azure.networks()
          .define(vNetDto.getName())
          .withRegion(vNetDto.getRegion())
          .withExistingResourceGroup(vNetDto.getResourceGroupName())
          .withAddressSpace(vNetDto.getAddressSpace()).withSubnets(subnetAddressMap)
          .withTags(tags)
          .withTag("Name",vNetDto.getName())
          .create();
      logger.info("Virtual network Created");
    } catch (CloudException e) {
        logger.error("Exception: createVNet : "+ e.getMessage());
      e.getLocalizedMessage();
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    SubnetDto subnetDto = new SubnetDto();
    List<SubnetDto> subnetDtoList= new ArrayList<>();
    network.subnets().forEach((s, subnet) -> {
        logger.info(subnet.name() + "::" + subnet.addressPrefix());
      subnetDto.setName(subnet.name());
      subnetDto.setAddressPrefix(subnet.addressPrefix());
      subnetDtoList.add(subnetDto);
    });

    return
        VNetResponseDto
            .builder()
            .id(network.id())
            .name(network.name())
            .addressSpace(String.join("", network.addressSpaces()))
            .location(network.regionName())
            .subnetDto(subnetDto).subnetDtoList(subnetDtoList)
                .tags(network.tags())
            .build();
  }

  @Override
  public Network updateVNet(HttpServletRequest request) throws JSONException {

    AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
    Azure azure = Azure.configure() // Initial an Azure.Configurable object
        .authenticate(credentials.getApplicationTokenCredentials())
        .withSubscription(credentials.getSubscriptionId());

    Network network = azure.networks().getById("");
    //network.update().withAddressSpace("").withSubnets("")
    return null;
  }

  @Override
  public String deleteVNet(HttpServletRequest request,String vNetId) throws Exception {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("VNet deleting...");
      Network network = azure
          .networks()
          .getById(vNetId);
      System.out.println("network details>>>>"+network);
      if(network!=null) {
          List<String> peeringIds =
                  network
                          .peerings()
                          .list()
                          .stream()
                          .map(HasId::id)
                          .collect(Collectors.toList());

          peeringIds.forEach(peeringId -> {
              network.peerings().deleteById(peeringId);
          });
      }else{
          throw new Exception(vNetId +" details not found");
      }

      azure.networks().deleteById(vNetId);
      logger.info("Vnet deleted");

    } catch (CloudException e) {
        logger.error("Exception: delete Vnet : "+ e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return null;
  }

    @Override
    public String deleteVNets(HttpServletRequest request,List<VNetDto> vNetDto) throws Exception {
        try{
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());

            for (VNetDto vNetDto1:vNetDto) {
                String vNetId="/subscriptions/"+azure.subscriptionId()+"/resourceGroups/"+vNetDto1.getResourceGroupName()+"/providers/Microsoft.Network/virtualNetworks/"+vNetDto1.getName();
                logger.info("VNet deleting...");
                Network network = azure
                        .networks()
                        .getById(vNetId);
                System.out.println("network details>>>>"+network);
                if(network!=null) {
                    List<String> peeringIds =
                            network
                                    .peerings()
                                    .list()
                                    .stream()
                                    .map(HasId::id)
                                    .collect(Collectors.toList());

                    peeringIds.forEach(peeringId -> {
                        network.peerings().deleteById(peeringId);
                    });
                }else{
                    throw new Exception(vNetId +" details not found");
                }
                azure.networks().deleteById(vNetId);

            }

        } catch (CloudException e) {
            logger.error("Exception: delete Vnet : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

  @Override
  public List<String> resourceGroups(HttpServletRequest request) throws JSONException {
    Azure azure = null;
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

    } catch (CloudException e) {
        logger.error("RG exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return azure
        .resourceGroups()
        .list()
        .stream()
        .map(HasName::name)
        .collect(Collectors.toList());
  }

  @Override
  public List<VNetResponseDto> getVnets(HttpServletRequest request,String resourceGroupName,String region) throws JSONException {
    List<VNetResponseDto> vNetResponseDtoList = new ArrayList<>();
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
        logger.info(credentials.getSubscriptionId());

      List<Network> networkList = new ArrayList<>(
          azure.networks().listByResourceGroup(resourceGroupName));

      networkList.forEach(network -> {
          // network Peering details
          boolean isVNetPeered = false;
          if(network.peerings().list().size() > 0){
              isVNetPeered =true;
              logger.info("is peered ::"+isVNetPeered);
          }
          if (region!=null){
              if(region.equalsIgnoreCase(network.regionName())) {

                  VNetResponseDto vNetResponseDto = VNetResponseDto
                          .builder()
                          .id(network.id())
                          .name(network.name())
                          .addressSpace(String.join("", network.addressSpaces()))
                          .location(network.regionName())
                          .isVNetPeered(isVNetPeered)
                          .tags(network.tags())
                          .build();

                  vNetResponseDtoList.add(vNetResponseDto);
              }
          }else {
              VNetResponseDto vNetResponseDto = VNetResponseDto
                      .builder()
                      .id(network.id())
                      .name(network.name())
                      .addressSpace(String.join("", network.addressSpaces()))
                      .location(network.regionName())
                      .isVNetPeered(isVNetPeered)
                      .tags(network.tags())
                      .build();
              vNetResponseDtoList.add(vNetResponseDto);
          }

      });
    } catch (CloudException e) {
        logger.error("Error occured while : getVnets::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return vNetResponseDtoList;
  }


    @Override
    public VNetPeeringResponseDto createVNetPeering(HttpServletRequest request,VNetPeeringDto vNetPeeringDto) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            String remoteNetworkId="/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+vNetPeeringDto.getResourceGroup()+"/providers/Microsoft.Network/virtualNetworks/"+vNetPeeringDto.getRemoteVNetId();

            Network sourceNetwork = azure.networks().getById(vNetPeeringDto.getSourceVNetId());
            Network remoteNetwork = azure.networks().getById(remoteNetworkId);

            logger.info("Creating VNet peering in the same region and subscription with default settings ...");

            NetworkPeering peering = sourceNetwork
                    .peerings()
                    .define(vNetPeeringDto.getPeeringName())
                    .withRemoteNetwork(remoteNetwork)
                    .create();

            logger.info("Created VNet peering in the same region and subscription with default settings...");

            logger.info("Updating the peering to allow access between " + sourceNetwork.name() + " and " + remoteNetwork.name());

            NetworkPeering networkPeering = peering
                    .update()
                    .withAccessBetweenBothNetworks()
                    .apply();
            logger.info("Updated the peering to allow access between " + sourceNetwork.name() + " and " + remoteNetwork.name());

            return VNetPeeringResponseDto.builder()
                    .id(networkPeering.id())
                    .networkId(networkPeering.networkId())
                    .name(networkPeering.name())
                    .state(networkPeering.state())
                    .remoteNetwokName(networkPeering.getRemoteNetwork().name())
                    .build();
        } catch (CloudException e) {
            logger.error("createVNetPeering Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public String deleteVNetPeering(HttpServletRequest request,String sourceVNetId,String peeringId) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());


            logger.info("Deleting the peering from the networks...");

            Network sourceNetwork = azure.networks().getById(sourceVNetId);
            sourceNetwork.peerings().deleteById(peeringId);

            logger.info("Deleted the peering from both sides.");
            return null;
        } catch (CloudException e) {
            logger.error("deleteVNetPeering Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public List<VNetPeeringResponseDto> getVnetPeering(HttpServletRequest request,String sourceVNetId) throws JSONException {
        try {
            List<VNetPeeringResponseDto> vNetPeeringResponseDtoList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());


            Network sourceNetwork = azure.networks().getById(sourceVNetId);
            sourceNetwork.peerings().list()
                    .forEach(networkPeering -> {
                        VNetPeeringResponseDto vNetPeeringResponseDto = VNetPeeringResponseDto
                                .builder()
                                .id(networkPeering.id())
                                .name(networkPeering.name())
                                .state(networkPeering.state())
                                .remoteNetwokName(networkPeering.getRemoteNetwork().name())
                                .allowForwardedTraffic(networkPeering.isTrafficForwardingFromRemoteNetworkAllowed())
                                .build();
                        vNetPeeringResponseDtoList.add(vNetPeeringResponseDto);
                    });
            return vNetPeeringResponseDtoList;
        } catch (CloudException e) {
            logger.error("delete NSG Exception ; "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public VNetPeeringResponseDto updateVnetPeering(HttpServletRequest request,UpdateVnetPeeringDto updateVnetPeeringDto) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            Network sourceNetwork = azure.networks().getById(updateVnetPeeringDto.getSourceVNetId());
            NetworkPeering networkPeering = sourceNetwork.peerings().getById(updateVnetPeeringDto.getPeeringId());
            NetworkPeering updatePeering = null;

            if(updateVnetPeeringDto.isAllowForwardedTraffic()) {
                logger.info("Updating peering with allowForwardedTraffic");
                updatePeering=networkPeering.update().withTrafficForwardingBetweenBothNetworks().apply();

            }else {
                logger.info("Updating peering without allowForwardedTraffic");
                updatePeering= networkPeering.update().withoutTrafficForwardingFromEitherNetwork().apply();
            }
            return VNetPeeringResponseDto.builder()
                    .id(updatePeering.id())
                    .networkId(updatePeering.networkId())
                    .state(updatePeering.state())
                    .name(updatePeering.name())
                    .remoteNetwokName(updatePeering.getRemoteNetwork().name())
                    .allowForwardedTraffic(updatePeering.isTrafficForwardingFromRemoteNetworkAllowed())
                    .build();
        }catch (CloudException e) {
            logger.error("Exception : Update Vnet Peering:"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<String> getLocations(HttpServletRequest request) throws JSONException {
        try {
            List<String> locationNameList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            azure.getCurrentSubscription().listLocations().forEach(location -> {
            	if(location.geographyGroup()!=null && "Physical".equals(location.regionType().toString())&&
                        !location.region().name().equals("eastus2euap")&&
                        !location.region().name().equals("eastusstg")&&
                        !location.region().name().equals("jioindiacentral")&&
                        !location.region().name().equals("jioindiawest")&&
                        !location.region().name().equals("southcentralusstg")&&
                        !location.region().name().equals("centraluseuap")) {

            		locationNameList.add(location.region().name());
            	}
            });
            return locationNameList;
        } catch (CloudException e) {
            logger.error("Location Exception ; "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<VNetResponseDto> getVNetsByRegion(HttpServletRequest request,String resourceGroupName,String region) throws JSONException {
        List<VNetResponseDto> vNetResponseDtoList = new ArrayList<>();
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info(credentials.getSubscriptionId());

            List<Network> networkList = new ArrayList<>(
                    azure.networks().listByResourceGroup(resourceGroupName));

            networkList.forEach(network -> {
                // network Peering details
                boolean isVNetPeered = false;
                if(network.peerings().list().size() > 0){
                    isVNetPeered =true;
                    logger.info("is peered ::"+isVNetPeered);
                }
                if (region.equalsIgnoreCase(network.regionName())){
                    VNetResponseDto vNetResponseDto = VNetResponseDto
                            .builder()
                            .id(network.id())
                            .name(network.name())
                            .addressSpace(String.join("", network.addressSpaces()))
                            .location(network.regionName())
                            .isVNetPeered(isVNetPeered)
                            .tags(network.tags())
                            .build();
                    vNetResponseDtoList.add(vNetResponseDto);
                }else {
                    VNetResponseDto vNetResponseDto = VNetResponseDto
                            .builder()
                            .id(network.id())
                            .name(network.name())
                            .addressSpace(String.join("", network.addressSpaces()))
                            .location(network.regionName())
                            .isVNetPeered(isVNetPeered)
                            .tags(network.tags())
                            .build();
                    vNetResponseDtoList.add(vNetResponseDto);
                }

            });
        } catch (CloudException e) {
            logger.error("Error occured while :getVNetsByRegion::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return vNetResponseDtoList;
    }

    @Override
    public VNetResponseDto getSubnets(HttpServletRequest request,String vNetName,String resourceGroupName,String activeFlag,String nsgName) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            Network network = azure
                    .networks()
                    .listByResourceGroup(resourceGroupName)
                    .stream()
                    .filter(network1 -> network1.name().equalsIgnoreCase(vNetName))
                    .findFirst().orElseThrow(()->
                            new AzureAcltrRuntimeException(
                            "No Virtual netwok found with name " + vNetName,
                            null,
                            "No Virtual netwok found with name " + vNetName,
                            HttpStatus.INTERNAL_SERVER_ERROR));

            List<SubnetDto> subnetDtoList = new ArrayList<>();
            network.subnets().forEach((s, subnet) -> {
                logger.info(subnet.name() + "::" + subnet.addressPrefix());
                logger.info("NSG>>>>>> :: "+subnet.getNetworkSecurityGroup());
                SubnetDto subnetDto = new SubnetDto();
                subnetDto.setName(subnet.name());
                subnetDto.setAddressPrefix(subnet.addressPrefix());
                //subnetDtoList.add(subnetDto);
                if (activeFlag!=null && activeFlag.equalsIgnoreCase("Y") ) {
                    if (subnet.getNetworkSecurityGroup() == null) {
                        logger.info(subnet.name() + "::" + subnet.addressPrefix());
                        System.out.println(subnet.name() + "::" + subnet.addressPrefix());
                        subnetDtoList.add(subnetDto);

                    }
                }else if (activeFlag!=null && activeFlag.equalsIgnoreCase("N") ) {
                    if (subnet.getNetworkSecurityGroup() != null && subnet.getNetworkSecurityGroup().name().equalsIgnoreCase(nsgName)) {
                        logger.info(subnet.name() + "::" + subnet.addressPrefix());
                        subnetDtoList.add(subnetDto);

                    }
                }else {
                    subnetDtoList.add(subnetDto);
                }
            });

            return
                    VNetResponseDto
                            .builder()
                            .id(network.id())
                            .name(network.name())
                            .addressSpace(String.join("", network.addressSpaces()))
                            .location(network.regionName())
                            .subnetDtoList(subnetDtoList)
                            .build();


        } catch (CloudException e) {
            logger.error("Error occured while getting getSubnets:::::"+e.getMessage());
            e.getLocalizedMessage();
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private AzureCredentials applicationTokenCredentials(String userName) throws JSONException {
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
}
