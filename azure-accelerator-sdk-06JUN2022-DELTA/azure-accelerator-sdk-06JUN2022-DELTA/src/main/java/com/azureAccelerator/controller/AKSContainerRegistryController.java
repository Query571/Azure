package com.azureAccelerator.controller;

import com.azureAccelerator.dto.AKSContainerRegistryDto;
import com.azureAccelerator.dto.AKSContainerRegistryResponseDto;
import com.azureAccelerator.dto.AKSDeployDto;
import com.azureAccelerator.dto.AKSDeployResponse;
import com.azureAccelerator.service.AKSContainerRegistryService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class AKSContainerRegistryController {

    private final AKSContainerRegistryService aksContainerRegistryService;

    public AKSContainerRegistryController(AKSContainerRegistryService aksContainerRegistryService) {
        this.aksContainerRegistryService = aksContainerRegistryService;
    }

    @PostMapping("createContainerReg")
    public ResponseEntity<AKSContainerRegistryResponseDto> createConainerReg(HttpServletRequest request,
                                                                             @RequestParam String name, @RequestParam String resourceGroupName, @RequestParam String region, @RequestParam String acrSize) throws JSONException {

        return new ResponseEntity<AKSContainerRegistryResponseDto>(
                aksContainerRegistryService.createConainerReg( request,name,resourceGroupName,region,acrSize), HttpStatus.CREATED);
    }

    @GetMapping("getContainerRegistry")
    public ResponseEntity<List<AKSContainerRegistryResponseDto>> getContainerRegistry(HttpServletRequest request,
            @RequestParam String resourceGroupName) throws JSONException {

        return new ResponseEntity<>(
                aksContainerRegistryService.getContainerRegistry(request,resourceGroupName), HttpStatus.OK);
    }

    @GetMapping("getContainerRepository")
    public ResponseEntity<List<String>> getContainerRepository(HttpServletRequest request,
            @RequestParam String containerRegName) throws JSONException {

        return new ResponseEntity<>(
                aksContainerRegistryService.getContainerRepository(request,containerRegName), HttpStatus.OK);
    }
    @DeleteMapping("deleteContainerRepository")
    public ResponseEntity<Map<String, String>> deleteACR(HttpServletRequest request,@RequestParam String myResourceGroup,@RequestParam String registryName) throws Exception {

        return new ResponseEntity<Map<String, String>>(aksContainerRegistryService.deleteAcr(request,myResourceGroup,registryName),HttpStatus.OK);
    }
}
