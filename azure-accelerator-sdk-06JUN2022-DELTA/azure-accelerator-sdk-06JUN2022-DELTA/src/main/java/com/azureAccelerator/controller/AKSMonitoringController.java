package com.azureAccelerator.controller;

import com.azureAccelerator.dto.ActionGroupDto;
import com.azureAccelerator.dto.ActionGroupResponse;
import com.azureAccelerator.dto.AlertRuleDto;
import com.azureAccelerator.dto.AlertRuleResponse;
import com.azureAccelerator.service.AKSMonitoringService;
import java.util.List;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AKSMonitoringController {

  private final AKSMonitoringService aksMonitoringService;

  public AKSMonitoringController(
      AKSMonitoringService aksMonitoringService) {
    this.aksMonitoringService = aksMonitoringService;
  }

  @GetMapping("getActionGroups")
  public ResponseEntity<List<ActionGroupResponse>> getActionGroups(HttpServletRequest request,
      @RequestParam String resourceGroupName) throws JSONException {

    return new ResponseEntity<List<ActionGroupResponse>>(
        aksMonitoringService.actionGroups(request,resourceGroupName), HttpStatus.OK);
  }

  @PostMapping("createActionGroup")
  public ResponseEntity<ActionGroupResponse> createActionGroup(HttpServletRequest request,
      @RequestBody ActionGroupDto actionGroupDto) throws JSONException {

    return new ResponseEntity<ActionGroupResponse>(
        aksMonitoringService.createActionGroup(request,actionGroupDto), HttpStatus.OK);
  }

  @GetMapping("getAlertRules")
  public ResponseEntity<List<AlertRuleResponse>> getAlertRules(HttpServletRequest request,
      @RequestParam String aksId) throws JSONException {

    return new ResponseEntity<List<AlertRuleResponse>>(
        aksMonitoringService.getAlertRules(request,aksId), HttpStatus.OK);
  }

  @PostMapping("createAlertRule")
  public ResponseEntity<List<AlertRuleResponse>> createAlertRule(HttpServletRequest request,
      @RequestBody AlertRuleDto alertRuleDto) throws JSONException {

    return new ResponseEntity<List<AlertRuleResponse>>(
        aksMonitoringService.createAlertRule(request,alertRuleDto), HttpStatus.OK);
  }

}
