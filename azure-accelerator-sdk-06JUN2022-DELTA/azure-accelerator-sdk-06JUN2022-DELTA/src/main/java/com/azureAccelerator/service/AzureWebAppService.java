package com.azureAccelerator.service;

import com.azureAccelerator.dto.WebAppDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AzureWebAppService {

  List<WebAppDto> getAppServices(HttpServletRequest request, String resourceGroupName) throws JSONException;

  void restartWebApp(HttpServletRequest request,String webAppId) throws JSONException;

  void startWebApp(HttpServletRequest request,String webAppId) throws JSONException;

  void deleteWebApp(HttpServletRequest request,String webAppId) throws JSONException;

  void stopWebApp(HttpServletRequest request,String webAppId) throws JSONException;

  List<String> browseWebApp(HttpServletRequest request,String webAppId) throws JSONException;
}
