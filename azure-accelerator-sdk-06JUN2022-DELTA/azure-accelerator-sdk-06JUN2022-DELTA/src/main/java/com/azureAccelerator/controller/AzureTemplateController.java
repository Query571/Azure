package com.azureAccelerator.controller;

import com.azureAccelerator.dto.AzureCommonResponseDto;
import com.azureAccelerator.dto.ExportTempDto;
import com.azureAccelerator.service.AzureTemplateService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AzureTemplateController {

  private final AzureTemplateService azureTemplateService;

  public AzureTemplateController(
      AzureTemplateService azureTemplateService) {
    this.azureTemplateService = azureTemplateService;
  }

  @PostMapping("exportTemplate")
  public ResponseEntity<AzureCommonResponseDto> exportTemplate(HttpServletRequest request,
                                                               @RequestBody ExportTempDto exportTempDto ) throws JSONException {

    return new ResponseEntity<>(
        azureTemplateService.exportTemplate(request,exportTempDto), HttpStatus.OK);
  }

  @GetMapping("exportAllTemplates")
  public ResponseEntity<String> exportAllTemplates(HttpServletRequest request,
      @RequestParam String resourceGroupName) throws JSONException {

    return new ResponseEntity<String>(
        azureTemplateService.exportAllTemplates(request,resourceGroupName), HttpStatus.OK);
  }

  @PostMapping("uploadTemplate")
  public ResponseEntity<AzureCommonResponseDto> uploadTemplate(HttpServletRequest request,
      @RequestParam("file") MultipartFile file,
      @RequestParam String resourceGroup) throws Exception {

    String fileExtensions = ".json,.JSON";
    String substring;
    try {
      String fileName = file.getOriginalFilename();
      int lastIndex = fileName.lastIndexOf('.');
      substring = fileName.substring(lastIndex, fileName.length());
    }catch (Exception e){
      throw new Exception("please select .json file");
    }
    if(fileExtensions.contains(substring.toLowerCase())){
      if (file.getSize()<= 5000000) {
        return new ResponseEntity<AzureCommonResponseDto>(
                azureTemplateService.uploadTemplate(request,file, resourceGroup), HttpStatus.OK);
      }else {
        throw new Exception("size limit is 5 MB only");
      }
    }
    else{
      throw new Exception("In-Valid File Type");
    }

  }

}
