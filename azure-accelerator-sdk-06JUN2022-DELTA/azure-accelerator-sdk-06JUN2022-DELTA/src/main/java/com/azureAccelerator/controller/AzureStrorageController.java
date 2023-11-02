package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.service.AzureStrorageService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class AzureStrorageController {

    private final AzureStrorageService azureStrorageService;

    public AzureStrorageController(AzureStrorageService azureStrorageService) {
        this.azureStrorageService = azureStrorageService;
    }

    @PostMapping("createStorage")
    public ResponseEntity<StrorageResponseDto> createStorage(HttpServletRequest request,
            @RequestBody StroageDto stroageDto) throws JSONException {

        return new ResponseEntity<>(
                azureStrorageService.createStorage(request,stroageDto), HttpStatus.OK);
    }

    @GetMapping("storage")
    public ResponseEntity<List<StrorageResponseDto>> storage(HttpServletRequest request,
            @RequestParam String resourceGroupName) throws JSONException {

        return new ResponseEntity<>(
                azureStrorageService.storage(request,resourceGroupName),HttpStatus.OK);
    }

    @DeleteMapping("deleteStorage")
    public ResponseEntity<Map<String,String>> deleteStorage(HttpServletRequest request,@RequestParam String storageId) throws Exception {

        return new ResponseEntity<>(
                azureStrorageService.deleteStorage(request,storageId), HttpStatus.OK);
    }

    @DeleteMapping("deleteStorages")
    public ResponseEntity<Map<String,String>> deleteStorages(HttpServletRequest request, @RequestBody List<StoragesDto> storageId) throws Exception {

        return new ResponseEntity<>(
                azureStrorageService.deleteStorages(request,storageId), HttpStatus.OK);
    }

}
