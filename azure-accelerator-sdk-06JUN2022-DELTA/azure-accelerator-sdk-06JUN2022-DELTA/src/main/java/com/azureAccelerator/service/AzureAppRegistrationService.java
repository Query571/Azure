package com.azureAccelerator.service;

import com.azureAccelerator.dto.AppRegistrationResponse;
import com.azureAccelerator.dto.VNetDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AzureAppRegistrationService {
    AppRegistrationResponse createAppReg(HttpServletRequest request, String appName, String appUri) throws JSONException;

    List<AppRegistrationResponse> getAppReg(HttpServletRequest request) throws JSONException;

    String deleteAppReg(HttpServletRequest request,String appRegId) throws JSONException;
}
