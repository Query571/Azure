package com.azureAccelerator.controller;

import com.azureAccelerator.dto.AzureCommonDto;
import com.azureAccelerator.dto.AzureCommonResponseDto;
import com.azureAccelerator.service.AzureRGService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AzureRGController {

    private final AzureRGService azureRGService;

    public AzureRGController(AzureRGService azureRGService) {
        this.azureRGService = azureRGService;
    }

    @PostMapping("createResourceGrp")
    public ResponseEntity<AzureCommonResponseDto> createResourceGrp(HttpServletRequest request,
            @RequestParam String resourceGroupName,@RequestParam String rigonName) throws JSONException {

        return new ResponseEntity<>(
                azureRGService.createResourceGrp(request,resourceGroupName,rigonName), HttpStatus.CREATED);
    }
}
