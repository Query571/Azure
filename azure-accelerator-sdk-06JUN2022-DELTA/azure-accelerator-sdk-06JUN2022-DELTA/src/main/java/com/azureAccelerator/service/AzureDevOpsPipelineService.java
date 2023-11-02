/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service;

import org.json.simple.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface AzureDevOpsPipelineService {

  Map<String,String> storeSecret(HttpServletRequest request,String token,String username);

  Map<String,String> getDevopsToken(HttpServletRequest request,String username);

  HashMap<String,String> devopsTokenCheck(HttpServletRequest request, String token)throws IOException;

    JSONObject getProjectList(HttpServletRequest request, String token, String  organization) throws IOException;

  HashMap<String,String> checkSpecificOrgToken(HttpServletRequest request, String token, String  organization) throws IOException;

  JSONObject getAllPipeline(HttpServletRequest request,String token, String organization, String projectName) throws IOException;

  JSONObject runPipeline(HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String branch) throws IOException;

  JSONObject runsListForPipeline(HttpServletRequest request,String token,String organization,String projectName,String pipelineId) throws IOException;

  JSONObject getAllOrganization(HttpServletRequest request,String pilineDto) throws IOException;


  JSONObject getAllBuildLogsList(HttpServletRequest request,String token,String organization,String projectName) throws IOException;

  JSONObject getAllLogsOfBuild(HttpServletRequest request,String token,String organization,String projectName,String buildId) throws IOException;

  HashMap<String,StringBuffer> getLogsById(HttpServletRequest request,String token,String organization,String projectName,String buildId,String logId) throws IOException;

  JSONObject getAllLogsList(HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String  runId) throws IOException;

  JSONObject getAllLogByLogId(HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String  runId,String  logId) throws IOException;

  JSONObject getAllReleaseLogsList(HttpServletRequest request,String token,String organization,String projectName) throws IOException;

  JSONObject getAllReleaseDefinition(HttpServletRequest request,String token,String organization,String projectName) throws IOException;

  JSONObject getAllReleaseByPipeline(HttpServletRequest request,String token,String organization,String projectName,String definitionId) throws IOException;

  JSONObject getReleaseLogsById(HttpServletRequest request,String token,String organization,String projectName,String releaseId) throws IOException;



  JSONObject getAllReleaseTasklog(HttpServletRequest request,String token,String organization,String projectName,String releaseId,String environmentID,String deployPhasesId) throws IOException;

  HashMap<String,StringBuffer> getReleaseLogById(HttpServletRequest request,String token,String organization,String projectName,String releaseId
          ,String environmentID,String deployPhasesId,String taskId) throws IOException;

  JSONObject releaseList(HttpServletRequest request,String token, String organization, String projectName)throws IOException;

  ResponseEntity<Resource> releasesGetLogs(HttpServletRequest request, HttpServletResponse response, String token, String organization, String projectName, String releaseId)throws IOException;

  JSONObject repositoriesList(HttpServletRequest request,String token, String organization, String projectName)throws IOException;

  JSONObject releaseDefinitionsList(HttpServletRequest request,String token, String organization, String projectName) throws IOException;

  HashMap<String,String> deletePipeline(HttpServletRequest request,String token, String organization, String projectName, String definitionId)throws IOException;

  JSONObject renamePipeline(HttpServletRequest request,String token, String organization, String projectName, String definitionId,String oldName,String newName
          ,String oldPath,String newPath)throws IOException;

  JSONObject  getStageOfRelease(HttpServletRequest request,String token, String organization, String projectName, String releaseId)throws IOException;

  JSONObject runPipelineStatus(HttpServletRequest request, String token, String organization, String projectName, String pipelineId, String runId)throws IOException;

  JSONObject deployeStageOfRelease(HttpServletRequest request, String token, String organization, String projectName, String releaseId, String stageId)throws IOException;

}
