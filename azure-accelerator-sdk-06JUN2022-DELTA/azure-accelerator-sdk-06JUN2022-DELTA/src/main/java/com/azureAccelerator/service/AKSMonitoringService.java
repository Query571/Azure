package com.azureAccelerator.service;

import com.azureAccelerator.dto.ActionGroupDto;
import com.azureAccelerator.dto.ActionGroupResponse;
import com.azureAccelerator.dto.AlertRuleDto;
import com.azureAccelerator.dto.AlertRuleResponse;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AKSMonitoringService {

  List<AlertRuleResponse> createAlertRule(HttpServletRequest request, AlertRuleDto alertRuleDto) throws JSONException;

  ActionGroupResponse createActionGroup(HttpServletRequest request,ActionGroupDto actionGroupDto) throws JSONException;

  List<ActionGroupResponse> actionGroups(HttpServletRequest request,String resourceGroupName) throws JSONException;

  List<AlertRuleResponse> getAlertRules(HttpServletRequest request,String aksId) throws JSONException;
}
