package com.azureAccelerator.service;

import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IntegratingAKStoACRService {

    String integratingAKStoACR(HttpServletRequest request, String resourceGroupName, String aksName, String acr) throws JSONException;

    String disIntegratingAKStoACR(HttpServletRequest request,String resourceGroupName, String aksName, String acr);

    Map<String,String> importingDockerImages(HttpServletRequest request,String acrName, String imageName, String version) throws JSONException;

    Map<String,String> importingPrivateDockerhubImages(HttpServletRequest request,String acrName, String imageName, String version,String userName,String password) throws JSONException;

    Map<String,String> importingDockerImagesACRToACR(HttpServletRequest request,String acrResourceName, String destinationAcrName, String imageName, String version) throws JSONException;

    String deleteDockerImagesInAcr(HttpServletRequest request,String acrName, String imageName) throws JSONException;

    JSONObject checkNameAvailability(HttpServletRequest request,String acrName) throws JSONException;

    Map<String,String> enableKeystoreForAks(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException;

    Map<String,String> disableKeystoreForAks(HttpServletRequest request,String resourceGroupName,String aksName) throws JSONException;

    public JSONObject CheckingAzureKeyVaultSecretsProvider(HttpServletRequest request,String resourceGroupName,String aksName) throws JSONException;

    Object getImageTag( HttpServletRequest request,String acrName, String image) throws JSONException;

    String dashboardDeploy(HttpServletRequest request,String rg,String aksName) throws JSONException;

    List<String> listTags(HttpServletRequest request,String image) throws Exception;
}
