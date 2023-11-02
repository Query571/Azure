package com.azureAccelerator.service;

import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

public interface ARMTemplateService {

    Object getVnetARMTemplate(HttpServletRequest request, String resourceGroup, String vNet) throws JSONException;

    Object getKeyVaultARMTemplate(HttpServletRequest request,String resourceGroup,String keyVault) throws JSONException;

    Object getVMARMTemplate(HttpServletRequest request,String resourceGroup,String vm) throws JSONException;

    Object getNSGARMTemplate(HttpServletRequest request,String resourceGroup,String nsg) throws JSONException;

    Object getACRARMTemplate(HttpServletRequest request,String resourceGroup,String acr) throws JSONException;

}
