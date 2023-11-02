package com.azureAccelerator.controller;

import com.azureAccelerator.dto.DevOpsPipelineDto;
import com.azureAccelerator.service.AzureDevOpsPipelineService;
import org.json.simple.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AzureDevOpsPipelineController {
    private final AzureDevOpsPipelineService azureDevOpsPipelineService;

    public AzureDevOpsPipelineController(AzureDevOpsPipelineService azureDevOpsPipelineService) {
        this.azureDevOpsPipelineService= azureDevOpsPipelineService;
    }



    @PostMapping("storeDevopsToken")
    public ResponseEntity<Map<String,String>> storeSecret(HttpServletRequest request,@RequestParam String token,
            @RequestParam String username) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.storeSecret(request, token,username), HttpStatus.OK);
    }

    @GetMapping("getDevopsToken")
    public ResponseEntity<Map<String,String>> getDevopsToken(HttpServletRequest request,@RequestParam String username) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.getDevopsToken(request,username), HttpStatus.OK);
    }

    @GetMapping("devopsTokenCheck")
    public ResponseEntity<HashMap<String,String>> devopsTokenCheck(@RequestParam String token,HttpServletRequest request
                                                       ) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.devopsTokenCheck(request,token), HttpStatus.OK);
    }


    @GetMapping("getAllOrganization")
    public ResponseEntity<JSONObject> getAllOrganization(HttpServletRequest request,@RequestParam String token) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllOrganization(request,token), HttpStatus.OK);
    }


    @GetMapping("getAllProject")
    public ResponseEntity<JSONObject> getProjectList(HttpServletRequest request,@RequestParam String token,@RequestParam String organization) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getProjectList(request,token, organization), HttpStatus.OK);
    }

    @GetMapping("checkSpecificOrgToken")
    public ResponseEntity<Map<String,String>> checkSpecificOrgToken(HttpServletRequest request,@RequestParam String token,@RequestParam String organization) throws IOException {

        HashMap<String,String>resp=azureDevOpsPipelineService.checkSpecificOrgToken(request,token, organization);
        return new ResponseEntity<>(resp,Integer.parseInt(resp.get("code"))==200?HttpStatus.OK:HttpStatus.NOT_FOUND);
    }

    //Get a list of pipelines.
    @GetMapping("getAllPipeline")
    public ResponseEntity<JSONObject> getAllPipeline(HttpServletRequest request,@RequestParam String token,
                                                     @RequestParam String organization,
                                                     @RequestParam String projectName) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllPipeline(request,token,organization,projectName), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getAllBuildLogsList")
    public ResponseEntity<JSONObject> getAllBuildLogsList(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                          @RequestParam String projectName) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllBuildLogsList(request,token,organization,projectName), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getAllLogsOfBuild")
    public ResponseEntity<JSONObject> getAllLogsOfBuild(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                        @RequestParam String projectName,@RequestParam String buildId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllLogsOfBuild(request,token,organization,projectName,buildId), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getLogsById")
    public ResponseEntity<HashMap<String,StringBuffer>> getLogsById(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                        @RequestParam String projectName,@RequestParam String buildId,
                                                        @RequestParam String logId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getLogsById(request,token,organization,projectName,buildId,logId), HttpStatus.OK);
    }


    //Get a list of logs from a pipeline run build.
    @GetMapping("getAllReleaseDefinition")
    public ResponseEntity<JSONObject> getAllReleaseDefinition(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                            @RequestParam String projectName) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllReleaseDefinition(request,token,organization,projectName), HttpStatus.OK);
    }


    //Get a list of logs from a pipeline run build.
    @GetMapping("getAllReleaseLogsList")
    public ResponseEntity<JSONObject> getAllReleaseLogsList(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                            @RequestParam String projectName) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllReleaseLogsList(request,token,organization,projectName), HttpStatus.OK);
    }

    @GetMapping("getAllReleaseByPipeline")
    public ResponseEntity<JSONObject> getAllReleaseByPipeline(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                            @RequestParam String projectName,@RequestParam String definitionId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllReleaseByPipeline(request,token,organization,projectName,definitionId), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getReleaseLogListById")
    public ResponseEntity<JSONObject> getReleaseLogsById(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                            @RequestParam String projectName,@RequestParam String releaseId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getReleaseLogsById(request,token,organization,projectName,releaseId), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getReleaseLogTaskById")
    public ResponseEntity<HashMap<String,StringBuffer>> getReleaseLogById(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                                    @RequestParam String projectName,@RequestParam String releaseId,
                                                                    @RequestParam String environmentID,@RequestParam String deployPhasesId,@RequestParam String taskId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getReleaseLogById(request,token,organization,projectName,releaseId
                        ,environmentID,deployPhasesId,taskId), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run build.
    @GetMapping("getAllReleaseTasklog")
    public ResponseEntity<JSONObject> getAllReleaseTasklog(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                           @RequestParam String projectName,@RequestParam String releaseId,
                                                           @RequestParam String environmentID,@RequestParam String deployPhasesId)throws IOException {


        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllReleaseTasklog(request,token,organization,projectName,releaseId
                        ,environmentID,deployPhasesId), HttpStatus.OK);
    }




    //Get a list of logs from a pipeline run.
    @GetMapping("getAllLogsList")
    public ResponseEntity<JSONObject> getAllLogsList(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                     @RequestParam String runId,@RequestParam String projectName,@RequestParam String pipelineId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllLogsList(request,token,organization,projectName,pipelineId, runId), HttpStatus.OK);
    }

    //Get a list of logs from a pipeline run.
    @GetMapping("getAllLogByLogId")
    public ResponseEntity<JSONObject> getAllLogByLogId(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                       @RequestParam String runId,@RequestParam String projectName,
                                                       @RequestParam String pipelineId,@RequestParam String logId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.getAllLogByLogId(request,token,organization,projectName,pipelineId, runId,logId), HttpStatus.OK);
    }

    @PostMapping("runPipeline")
    public ResponseEntity<JSONObject> runPipeline(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                  @RequestParam String  projectName,
                                                  @RequestParam  String pipelineId,
                                                  @RequestParam  String branch) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.runPipeline(request,token,organization,projectName,pipelineId,branch), HttpStatus.OK);
    }



    @GetMapping("runPipelineStatus")
    public ResponseEntity<JSONObject> runPipelineStatus(HttpServletRequest request,@RequestParam String token,@RequestParam String organization,
                                                  @RequestParam String  projectName,
                                                  @RequestParam  String pipelineId,
                                                  @RequestParam  String runId) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.runPipelineStatus(request,token,organization,projectName,pipelineId,runId), HttpStatus.OK);
    }
    @PostMapping("renamePipeline")
    public ResponseEntity<JSONObject> renamePipeline(@RequestParam String token,
                                                     @RequestParam String organization,
                                                     @RequestParam String  projectName,
                                                     @RequestParam  String definitionId,
                                                     @RequestParam( required=false)  String oldName,
                                                     @RequestParam( required=false)  String newName,
                                                     @RequestParam( required=false)  String oldPath,
                                                     @RequestParam( required=false)  String newPath,
                                                     HttpServletRequest request

    ) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.renamePipeline(request,token,organization,projectName,definitionId,oldName,newName,oldPath,newPath), HttpStatus.OK);
    }

    @DeleteMapping("deletePipeline")
    public ResponseEntity<HashMap<String,String>> deletePipeline( HttpServletRequest request,@RequestParam String token, @RequestParam String organization, @RequestParam String  projectName, @RequestParam  String definitionId) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.deletePipeline(request,token,organization,projectName,definitionId), HttpStatus.OK);
    }


    //Gets top 10000 runs for a particular pipeline.
    @GetMapping("runsListForPipeline")
    public ResponseEntity<JSONObject> runsListForPipeline(HttpServletRequest request,@RequestParam String token,
                                                          @RequestParam String organization,
                                                          @RequestParam String  projectName,
                                                          @RequestParam  String pipelineId
                                                          ) throws IOException {

        return new ResponseEntity<>(
                azureDevOpsPipelineService.runsListForPipeline(request,token,organization,projectName,pipelineId), HttpStatus.OK);
    }


    //Gets top 10000 runs for a particular pipeline.
    @GetMapping("releaseDefinitionsList")
    public ResponseEntity<JSONObject> releaseDefinitionsList( HttpServletRequest request,@RequestParam String token,
                                                             @RequestParam String organization,
                                                             @RequestParam String  projectName) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.releaseDefinitionsList(request,token,organization,projectName), HttpStatus.OK);
    }


    @GetMapping("releaseList")
    public ResponseEntity<JSONObject> releaseList(@RequestParam String token,HttpServletRequest request,
                                                  @RequestParam String organization,
                                                  @RequestParam String  projectName) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.releaseList(request,token,organization,projectName), HttpStatus.OK);
    }

    @GetMapping("getStageOfRelease")
    public ResponseEntity<JSONObject> getStageOfRelease(@RequestParam String token,HttpServletRequest request,
                                                        @RequestParam String organization,
                                                        @RequestParam String  projectName,
                                                        @RequestParam String  releaseId) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.getStageOfRelease(request,token,organization,projectName,releaseId), HttpStatus.OK);
    }

    @PatchMapping("deployeStageOfRelease")
    public ResponseEntity<JSONObject> deployeStageOfRelease(@RequestParam String token,HttpServletRequest request,
                                                        @RequestParam String organization,
                                                        @RequestParam String  projectName,
                                                        @RequestParam String  releaseId,
                                                        @RequestParam String  stageId) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.deployeStageOfRelease(request,token,organization,projectName,releaseId,stageId), HttpStatus.OK);
    }

    @GetMapping("releasesGetLogs")
    public ResponseEntity<Resource> releasesGetLogs(@RequestParam String token, HttpServletRequest request, HttpServletResponse response,
                                                      @RequestParam(value="projectName") String  projectName ,
                                                    @RequestParam(value="organization") String organization,
                                                    @RequestParam(value="releaseId") String releaseId
                                                      ) throws IOException {

        URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
                "release/releases/"+releaseId+"/logs?api-version=6.1");
        URLConnection uc = url.openConnection();
        String username=null;
        String password=token;
        String inputLine=null;
        //StringBuffer response = new StringBuffer();
        final  Long MILLS_IN_DAY = 86400000L;
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        uc.setRequestProperty ("Authorization", basicAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        InputStream is =  uc.getInputStream();; // get your input stream here
        Resource resource = new InputStreamResource(is);
        response.setContentType("application/zip");
         response.setHeader("Content-Disposition", "attachment; filename="+projectName+"_releasesLogs.zip");
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("repositoriesList")
    public ResponseEntity<JSONObject> repositoriesList(@RequestParam String token,HttpServletRequest request,
                                                       @RequestParam String organization,
                                                       @RequestParam String  projectName) throws IOException {
        return new ResponseEntity<>(
                azureDevOpsPipelineService.repositoriesList(request,token,organization,projectName), HttpStatus.OK);
    }


}
