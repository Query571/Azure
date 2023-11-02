package com.azureAccelerator.service.impl;

import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.entity.AzureInstanceTypes;
import com.azureAccelerator.repository.AzureInstanceTypesRepository;
import com.azureAccelerator.service.NetworkSecurityGroupsService;
import com.azureAccelerator.service.UserService;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.Network;
import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.VMachineDto;
import com.azureAccelerator.dto.VMachineResponseDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.VMachineService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;


@Service
public class VMachineServiceImpl implements VMachineService {

   private static final Logger logger = LogManager.getLogger(VMachineServiceImpl.class);


    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;

    private final NetworkSecurityGroupsService networkSecurityGroupsService;
    private final AzureInstanceTypesRepository azureInstanceTypesRepository;

    private static String sshPublicKey;
    private final UserService userService;


    public VMachineServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, NetworkSecurityGroupsService networkSecurityGroupsService, AzureInstanceTypesRepository azureInstanceTypesRepository, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;

        this.networkSecurityGroupsService = networkSecurityGroupsService;
        this.azureInstanceTypesRepository = azureInstanceTypesRepository;
        this.userService = userService;
    }


    @Override
    public List<VMachineResponseDto> createVM(HttpServletRequest request, List<VMachineDto> vMsDto, int vmCount) throws Exception {
        VirtualMachine virtualMachine = null;

        List<VMachineResponseDto> response = new ArrayList<>();
        logger.info("creating vm...");
        List<VirtualMachine.DefinitionStages.WithCreate> creatableVirtualMachines = new ArrayList<>();
        VirtualMachine.DefinitionStages.WithCreate creatableVirtualMachine;


        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            List<VirtualMachine> vms=azure.virtualMachines().listByResourceGroup(vMsDto.get(0).getResourceGroupName());
            for (VirtualMachine virtualMachine1:vms ) {
                for (VMachineDto vMachineDto1:vMsDto ) {
                    if (virtualMachine1.name().equals(vMachineDto1.getName())){
                        throw new Exception("Vm Name must be unique or given wrong details");
                    }
                }
            }

            for(int i = 0; i < vmCount; i++) {
                VMachineDto vMachineDto = vMsDto.get(i);
                logger.debug("Input parameters >>>> createVM :"+vMachineDto.toString());


                Network network = azure.networks().getById("/subscriptions/"+credentials.getSubscriptionId()+"/" +
                    "resourceGroups/"+vMachineDto.getResourceGroupName()+"/providers/Microsoft.Network/virtualNetworks/"+vMachineDto.getVnet());
                logger.debug("Network id"+network.id());

            String x=getAssociatedSubnetNSGType(request,vMachineDto.getResourceGroupName(),vMachineDto.getVnet(),vMachineDto.getSubnet());

            logger.debug("Type of Subnet >>>>>>"+x);

            logger.debug(">>>  /subscriptions/"+credentials.getSubscriptionId()+"/" +
                    "resourceGroups/"+vMachineDto.getResourceGroupName()+"/providers/Microsoft.Network/virtualNetworks/"+vMachineDto.getVnet());



            if (vMachineDto.getImage().equalsIgnoreCase("linux")) {

                logger.info("Image type is linux");

                    if (x.equalsIgnoreCase("public")) {

                        logger.debug("Starting Creation of Public IP  >>>>>");
                        PublicIPAddress public_ip = azure.publicIPAddresses().define(vMachineDto.getName() + "_PublicIP")
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withStaticIP().create();

                        logger.debug("Public VM  Creation  with Public IP>>>>" + public_ip.name());

                        creatableVirtualMachine = azure.virtualMachines().define(vMachineDto.getName())
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withExistingPrimaryNetwork(network)
                                .withSubnet(vMachineDto.getSubnet())
                                .withPrimaryPrivateIPAddressDynamic()
                                .withExistingPrimaryPublicIPAddress(public_ip)
                                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                                .withRootUsername(vMachineDto.getUserName())
                                .withSsh(getSSHKeyFromVm(request,vMachineDto.getResourceGroupName(), vMachineDto.getSshKey()))
                                .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                                .withTags(vMachineDto.getTags());
                        creatableVirtualMachines.add(creatableVirtualMachine);

                    } else {
                        logger.info("Private VM creation ");
                        creatableVirtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withExistingPrimaryNetwork(network)
                                .withSubnet(vMachineDto.getSubnet())
                                .withPrimaryPrivateIPAddressDynamic()
                                .withoutPrimaryPublicIPAddress()
                                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                                .withRootUsername(vMachineDto.getUserName())
                                .withSsh(getSSHKeyFromVm(request,vMachineDto.getResourceGroupName(), vMachineDto.getSshKey()))
                                .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                                .withTags(vMachineDto.getTags());
                        creatableVirtualMachines.add(creatableVirtualMachine);


                    }



            } else if (vMachineDto.getImage().equalsIgnoreCase("windows")) {
                    logger.info("Image type is windows");

                    if (x.equalsIgnoreCase("public")) {

                        logger.debug("Starting Creation of Public IP  >>>>>");
                        PublicIPAddress public_ip = azure.publicIPAddresses().define(vMachineDto.getName() + "_PublicIP")
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withStaticIP().create();

                        logger.debug("Public IP  Creation Done >>>>>" + public_ip);
                        creatableVirtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withExistingPrimaryNetwork(network)
                                .withSubnet(vMachineDto.getSubnet())
                                .withPrimaryPrivateIPAddressDynamic()
                                .withExistingPrimaryPublicIPAddress(public_ip)
                                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                                .withAdminUsername(vMachineDto.getUserName())
                                .withAdminPassword(vMachineDto.getPassword())
                                .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                                .withTags(vMachineDto.getTags());
                        creatableVirtualMachines.add(creatableVirtualMachine);


                    } else {


                        creatableVirtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                                .withRegion(vMachineDto.getRegion())
                                .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                                .withExistingPrimaryNetwork(network)
                                .withSubnet(vMachineDto.getSubnet())
                                .withPrimaryPrivateIPAddressDynamic()
                                .withoutPrimaryPublicIPAddress()
                                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                                .withAdminUsername(vMachineDto.getUserName())
                                .withAdminPassword(vMachineDto.getPassword())
                                .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                                .withTags(vMachineDto.getTags());
                        creatableVirtualMachines.add(creatableVirtualMachine);


                    }
                }

            }
            Collection<VirtualMachine> virtualMachines= azure.virtualMachines()
                    .create(creatableVirtualMachines.toArray(new VirtualMachine.DefinitionStages.WithCreate[vmCount])).values();

            List<String> subnetList=new ArrayList<>();
             for(int i = 0; i < vmCount; i++){
                VMachineDto vMachineDto = vMsDto.get(i);
                subnetList.add(vMachineDto.getSubnet());

            }

            for (VirtualMachine vm : virtualMachines) {

                Network network=vm.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
                for (String subnetName:subnetList){
                    for (Subnet subnet : network.subnets().values()) {
                        if (subnet.name().equals(subnetName)) {
                            if (subnet.getNetworkSecurityGroup()!=null) {
                                NetworkInterface nic2 = azure.networkInterfaces().getById(vm.primaryNetworkInterfaceId())
                                        .update().withExistingNetworkSecurityGroup(subnet.getNetworkSecurityGroup()).apply();
                            }
                        }
                    }
                }

                response.add( VMachineResponseDto
                        .builder()
                        .id(vm.id())
                        .name(vm.name())
                        .networkDetails(vm.getPrimaryNetworkInterface().primaryPrivateIP())
                        .size(vm.size())
                        .tags(vm.tags())
                        .build());
            }

        } catch (
                CloudException e) {
            logger.error("Exception: createVM : "+ e.body().message());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String str = null;
        VMachineResponseDto vmResponse;



        return response;



    }

    @Override
    public VMachineResponseDto createVM2(HttpServletRequest request,VMachineDto vMachineDto) throws JSONException, IOException {

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        VirtualMachine virtualMachine;
        Network network = azure.networks().getById("/subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+vMachineDto.getResourceGroupName()+"/providers/Microsoft.Network/virtualNetworks/"+vMachineDto.getVnet());


        String x=getAssociatedSubnetNSGType(request,vMachineDto.getResourceGroupName(),vMachineDto.getVnet(),vMachineDto.getSubnet());

        logger.debug("Type of Subnet >>>>>>"+x);

        logger.debug(">>>  /subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+vMachineDto.getResourceGroupName()+"/providers/Microsoft.Network/virtualNetworks/"+vMachineDto.getVnet());



        if (x.equalsIgnoreCase("public")) {

            logger.debug("Starting Creation of Public IP  >>>>>");
            PublicIPAddress public_ip = azure.publicIPAddresses().define(vMachineDto.getName() + "_PublicIP")
                    .withRegion(vMachineDto.getRegion())
                    .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                    .withStaticIP().create();

            logger.info("Public VM  Creation  with Public IP>>>>" + public_ip);

            virtualMachine = azure.virtualMachines().define(vMachineDto.getName())
                    .withRegion(vMachineDto.getRegion())
                    .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(vMachineDto.getSubnet())
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(public_ip)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                    .withRootUsername(vMachineDto.getUserName())
                    .withSsh(getSSHKeyFromVm(request,vMachineDto.getResourceGroupName(), vMachineDto.getSshKey()))
                    .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                    .withTags(vMachineDto.getTags()).create();
            //creatableVirtualMachines.add(creatableVirtualMachine);

        } else {
            logger.info("Private VM creation ");
            virtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                    .withRegion(vMachineDto.getRegion())
                    .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(vMachineDto.getSubnet())
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                    .withRootUsername(vMachineDto.getUserName())
                    .withSsh(getSSHKeyFromVm(request,vMachineDto.getResourceGroupName(), vMachineDto.getSshKey()))
                    .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                    .withTags(vMachineDto.getTags()).create();
            //creatableVirtualMachines.add(creatableVirtualMachine);
        }  if (vMachineDto.getImage().equalsIgnoreCase("windows")) {
            logger.info("Image type is windows");

            if (x.equalsIgnoreCase("public")) {

                logger.debug("Starting Creation of Public IP  >>>>>");
                PublicIPAddress public_ip = azure.publicIPAddresses().define(vMachineDto.getName() + "_PublicIP")
                        .withRegion(vMachineDto.getRegion())
                        .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                        .withStaticIP().create();

                logger.debug("Public IP  Creation Done >>>>>" + public_ip);
                virtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                        .withRegion(vMachineDto.getRegion())
                        .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                        .withExistingPrimaryNetwork(network)
                        .withSubnet(vMachineDto.getSubnet())
                        .withPrimaryPrivateIPAddressDynamic()
                        .withExistingPrimaryPublicIPAddress(public_ip)
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                        .withAdminUsername(vMachineDto.getUserName())
                        .withAdminPassword(vMachineDto.getPassword())
                        .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                        .withTags(vMachineDto.getTags()).create();
                //creatableVirtualMachines.add(creatableVirtualMachine);


            } else {


                virtualMachine  = azure.virtualMachines().define(vMachineDto.getName())
                        .withRegion(vMachineDto.getRegion())
                        .withExistingResourceGroup(vMachineDto.getResourceGroupName())
                        .withExistingPrimaryNetwork(network)
                        .withSubnet(vMachineDto.getSubnet())
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.valueOf(vMachineDto.getImageFlavour()))
                        .withAdminUsername(vMachineDto.getUserName())
                        .withAdminPassword(vMachineDto.getPassword())
                        .withSize(VirtualMachineSizeTypes.fromString(vMachineDto.getSize()))
                        .withTags(vMachineDto.getTags()).create();
                //creatableVirtualMachines.add(creatableVirtualMachine);


            }
        }




        return VMachineResponseDto
                .builder()
                .id(virtualMachine.id())
                .name(virtualMachine.name())
                .networkDetails(virtualMachine.getPrimaryNetworkInterface().primaryPrivateIP())
                .size(virtualMachine.size())
                .tags(virtualMachine.tags())
                .build();
    }

    @Override
    public Map<String, String> startVM(HttpServletRequest request,String myResourceGroup, String myVM) throws JSONException {

        VirtualMachine vm = null;
        Map<String,String> map= new HashMap();
        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            vm = azure.virtualMachines().getByResourceGroup(myResourceGroup, myVM);

            logger.info("Starting vm...");
            vm.start();
            logger.info("Started vm");

            map.put("status",vm.name()+" is started");

        } catch (CloudException e) {
            logger.error("Error occurred while startVM:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;

    }

    @Override
    public Map<String, String> reStartVM(HttpServletRequest request,String myResourceGroup, String myVM) throws JSONException {
        VirtualMachine vm = null;

        Map<String,String> map= new HashMap();
        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            vm = azure.virtualMachines().getByResourceGroup(myResourceGroup, myVM);

            logger.info("Re-Starting vm...");
            logger.debug("vm id :"+vm.id());
            vm.restart();
            logger.info("Re-Started vm");

            map.put("status",vm.name()+" is Re-Started");


        } catch (CloudException e) {
            logger.error("Error occurred while reStartVM:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    @Override
    public Map<String, String> stopVM(HttpServletRequest request,String myResourceGroup, String myVM) throws JSONException {

        VirtualMachine vm = null;

        Map<String,String> map= new HashMap();

        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            vm = azure.virtualMachines().getByResourceGroup(myResourceGroup, myVM);
            logger.info("Stopping vm...");
            logger.debug("vm id :"+vm.id());
            vm.deallocate();
            logger.info("Stopped vm");


            map.put("status",vm.name()+" is stopped");



        } catch (CloudException e) {
            logger.error("Error occurred while stopVM::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return map;
    }

    public Map<String, String> deleteVMs(HttpServletRequest request,List<VMachineDto> vMsDto) throws Exception {
        Map<String,String> map= new HashMap();
        try{

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            logger.info("deleting vm...");

            for (VMachineDto vMachineDto:vMsDto){

                VirtualMachine virtualMachine=azure.virtualMachines().getByResourceGroup(vMachineDto.getResourceGroupName(),vMachineDto.getName());
                Collection<VirtualMachineDataDisk> disksListId=virtualMachine.dataDisks().values();
                logger.debug("disksListId ::::"+disksListId);
                logger.debug("virtualMachine.osDiskId() ::::"+virtualMachine.osDiskId());
                //String publicIp = virtualMachine.getPrimaryPublicIPAddress().id();
                String osDisk=virtualMachine.osDiskId();

                String networkInterface=getNI(request,vMachineDto.getResourceGroupName(),vMachineDto.getName());
                logger.info("networkInterface>>>>" + networkInterface);
                String publicIp2=getPublicIp(request,vMachineDto.getResourceGroupName(),vMachineDto.getName());
                logger.info("public Ip id>>>>" + publicIp2);

                if(networkInterface!=null) {

                    logger.info("networkInterface>>>>" + networkInterface);
                    azure.virtualMachines().deleteByResourceGroup(vMachineDto.getResourceGroupName(),vMachineDto.getName());
                    logger.info("VM is deleted");
                    azure.disks().deleteById(virtualMachine.osDiskId());
                    if (virtualMachine.dataDisks().values()!=null) {
                        for (VirtualMachineDataDisk dataDisk : disksListId) {
                            logger.debug("dataDisk : "+dataDisk+" is deleting");
                            azure.disks().deleteById(dataDisk.id());
                            logger.debug("dataDisk is deleted");
                        }
                    }
                    azure.disks().deleteById(virtualMachine.osDiskId());

                    azure.networkInterfaces().deleteByResourceGroup(vMachineDto.getResourceGroupName(), networkInterface);
                    logger.info("networkInterface is deleted");

                    if (publicIp2 == null) {
                        logger.info("No public IP");
                    } else {

                        azure.publicIPAddresses().deleteByResourceGroup(vMachineDto.getResourceGroupName(), publicIp2);
                        logger.debug("publicIPAddresses is deleted");
                    }


                }else {
                    azure.virtualMachines().deleteByResourceGroup(vMachineDto.getResourceGroupName(),vMachineDto.getName());
                    if (virtualMachine.dataDisks().values()!=null) {
                        for (VirtualMachineDataDisk dataDisk : disksListId) {

                            azure.disks().deleteById(dataDisk.id());
                        }
                    }
                    if (osDisk!=null) {
                        azure.disks().deleteById(virtualMachine.osDiskId());
                    }
                    logger.info("VM >>>> is deleted");
                }


                map.put("status","vms are is deleted");
                logger.info(map);
            }

        } catch (CloudException e) {
            logger.error("Error occurred while deleteVM:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return map;
    }

    @Override
    public Map<String, String> deleteVM(HttpServletRequest request,String myResourceGroup, String myVM) throws Exception {

        Map<String,String> map= new HashMap();

        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            logger.info("deleting vm...");

            VirtualMachine virtualMachine=azure.virtualMachines().getByResourceGroup(myResourceGroup,myVM);
            Collection<VirtualMachineDataDisk> disksListId=virtualMachine.dataDisks().values();
            logger.debug("disksListId ::::"+disksListId);
            logger.debug("virtualMachine.osDiskId() ::::"+virtualMachine.osDiskId());
            //String publicIp = virtualMachine.getPrimaryPublicIPAddress().id();
            String osDisk=virtualMachine.osDiskId();

            String networkInterface=getNI(request,myResourceGroup,myVM);
            logger.info("networkInterface>>>>" + networkInterface);
            String publicIp2=getPublicIp(request,myResourceGroup,myVM);
            logger.info("public Ip id>>>>" + publicIp2);

            if(networkInterface!=null) {

                logger.info("networkInterface>>>>" + networkInterface);
                azure.virtualMachines().deleteByResourceGroup(myResourceGroup, myVM);
                logger.info("VM is deleted");
                azure.disks().deleteById(virtualMachine.osDiskId());
                if (virtualMachine.dataDisks().values()!=null) {
                    for (VirtualMachineDataDisk dataDisk : disksListId) {
                        logger.debug("dataDisk : "+dataDisk+" is deleting");
                        azure.disks().deleteById(dataDisk.id());
                        logger.debug("dataDisk is deleted");
                    }
                }
                azure.disks().deleteById(virtualMachine.osDiskId());

                azure.networkInterfaces().deleteByResourceGroup(myResourceGroup, networkInterface);
                logger.info("networkInterface is deleted");

                    if (publicIp2 == null) {
                        logger.info("No public IP");
                    } else {

                        azure.publicIPAddresses().deleteByResourceGroup(myResourceGroup, publicIp2);
                        logger.debug("publicIPAddresses is deleted");
                    }


            }else {
                azure.virtualMachines().deleteByResourceGroup(myResourceGroup, myVM);
                if (virtualMachine.dataDisks().values()!=null) {
                    for (VirtualMachineDataDisk dataDisk : disksListId) {

                        azure.disks().deleteById(dataDisk.id());
                    }
                }
                if (osDisk!=null) {
                    azure.disks().deleteById(virtualMachine.osDiskId());
                }
                logger.info("VM >>>> is deleted");
            }


            map.put("status",myVM+" is deleted");
            logger.info(map);

        } catch (CloudException e) {
            logger.error("Error occurred while deleteVM:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return map;
    }



    @Override
    public List<VMachineResponseDto> listVMs(HttpServletRequest request,String resourceGroupName) throws JSONException {
        List<VMachineResponseDto> vMachineResponseDtoList = new ArrayList<>();
        logger.info("getting all vms... ");
        try {

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());


        List<VirtualMachine> vmList;

        if(resourceGroupName != null && resourceGroupName.length() > 0)
        {
            vmList= new ArrayList(
                azure.virtualMachines().listByResourceGroup(resourceGroupName));
        }else
        {
            vmList = new ArrayList(
                    azure.virtualMachines().list());
        }



            //String finalAzureToken = azureToken;
            vmList.forEach(vm->{

           // String dataDisk = "";

            /*org.json.simple.JSONObject health = null;
            try {
                health=getVM(vm.resourceGroupName(),vm.name(), finalAzureToken);


            } catch (IOException e) {
                logger.error("Error occured while  getting listVMs:::::"+e.getMessage());
            }*/

                //List<String> distList2=new ArrayList<>();

            /*for (DiskInstanceView disk : vm.instanceView().disks()) {

                if (dataDisk.length() > 1)
                {
                    dataDisk = dataDisk + "#" +disk.name();
                    diskList.add(disk.name());
                }
                else
                {
                    dataDisk = disk.name();
                    diskList.add(disk.name());

                }


            }*/

                List<Map<String, String>> disks=new ArrayList<>();
                Collection<VirtualMachineDataDisk> diskList= vm.dataDisks().values();
                for(VirtualMachineDataDisk dataDisk:diskList){
                    Map<String,String> map=new HashMap<>();
                            map.put("lun",dataDisk.lun()+"");
                            map.put("name",dataDisk.name());
                            disks.add(map);
                }

//               vm.dataDisks().values().stream().forEach(
//                       d-> diskList.add(d.lun()+" = "+d.name())
//
//               );
                String publicIPAddress=null;
                PublicIPAddress pia=vm.getPrimaryPublicIPAddress();

                if (null!=pia){
                    publicIPAddress =pia.ipAddress();

                }
                String networkDetails=vm.getPrimaryNetworkInterface().primaryPrivateIP();
                logger.debug(vm.id());

                Network network=vm.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
                String nsg = null;
                for (Subnet subnetName:network.subnets().values()){
                    if(vm.getPrimaryNetworkInterface().primaryIPConfiguration().subnetName().equals(subnetName.name())){

                        if(subnetName.getNetworkSecurityGroup()==null) {
                            nsg=null;
                        }else {
                           nsg = subnetName.getNetworkSecurityGroup().name();
                        }
                    }
                }
                vMachineResponseDtoList.add(VMachineResponseDto
                        .builder()
                        .id(vm.id())
                        .name(vm.name())
                        .resourceGroup(vm.resourceGroupName())
                        .region(vm.regionName())
                        .networkDetails(networkDetails)
                        .size(vm.size())
                        .primaryPublicIPAddress(publicIPAddress)
                        .vNet(vm.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork().name())
                        .subnet(vm.getPrimaryNetworkInterface().primaryIPConfiguration().subnetName())
                        .nsg(nsg)
                        .tags(vm.tags())
                        .others_details(vm.osType() + " # " + vm.powerState() + " # " + vm.osDiskSize())
                        .dataDisk(disks)
                        //  .data(health)
                        .build()
                );


            });
        logger.info("getting vms are ended");
        } catch (CloudException e) {
            logger.error("Error occurred while  getting listVMs:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                e.body().message(),
                null,
                e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return vMachineResponseDtoList;
    }

    @Override
    public VMachineResponseDto getVM(HttpServletRequest request, String resourceGroupName, String myVm) throws JSONException {

        VirtualMachine virtualMachine;
        String nsg = null;
        logger.info("getting  vm... ");
        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
           virtualMachine= azure.virtualMachines().getByResourceGroup(resourceGroupName,myVm);
            String networkDetails=virtualMachine.getPrimaryNetworkInterface().primaryPrivateIP();
            logger.debug(virtualMachine.id());

            Network network=virtualMachine.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
            for (Subnet subnetName:network.subnets().values()){
                if(virtualMachine.getPrimaryNetworkInterface().primaryIPConfiguration().subnetName().equals(subnetName.name())){

                    if(subnetName.getNetworkSecurityGroup()==null) {
                        nsg=null;
                    }else {
                        nsg = subnetName.getNetworkSecurityGroup().name();
                    }
                }
            }
        }catch (CloudException e) {
            logger.error("Error occurred while getVm:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


            return VMachineResponseDto.builder()
                    .id(virtualMachine.id())
                    .name(virtualMachine.name())
                    .resourceGroup(virtualMachine.resourceGroupName())
                    .region(virtualMachine.regionName())
                    .vNet(virtualMachine.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork().name())
                    .subnet(virtualMachine.getPrimaryNetworkInterface().primaryIPConfiguration().subnetName())
                    .nsg(nsg)
                    .build();
    }

    @Override
    public VMachineResponseDto reSizeVM(HttpServletRequest request,String myResourceGroup, String myVM, String size) throws JSONException {

        try {

                AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
                Azure azure = Azure.configure()
                        .authenticate(credentials.getApplicationTokenCredentials())
                        .withSubscription(credentials.getSubscriptionId());
            logger.info(credentials.getSubscriptionId());
                logger.info("Resizing vm...");
                VirtualMachine   vm = azure.virtualMachines().getByResourceGroup(myResourceGroup, myVM);
            logger.debug("vm id :"+vm.id());
                vm.update()
                    .withSize(size)
                    .apply();
                logger.info("Size will Updated");

                return VMachineResponseDto
                        .builder()
                        .id(vm.id())
                        .name(vm.name())
                        .networkDetails(vm.getPrimaryNetworkInterface().primaryPrivateIP())
                        .size(vm.size())
                        .tags(vm.tags())
                        .build();
        } catch (CloudException e) {
            logger.error("Error occurred while getting reSizeVM:::::"+e.getMessage());
                throw new AzureAcltrRuntimeException(
                         e.body().message(),
                         null,
                         e.body().message(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public VMachineResponseDto addDisk(HttpServletRequest request,String diskNAme,String myResourceGroup, String myVM, String disk_cachingType,int gb,int lun,String storageAccountTypes ) throws JSONException {

        try {
                AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
                Azure azure = Azure.configure()
                     .authenticate(credentials.getApplicationTokenCredentials())
                     .withSubscription(credentials.getSubscriptionId());

                logger.info("Adding Disk....");

                VirtualMachine   vm = azure.virtualMachines().getByResourceGroup(myResourceGroup, myVM);
           /* Disk disk=azure.disks().define("")
                    .withRegion(vm.regionName())
                    .withExistingResourceGroup(vm.resourceGroupName())
                    .withData()
                    .withSizeInGB(1).create();*/
            Creatable<Disk> dataDiskCreatable1 = azure.disks().define(diskNAme).withRegion(vm.region()).withExistingResourceGroup(vm.resourceGroupName()).withData()
                    .withSizeInGB(gb);
            vm.update().withDataDiskDefaultStorageAccountType(StorageAccountTypes.fromString(storageAccountTypes)).withNewDataDisk(dataDiskCreatable1,lun,CachingTypes.valueOf(disk_cachingType))
                    //.withNewDataDisk(gb, lun, CachingTypes.valueOf(disk_cachingType),StorageAccountTypes.fromString(storageAccountTypes))
                    .apply();

                                 /*vm.update()
                                 .withNewDataDisk(gb, lun, CachingTypes.valueOf(disk_cachingType),StorageAccountTypes.fromString(storageAccountTypes))
                                 .apply();*/
            logger.debug("vm id :"+vm.id());
            logger.info("new Disk added....");

                return VMachineResponseDto
                .builder()
                .id(vm.id())
                .name(vm.name())
                .networkDetails(vm.getPrimaryNetworkInterface().primaryPrivateIP())
                .disk_cachingType(vm.osDiskCachingType().toString())
                .build();
        } catch (CloudException e) {
            logger.error("Error occured while getting addDisk:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getNI(HttpServletRequest request,String myResourceGroup, String name) throws Exception {

        NetworkInterface networkInterface = null;
        String name1 = null;
        try{

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
             Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
                String s1="/subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+myResourceGroup+"/providers/Microsoft.Compute/virtualMachines/"+name;
                logger.info(s1);

                networkInterface = azure.virtualMachines().getById(s1).getPrimaryNetworkInterface();


         if(networkInterface!=null)
         {

             name1=networkInterface.name();
         }



        } catch (CloudException e) {
            logger.error("Error occurred while getting getNI::::"+e.getMessage());
        throw new AzureAcltrRuntimeException(
                e.body().message(),
                null,
                e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }

       logger.info("NetworkInterface >>"+networkInterface.name());

        return name1;
    }
    private String getPublicIp(HttpServletRequest request,String myResourceGroup, String name) throws Exception {
        PublicIPAddress publicIPAddress = null;
        String name1 = null;
        try{

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            String s1="/subscriptions/"+credentials.getSubscriptionId()+"/" +
                    "resourceGroups/"+myResourceGroup+"/providers/Microsoft.Compute/virtualMachines/"+name;

                publicIPAddress = azure.virtualMachines().getById(s1).getPrimaryPublicIPAddress();


            if(publicIPAddress!=null)
            {

                name1=publicIPAddress.name();
                logger.info("publicIPAddress.name() >>"+publicIPAddress.name());
            }



        } catch (CloudException e) {
            logger.error("Error occurred while getting getNI::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }



        return name1;
    }

    @Override
    public List<String> getVMImagesList(HttpServletRequest request,String osType) {

        List<String> imageList=new ArrayList<>();
        logger.info("getting all favours of images");
        switch (osType.toLowerCase()) {
            case "windows" :
            KnownWindowsVirtualMachineImage knownWindowsVirtualMachineImages[] = KnownWindowsVirtualMachineImage.values();

            for (KnownWindowsVirtualMachineImage knownWindowsVirtualMachineImage1 : knownWindowsVirtualMachineImages) {
                logger.debug("images >>>>" + knownWindowsVirtualMachineImage1);
                imageList.add(knownWindowsVirtualMachineImage1.name());
            }
            break;

            case "linux" :
                KnownLinuxVirtualMachineImage knownLinuxVirtualMachineImages[] = KnownLinuxVirtualMachineImage.values();

                for (KnownLinuxVirtualMachineImage knownLinuxVirtualMachineImage1 : knownLinuxVirtualMachineImages) {
                    logger.debug("images >>>>" + knownLinuxVirtualMachineImage1);
                    imageList.add(knownLinuxVirtualMachineImage1.name());
                }
                break;

            default:imageList.add("InValid");
        }
        logger.debug(imageList);
        logger.info("image list ended");
        return imageList;
    }

    @Override
    public org.json.simple.JSONObject getRegions(HttpServletRequest request) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        org.json.simple.JSONObject json = null;


        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url =new URL("https://management.azure.com/" +
                "subscriptions/"+credentials.getSubscriptionId()+"/locations?api-version=2020-01-01");

        GetToken getToken = new GetToken(vaultService, applicationProperties, userService);
        logger.info("url : " + url.toString());

        logger.info("getToken : " + getToken.gettingToken(request.getHeader("userName")));


        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));


        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));


            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);

            }
            /*Object obj = JSONValue.parse(response.toString());
            json = (org.json.simple.JSONObject) obj;
            Object obj2= json.get("value");
            org.json.simple.JSONObject jsonObject2=(org.json.simple.JSONObject) obj2;
            Object obj3=jsonObject2.get();*/

            in1.close();
        } else{
            logger.debug("GET request not worked");
        }

        conn.disconnect();

        return json;
    }

    @Override
    public List<AzureInstanceTypes> recVMSizes(HttpServletRequest request) {
        List<String> vmSizes = new ArrayList<>();
        try {

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        /*Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        azure.virtualMachines().sizes().listByRegion(region).forEach(
                d-> vmSizes.add(d.name())
        );*/
            vmSizes.add("Standard_DS1_v2");
            vmSizes.add("Standard_D2s_v3");
            vmSizes.add("Standard_D2as_v4");
            vmSizes.add("Standard_B2s");
            vmSizes.add("Standard_B1s");
            vmSizes.add("Standard_B2ms");
            vmSizes.add("Standard_DS2_v2");
            vmSizes.add("Standard_B4ms");
            vmSizes.add("Standard_D4s_v3");
            //vmSizes.add("Standard_Ds3_v3");
            vmSizes.add("Standard_D8s_v3");
            //  vmSizes.add("Standard_B1ls");
            //vmSizes.add("Standard_Ds2_v2");
            //vmSizes.add("Standard_B4ms");
            return azureInstanceTypesRepository.findByVmSizeIn(vmSizes);

        }
        catch (Exception e){
            logger.error("Error occured while getting recVMSizes:::::"+e.getMessage());

        }
        //return azureInstanceTypesRepository;
        return null;
    }

    @Override
    public Map<String, String> removeDisk(HttpServletRequest request,String resourceGroups,String myVM,int lun) throws JSONException {

        Map<String,String> map=new HashMap();

      try{

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        VirtualMachine virtualMachine=azure.virtualMachines().getByResourceGroup(resourceGroups,myVM);

        virtualMachine.update().withoutDataDisk(lun).apply();

        map.put("Status","data disk is removed successfully");
        logger.info(map);
     } catch (CloudException e) {
          logger.error("Error occurred while removeDisk::::::"+e.getMessage());
        throw new AzureAcltrRuntimeException(
                e.body().message(),
                null,
                e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
        return map;
    }

    public org.json.simple.JSONObject getVM(HttpServletRequest request,String resourceGroups, String virtualMachines , String azure_token) throws IOException, JSONException {

        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        org.json.simple.JSONObject responseDetailsJson = new org.json.simple.JSONObject();


        JSONArray ja = null;
        JSONArray ja1 = null;
        JSONObject json1 = null;


        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url = new URL("https://management.azure.com/" +
                "subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+resourceGroups+"/providers/Microsoft.Compute/" +
                "virtualMachines/"+virtualMachines+"/" +
                "providers/microsoft.insights/metrics?api-version=2018-01-01&metricnames=Percentage%20CPU");


        logger.info("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + azure_token);


        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));

            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);
            }

            try {
                Object obj = JSONValue.parse(response.toString());
                org.json.simple.JSONObject json = (org.json.simple.JSONObject) obj;
                ja = (JSONArray) json.get("value");

                org.json.JSONArray jsonArray1 = new org.json.JSONArray(ja.toString());
                Iterator<String> keys = null;

                for (int i = 0; i < jsonArray1.length(); i++) {
                    json1 = jsonArray1.getJSONObject(i);
                    keys = json1.keys();
                }

                    while (keys.hasNext()) {
                        String key = keys.next();
                        if(key.equals("timeseries")){
                            responseDetailsJson.put("timeSeries",JSONValue.parse(json1.get(key).toString()));

                        }
                    }
            }catch (Exception e) {
                logger.debug("Exception"+e);
            }
            in1.close();
        } else {
            logger.debug("GET request not worked");
        }
        conn.disconnect();

        return responseDetailsJson;
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
    private String getAssociatedSubnetNSGType(HttpServletRequest request,String resourceGroupName, String virtualNetworkName,String subnetName) throws JSONException, IOException {

        org.json.simple.JSONObject json = null;

        String str= networkSecurityGroupsService.getSubNet(request,resourceGroupName,virtualNetworkName,subnetName);
        Object obj = JSONValue.parse(str);
        json = (org.json.simple.JSONObject) obj;
        Object obj1=json.get("networkSecurityGroupType");
        logger.debug("networkSecurityGroupType >>>>"+json.get("networkSecurityGroupType"));

        if(obj1!=null) {
            return obj1.toString();
        }else{
            return "private";
        }
    }

    public static String sshPublicKey() {
        if (sshPublicKey == null) {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair pair = keyGen.generateKeyPair();
                PublicKey publicKey = pair.getPublic();

                RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
                ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(byteOs);
                dos.writeInt("ssh-rsa".getBytes(StandardCharsets.US_ASCII).length);
                dos.write("ssh-rsa".getBytes(StandardCharsets.US_ASCII));
                dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
                dos.write(rsaPublicKey.getPublicExponent().toByteArray());
                dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
                dos.write(rsaPublicKey.getModulus().toByteArray());
                String publicKeyEncoded = new String(Base64.getEncoder().encode(byteOs.toByteArray()), StandardCharsets.US_ASCII);
                sshPublicKey = "ssh-rsa " + publicKeyEncoded;
            } catch (IOException | NoSuchAlgorithmException e) {
                logger.error("Error occurred while getting sshPublicKey:::::"+e.getMessage());
            }
        }
        return sshPublicKey;
    }

    private String getSSHKeyFromVm(HttpServletRequest request,String resourceGroupName, String sshPublicKeyName) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        String sshKeys = "";


        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

        logger.info("vm_vaultService >> " + vaultService);
        logger.info("vm_applicationProperties >> " + applicationProperties);
        logger.info("credentials.getSubscriptionId()" + credentials.getSubscriptionId());

        URL url = new URL("https://management.azure.com" +
                "/subscriptions/" + credentials.getSubscriptionId() +
                "/resourceGroups/" + resourceGroupName + "/providers/Microsoft.Compute" +
                "/sshPublicKeys/" + sshPublicKeyName + "?api-version=2021-11-01");

        GetToken getToken = new GetToken(vaultService, applicationProperties, userService);


        logger.debug("url : " + url.toString());



        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));




        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));


            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);

            }

            in1.close();
        } else {
            logger.debug("GET request not worked");

        }

        conn.disconnect();
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(response.toString());
            sshKeys = jsonObject.getJSONObject("properties").getString("publicKey");
        } catch (Exception e) {
            logger.error("Exception :" + e.getMessage());
        }

        return sshKeys;

    }

}
