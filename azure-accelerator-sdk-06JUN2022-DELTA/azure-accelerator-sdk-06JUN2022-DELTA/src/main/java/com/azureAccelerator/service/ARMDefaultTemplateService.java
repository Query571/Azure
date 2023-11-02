package com.azureAccelerator.service;

import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

public interface ARMDefaultTemplateService {

    Object defaultJsonTypeARM(HttpServletRequest request, String templateName, int count) throws Exception;
}
