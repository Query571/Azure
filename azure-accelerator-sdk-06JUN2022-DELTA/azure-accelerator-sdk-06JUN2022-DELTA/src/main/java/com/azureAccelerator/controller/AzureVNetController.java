package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.service.AzureVNetService;
import com.microsoft.azure.management.network.Network;

import java.util.List;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AzureVNetController {

  private final AzureVNetService azureVNetService;

  public AzureVNetController(AzureVNetService azureVNetService) {
    this.azureVNetService = azureVNetService;
  }

  @GetMapping("resourceGroups")
  public ResponseEntity<List<String>> resourceGroups(HttpServletRequest request) throws JSONException {

    return new ResponseEntity<>(
        azureVNetService.resourceGroups(request), HttpStatus.OK);
  }

  @PostMapping("createVNet")
  public ResponseEntity<VNetResponseDto> createVNet(HttpServletRequest request,
      @RequestBody VNetDto vNetDto) throws Exception {

    return new ResponseEntity<>(
        azureVNetService.createVNet(request,vNetDto), HttpStatus.CREATED);
  }

  @GetMapping("updateVNet")
  public ResponseEntity<Network> updateVNet(HttpServletRequest request) throws JSONException {

    return new ResponseEntity<>(
        azureVNetService.updateVNet(request), HttpStatus.OK);
  }

  @DeleteMapping("deleteVNet")
  public ResponseEntity<String> deleteVNet(HttpServletRequest request,@RequestParam String  vNetId) throws Exception {

    return new ResponseEntity<>(
        azureVNetService.deleteVNet(request,vNetId), HttpStatus.OK);
  }

  @DeleteMapping("deleteVNets")
  public ResponseEntity<String> deleteVNets(HttpServletRequest request,@RequestBody List<VNetDto> vNetDto) throws Exception {

    return new ResponseEntity<>(
            azureVNetService.deleteVNets(request,vNetDto), HttpStatus.OK);
  }

  @GetMapping("getVnets")
  public ResponseEntity<List<VNetResponseDto>> getVnets(HttpServletRequest request,
            @RequestParam String resourceGroupName,@RequestParam(required = false)
          String region) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.getVnets(request,resourceGroupName,region), HttpStatus.OK);
  }

  @PostMapping("createVNetPeering")
  public ResponseEntity<VNetPeeringResponseDto> createVNetPeering(HttpServletRequest request,
          @RequestBody VNetPeeringDto vNetPeeringDto) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.createVNetPeering(request,vNetPeeringDto), HttpStatus.CREATED);
  }

  @GetMapping("getVnetPeering")
  public ResponseEntity<List<VNetPeeringResponseDto>> getVnetPeering(HttpServletRequest request,
          @RequestParam String sourceVNetId) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.getVnetPeering(request,sourceVNetId), HttpStatus.OK);
  }

  @PutMapping("updateVnetPeering")
  public ResponseEntity<VNetPeeringResponseDto> updateVnetPeering(HttpServletRequest request,
          @RequestBody UpdateVnetPeeringDto updateVnetPeeringDto) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.updateVnetPeering(request,updateVnetPeeringDto), HttpStatus.OK);
  }

  @DeleteMapping("deleteVNetPeering")
  public ResponseEntity<String> deleteVNetPeering(HttpServletRequest request,
          @RequestParam String  sourceVNetId,
          @RequestParam String  peeringId) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.deleteVNetPeering(request,sourceVNetId,peeringId), HttpStatus.OK);
  }

  @GetMapping("getLocations")
  public ResponseEntity<List<String>> getLocations(HttpServletRequest request) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.getLocations(request), HttpStatus.OK);
  }

  @GetMapping("getSubnets")
  public ResponseEntity<VNetResponseDto> getSubnets(HttpServletRequest request,
          @RequestParam String vNetName,
          @RequestParam String resourceGroupName,@RequestParam(required = false) String activeFlag,@RequestParam(required = false) String nsgName) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.getSubnets(request,vNetName,resourceGroupName,activeFlag,nsgName), HttpStatus.OK);
  }

  @GetMapping("getVNetsByRegion")
  public ResponseEntity<List<VNetResponseDto>> getVNetsByRegion(HttpServletRequest request,
          @RequestParam String resourceGroupName,@RequestParam(required = false)
          String region) throws JSONException {

    return new ResponseEntity<>(
            azureVNetService.getVNetsByRegion(request,resourceGroupName,region), HttpStatus.OK);
  }

}
