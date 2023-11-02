package com.azureAccelerator.controller;

import com.azureAccelerator.dto.WebAppDto;
import com.azureAccelerator.service.AzureWebAppService;
import java.util.List;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AzureWebAppServiceController {

  private final AzureWebAppService azureWebAppService;

  public AzureWebAppServiceController(AzureWebAppService azureWebAppService) {
    this.azureWebAppService = azureWebAppService;
  }

  @GetMapping("getWebApps")
  public ResponseEntity<List<WebAppDto>> getAppServices(HttpServletRequest request,
      @RequestParam String resourceGroupName) throws JSONException {

    return new ResponseEntity<List<WebAppDto>>(
        azureWebAppService.getAppServices(request,resourceGroupName), HttpStatus.OK);
  }

  @GetMapping("restartWebApp")
  public ResponseEntity<String> restartWebApp(HttpServletRequest request,
      @RequestParam String webAppId) throws JSONException {

    azureWebAppService.restartWebApp(request,webAppId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
  @GetMapping("startWebApp")
  public ResponseEntity<String> startWebApp(HttpServletRequest request,
      @RequestParam String webAppId) throws JSONException {

    azureWebAppService.startWebApp(request,webAppId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("stopWebApp")
  public ResponseEntity<String> stopWebApp(HttpServletRequest request,
      @RequestParam String webAppId) throws JSONException {

    azureWebAppService.stopWebApp(request,webAppId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("browseWebApp")
  public ResponseEntity<List<String>> browseWebApp(HttpServletRequest request,
      @RequestParam String webAppId) throws JSONException {

    return new ResponseEntity<List<String>>(
        azureWebAppService.browseWebApp(request,webAppId), HttpStatus.OK);
  }

  @GetMapping("deleteWebApp")
  public ResponseEntity<String> deleteWebApp(HttpServletRequest request,
      @RequestParam String webAppId) throws JSONException {

    azureWebAppService.deleteWebApp(request,webAppId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
