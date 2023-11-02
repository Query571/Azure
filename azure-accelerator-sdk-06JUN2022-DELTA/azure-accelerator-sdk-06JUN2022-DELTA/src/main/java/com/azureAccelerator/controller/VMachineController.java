package com.azureAccelerator.controller;


import com.azureAccelerator.dto.VMachineDto;
import com.azureAccelerator.dto.VMachineResponseDto;
import com.azureAccelerator.dto.VMsDto;
import com.azureAccelerator.dto.VNetResponseDto;
import com.azureAccelerator.entity.AzureInstanceTypes;
import com.azureAccelerator.service.VMachineService;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class VMachineController {

    private final VMachineService vMachineService;

    public VMachineController(VMachineService vMachineService) {
        this.vMachineService = vMachineService;
    }

    @PostMapping("createVM")
    public ResponseEntity<List<VMachineResponseDto>> createVM(HttpServletRequest request,
            @RequestBody List<VMachineDto> vMsDto, @RequestParam int vmCount) throws Exception {

            return new ResponseEntity<List<VMachineResponseDto>>(
                    vMachineService.createVM(request,vMsDto ,vmCount), HttpStatus.CREATED);
    }

    @PostMapping("createVM2")
    public ResponseEntity<VMachineResponseDto> createVM2(HttpServletRequest request,
            @RequestBody VMachineDto vMsDto) throws JSONException, IOException {

        return new ResponseEntity<>(
                vMachineService.createVM2(request,vMsDto), HttpStatus.CREATED);
    }

    @PostMapping("startVM")
    public ResponseEntity<Map<String, String>> startVM(HttpServletRequest request,@RequestParam String myResourceGroup,
                                          @RequestParam String myVM) throws JSONException {

        return new ResponseEntity<Map<String, String>>(vMachineService.startVM(request,myResourceGroup,myVM),HttpStatus.OK);
    }

    @PostMapping("reStartVM")
    public ResponseEntity<Map<String, String>> reStartVM(HttpServletRequest request,@RequestParam String myResourceGroup,
                                          @RequestParam String myVM) throws JSONException {

        return new ResponseEntity<Map<String, String>>(vMachineService.reStartVM(request,myResourceGroup,myVM),HttpStatus.OK);
    }

    @PostMapping("stopVMo")
    public ResponseEntity<Map<String, String>> stopVM(HttpServletRequest request,@RequestParam String myResourceGroup,
                                                      @RequestParam String myVM) throws JSONException {

        return new ResponseEntity<Map<String, String>>(vMachineService.stopVM(request,myResourceGroup,myVM),HttpStatus.OK);
    }

    @DeleteMapping("deleteVMs")
    public ResponseEntity<Map<String, String>> deleteVMs(HttpServletRequest request,@RequestBody List<VMachineDto> vMsDto) throws Exception {

        return new ResponseEntity<Map<String, String>>(vMachineService.deleteVMs(request,vMsDto),HttpStatus.OK);
    }

    @DeleteMapping("deleteVM")
    public ResponseEntity<Map<String, String>> deleteVM(HttpServletRequest request,@RequestParam String myResourceGroup,
                                         @RequestParam String myVM) throws Exception {

        return new ResponseEntity<Map<String, String>>(vMachineService.deleteVM(request,myResourceGroup,myVM),HttpStatus.OK);
    }

    /*@GetMapping("getSizes")
    public List<AzureInstanceTypes> getSizes(){
        return vMachineService.();}*/

    @GetMapping("getVms")
    public ResponseEntity<List<VMachineResponseDto>> getVms(HttpServletRequest request,@RequestParam String resourceGroupName) throws JSONException {


        return new ResponseEntity(vMachineService.listVMs(request,resourceGroupName), HttpStatus.OK);
    }
    @GetMapping("getVm")
    public ResponseEntity<VMachineResponseDto> getVm(HttpServletRequest request,@RequestParam String resourceGroupName, @RequestParam String myVm) throws JSONException {


        return new ResponseEntity(vMachineService.getVM(request,resourceGroupName,myVm), HttpStatus.OK);
    }
    @PutMapping("resizeVM")
    public ResponseEntity<VMachineResponseDto> reSize(HttpServletRequest request,@RequestParam String myResourceGroup,@RequestParam String myVM,
                                                      @RequestParam String size) throws JSONException {

        return new ResponseEntity<VMachineResponseDto>(
                vMachineService.reSizeVM(request,myResourceGroup,myVM,size), HttpStatus.OK);
    }
    @PutMapping("addDisk")
    public ResponseEntity<VMachineResponseDto> addDisk(HttpServletRequest request,@RequestParam String diskName,@RequestParam String myResourceGroup,@RequestParam String myVM
            ,@RequestParam String disk_cachingType,@RequestParam int gb,@RequestParam int lun,@RequestParam String storageAccountTypes ) throws JSONException {

        return new ResponseEntity<VMachineResponseDto>(
                vMachineService.addDisk(request,diskName,myResourceGroup,myVM,disk_cachingType,gb,lun,storageAccountTypes),HttpStatus.OK);
    }

    @PutMapping("disaaociation")
    public String getNI(HttpServletRequest request,@RequestParam String myResourceGroup,@RequestParam String ni) throws Exception {

        return  vMachineService.getNI(request,myResourceGroup,ni);
    }

    /*@GetMapping("getVm")
    public JSONObject getVm(@RequestParam String resourceGroupName, @RequestParam String myVm) throws IOException {

        return vMachineService.getVM(resourceGroupName,myVm);
    }*/

    /*@GetMapping("cachingList")
    public List<String> getCatchingTypes(){
        return vMachineService.getCatchingTypes();
    }*/
    @GetMapping("imagesList")
    public List<String> getImagesList(HttpServletRequest request,@RequestParam String osType){

        return vMachineService.getVMImagesList(request,osType);
    }

    @GetMapping("getRegions")
    public ResponseEntity<JSONObject> getVmSizes(HttpServletRequest request) throws IOException, JSONException {


        return new ResponseEntity<>(vMachineService.getRegions(request),HttpStatus.OK);
    }

    @GetMapping("getRecVMSizes")
    public ResponseEntity<List<AzureInstanceTypes>> getRecVMSizes(HttpServletRequest request){


        return new ResponseEntity<>(vMachineService.recVMSizes(request),HttpStatus.OK);
    }

    @DeleteMapping("deleteDataDisk")
    public ResponseEntity<Map<String, String>> deleteDataDisk(HttpServletRequest request, @RequestParam String myResourceGroup,
                                                              @RequestParam String myVM, @RequestParam int lun) throws JSONException {

        return new ResponseEntity<Map<String, String>>(vMachineService.removeDisk(request,myResourceGroup,myVM,lun),HttpStatus.OK);
    }

}
