package com.azureAccelerator.controller;


import com.azureAccelerator.dto.NetworkSecurityGroupsDto;
import com.azureAccelerator.dto.NetworkSecurityGroupsResponseDto;
import com.azureAccelerator.service.NetworkSecurityGroupsService;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class NetworkSecurityGroupsController {

    private NetworkSecurityGroupsService networkSecurityGroupsService;

    public NetworkSecurityGroupsController(NetworkSecurityGroupsService networkSecurityGroupsService) {
        this.networkSecurityGroupsService = networkSecurityGroupsService;
    }


    @PostMapping("createNSG")
    public String createNSG(HttpServletRequest request, @RequestBody NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {

        return networkSecurityGroupsService.createNSG(request,networkSecurityGroupsDto);
    }

    @GetMapping("listAllNSG")
    public JSONObject listAllNSG(HttpServletRequest request,@RequestParam String resourceGroupName) throws IOException, JSONException {

        return networkSecurityGroupsService.listAllNSG(request,resourceGroupName);
    }

    @GetMapping("getNSG")
    public String getNSG(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String networkSecurityGroupName) throws IOException, JSONException {

        return networkSecurityGroupsService.getNSG(request,resourceGroupName,networkSecurityGroupName);
    }

    @GetMapping("getNSG1")
    public JSONObject getNSG1(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String networkSecurityGroupName) throws IOException, JSONException {

        return networkSecurityGroupsService.getNSG1(request,resourceGroupName,networkSecurityGroupName);
    }

    @DeleteMapping("deleteNSG")
    public Map<String,String> deleteNSG(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String networkSecurityGroupName) throws IOException, JSONException {

        return networkSecurityGroupsService.deleteNSG(request,resourceGroupName,networkSecurityGroupName);
    }


    @PutMapping("updateSubnet")
    ResponseEntity<Map<String,String>> updateSubnet(HttpServletRequest request,@RequestParam String resourceGroupName, @RequestParam String vNetName, @RequestParam String subnetName, @RequestParam String nsg) throws JSONException {

        return new ResponseEntity<Map<String,String>>(networkSecurityGroupsService.updateSubnet(request,resourceGroupName,vNetName,subnetName,nsg),HttpStatus.OK);
    }

    @PutMapping("disAssociation")
    ResponseEntity<Map<String,String>> disAssociationSubnet(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String vNetName,@RequestParam String subnetName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(networkSecurityGroupsService.disAssociationSubnet(request,resourceGroupName,vNetName,subnetName),HttpStatus.OK);
    }

    @GetMapping("getSubNet")
    public String getSubNet(HttpServletRequest request,@RequestParam String resourceGroupName, @RequestParam String vNetName, @RequestParam String subnetName ) throws IOException, JSONException {

        return networkSecurityGroupsService.getSubNet(request,resourceGroupName,vNetName,subnetName);
    }

    @PutMapping("addNewNsgRule")
    public ResponseEntity<NetworkSecurityGroupsResponseDto> addNSGRule(HttpServletRequest request,@RequestBody NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {
        return new ResponseEntity<>(networkSecurityGroupsService.addNSGRule(request,networkSecurityGroupsDto),HttpStatus.OK);
    }


    @DeleteMapping("removeRule")
    public ResponseEntity<Map<String, String>> removeRule(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String myNsg,@RequestParam String ruleName) throws Exception {
        return  new ResponseEntity<>(networkSecurityGroupsService.removeRule(request,resourceGroupName,myNsg,ruleName),HttpStatus.OK);
    }
    @PutMapping("updateNsgRule")
    public ResponseEntity<NetworkSecurityGroupsResponseDto> updateNsgRule(HttpServletRequest request,@RequestBody NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {
        return new ResponseEntity<>(networkSecurityGroupsService.updateNsgRule(request,networkSecurityGroupsDto),HttpStatus.OK);
    }

    @GetMapping("getAssociatedSubnets")
    public ResponseEntity<List<Map<String,String>>> getAssociatedSubnets(HttpServletRequest request, @RequestParam String resourceGroupName, @RequestParam String nsg) throws JSONException {
        return new ResponseEntity<>(networkSecurityGroupsService.getAssociatedSubnets(request,resourceGroupName,nsg),HttpStatus.OK);
    }


}
