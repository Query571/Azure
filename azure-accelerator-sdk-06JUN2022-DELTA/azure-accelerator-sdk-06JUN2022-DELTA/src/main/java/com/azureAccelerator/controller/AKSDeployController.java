package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.entity.AzureInstanceTypes;
import com.azureAccelerator.service.AKSDeployService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.SizeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
public class AKSDeployController {

private final AKSDeployService aksDeployService;

  public AKSDeployController(AKSDeployService aksDeployService) {
    this.aksDeployService = aksDeployService;
  }

  @GetMapping("getAKSClusters")
  public ResponseEntity<List<AKSDeployResponse>> getAKSClusters(HttpServletRequest request,@RequestParam String resourceGroupName) throws JSONException {
    String subscriptionId=request.getHeader("subscription_id");
    return new ResponseEntity<>(
            aksDeployService.getAKSClusters(request,resourceGroupName), HttpStatus.OK);
  }

  @GetMapping("getAzureVMSizes")
  public ResponseEntity<List<AzureInstanceTypes>> getAzureVMSizes(HttpServletRequest request) {
    String subscriptionId=request.getHeader("subscription_id");
    return new ResponseEntity<>(
            aksDeployService.getAzureVMSizes(request), HttpStatus.OK);
  }

  @PostMapping("deployAKS")
  public ResponseEntity<AKSDeployResponse> deployAKS(HttpServletRequest request,
      @RequestBody AKSDeployDto aksDeployDto) throws Exception {

    return new ResponseEntity<>(
            aksDeployService.deployAKS(request,aksDeployDto), HttpStatus.CREATED);
  }

  @DeleteMapping("deleteAKS")
  public ResponseEntity<String> deleteAKS(HttpServletRequest request,@RequestParam String  aksId) throws Exception {

    aksDeployService.deleteAKS(request,aksId);
    return new ResponseEntity<>(HttpStatus.OK);

  }

  @GetMapping("getRGLocation")
  public ResponseEntity<String> getRGLocation(HttpServletRequest request,
      @RequestParam String resourceGroupName) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.getRGLocation(request,resourceGroupName), HttpStatus.OK);
  }

  @PostMapping("deployAppOnAKS")
  public ResponseEntity<AzureCommonResponseDto> deployAppOnAKS(HttpServletRequest request,
      @RequestParam String resourceGroupName,
      @RequestParam String aksName,
      @RequestParam("file")  MultipartFile file) throws Exception {


    String fileExtensions = ".yml,.yaml,.YML, .YAML";
    String substring;

      String fileName = file.getOriginalFilename();
      int lastIndex = fileName.lastIndexOf('.');
      try {
        substring = fileName.substring(lastIndex, fileName.length());
      }catch (Exception e){
        throw new Exception("please select .yml or .yaml file");
      }
      if (fileExtensions.contains(substring.toLowerCase())) {
        if (file.getSize() <= 5000000) {


          return new ResponseEntity<AzureCommonResponseDto>(
                  aksDeployService.deployAppOnAKS(request,resourceGroupName, aksName, file), HttpStatus.CREATED);

        } else {
          throw new Exception("size limit is 5MB only...");
        }
      } else {
        throw new Exception("In-Valid File Type");
      }

  }
  @PostMapping("addNodePool")
  public ResponseEntity<AzureCommonResponseDto> addNodePool(HttpServletRequest request,
          @RequestBody AddNodePoolDto addNodePoolDto) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.addNodePool(request,addNodePoolDto), HttpStatus.CREATED);
  }

  @PostMapping("scaleNode")
  public ResponseEntity<AzureCommonResponseDto> scaleNode(HttpServletRequest request,
          @RequestBody ScaleAKSNodePool scaleAKSNodePool) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.scaleNode(request,scaleAKSNodePool), HttpStatus.CREATED);
  }

  @PostMapping("startAKS")
  public ResponseEntity<Map<String,String>> aksStart(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String aksName) throws JSONException, IOException {

    return new ResponseEntity<>(aksDeployService.aksStart(request,resourceGroupName,aksName),HttpStatus.OK);
  }

  @PostMapping("stopAKS")
  public ResponseEntity<Map<String,String>> aksStop(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String aksName) throws JSONException, IOException {

    return new ResponseEntity<>(aksDeployService.aksStop(request,resourceGroupName,aksName),HttpStatus.OK);
  }

  @GetMapping("getKubernetVersion")
  public ResponseEntity<List<String>> getKubernetVersion(HttpServletRequest request,
          @RequestParam String resourceGroupName,
          @RequestParam String aksName) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.getKubernetVersion(request,resourceGroupName,aksName), HttpStatus.OK);
  }

  @PostMapping("upgradeKubernetVersion")
  public ResponseEntity<AzureCommonResponseDto> upgradeKubernetVersion(HttpServletRequest request,
          @RequestBody UpgrKuberNetVerDto upgrKuberNetVerDto) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.upgradeKubernetVersion(request,upgrKuberNetVerDto), HttpStatus.OK);
  }

  @GetMapping("getKbrntDashboard")
  public ResponseEntity<AKSDashboardDto> getKbrntDashboard(HttpServletRequest request,
          @RequestParam String resourceGroupName,
          @RequestParam String aksName) throws JSONException {

    return new ResponseEntity<>(
            aksDeployService.getKbrntDashboard(request,resourceGroupName,aksName), HttpStatus.OK);
  }
}
