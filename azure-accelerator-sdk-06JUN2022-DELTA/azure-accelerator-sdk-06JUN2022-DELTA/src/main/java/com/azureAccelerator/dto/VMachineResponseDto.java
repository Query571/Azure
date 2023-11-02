package com.azureAccelerator.dto;


import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.NetworkInterface;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
@ToString
public class VMachineResponseDto {

    private String id;
    private String name;
    private String resourceGroup;
    private String region;
    private String subnet;
    private String networkDetails;
    private VirtualMachineSizeTypes size;
    private String primaryPublicIPAddress;
    private String vNet;
    private String nsg;
    private Map<String,String> tags;
    private String others_details;
    private List<Map<String,String>> dataDisk;
    private String lun;
    private JSONObject data;
    private String  disk_cachingType;

}
