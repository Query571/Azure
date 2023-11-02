package com.azureAccelerator.service;

import com.azureAccelerator.dto.VMachineDto;
import com.azureAccelerator.dto.VMachineResponseDto;
import com.azureAccelerator.dto.VMsDto;
import com.azureAccelerator.entity.AzureInstanceTypes;
import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;

public interface VMachineService {

    List<VMachineResponseDto> createVM(HttpServletRequest request,List<VMachineDto> vMsDto, int vmCount) throws Exception;

    VMachineResponseDto createVM2(HttpServletRequest request,VMachineDto vMsDto) throws JSONException, IOException;

    Map<String, String> startVM(HttpServletRequest request,String myResourceGroup,String myVM) throws JSONException;

    Map<String, String> reStartVM(HttpServletRequest request,String myResourceGroup,String myVM) throws JSONException;

    Map<String, String> stopVM(HttpServletRequest request,String myResourceGroup, String myVM) throws JSONException;

    public Map<String, String> deleteVMs(HttpServletRequest request,List<VMachineDto> vMsDto) throws Exception;

    Map<String, String> deleteVM(HttpServletRequest request,String myResourceGroup,String myVM) throws Exception;

    List<VMachineResponseDto> listVMs(HttpServletRequest request,String resourceGroupName) throws JSONException;

    VMachineResponseDto getVM(HttpServletRequest request,String resourceGroupName,String myVm) throws JSONException;

    VMachineResponseDto reSizeVM(HttpServletRequest request,String myResourceGroup,String myVM,String size) throws JSONException;

    VMachineResponseDto addDisk(HttpServletRequest request,String diskNAme,String myResourceGroup,String myVM,String disk,int gb,int lun,String storageAccountTypes ) throws JSONException;

    String getNI(HttpServletRequest request,String myResourceGroup,String ni) throws Exception;

    //JSONObject getVM(String resourceGroups, String virtualMachines) throws IOException;

    //public List<String> getCatchingTypes();
    public List<String> getVMImagesList(HttpServletRequest request,String osType);

    JSONObject getRegions(HttpServletRequest request) throws IOException, JSONException;

    List<AzureInstanceTypes> recVMSizes(HttpServletRequest request);

    Map<String,String> removeDisk(HttpServletRequest request, String resourceGroups, String myVM, int lun) throws JSONException;





}
