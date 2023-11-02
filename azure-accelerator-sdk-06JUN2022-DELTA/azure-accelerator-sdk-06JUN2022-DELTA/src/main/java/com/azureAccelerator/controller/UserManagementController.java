package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class UserManagementController {
    private final UserService userManagementService;

    public UserManagementController(UserService userManagementService) {
        this.userManagementService= userManagementService;
    }

    @GetMapping("getRoles")
    public ResponseEntity<List<UserRoleDto>> getUsers()  {
        return new ResponseEntity<>(
                userManagementService.getAllRole(), HttpStatus.OK);
    }

    @PostMapping("addUser")
    public ResponseEntity<LocalUserDto> addUser(@RequestBody LocalUserDto user)  {
        return new ResponseEntity<>(
                userManagementService.addUser(user), HttpStatus.OK);
    }

    @GetMapping("getAllUser")
    public ResponseEntity<Map<String,String>> getAllUser()  {
        return new ResponseEntity<>(
                userManagementService.findAll(), HttpStatus.OK);
    }

    @PutMapping("editUser")
    public ResponseEntity<LocalUserDto> editUser(@RequestBody LocalUserDto user)  {
        return new ResponseEntity<>(
                userManagementService.updateUser(user), HttpStatus.OK);
    }


    @DeleteMapping("deleteUser")
    public ResponseEntity<LocalUserDto> deleteUser(@RequestParam String id)  {
        return new ResponseEntity<>(
                userManagementService.delete(Long.parseLong(id)), HttpStatus.OK);
    }


    @GetMapping("findByName")
    public ResponseEntity<List<LocalUserDto>> findByName(@RequestParam String name)  {
        return new ResponseEntity<>(
                userManagementService.findByName(name), HttpStatus.OK);
    }

    @GetMapping("findByEmail")
    public ResponseEntity<List<LocalUserDto>> findByEmail(@RequestParam String email)  {


        return new ResponseEntity<>(
                userManagementService.findByEmail(email), HttpStatus.OK);
    }


    @PutMapping("updateSubIdByUser")
    public ResponseEntity<Map<String,String>>  updateUserBySubId(HttpServletRequest req, @RequestParam String subId){

        String userName=req.getHeader("userName");
        return new ResponseEntity<>(userManagementService.updateSubIdByUser(userName,subId),HttpStatus.OK);
    }

    @PutMapping("updatePassword")
    public ResponseEntity<LocalUserDto>  updatePassword(@RequestBody UpdatePasswordDto userDto)  {
        return new ResponseEntity<>(
                userManagementService.updatePassword(userDto), HttpStatus.OK);
    }


    @GetMapping("unlock/user")
    public ResponseEntity<Map<Integer,String>> unlockUser(@RequestParam String uniqueUserId)  {
        return new ResponseEntity<>(
                userManagementService.unlockUser(uniqueUserId), HttpStatus.OK);
    }

    @PostMapping("lock/user")
    public ResponseEntity<Map<Integer,String>> lockUser(@RequestParam String uniqueUserId)  {
        return new ResponseEntity<>(
                userManagementService.lockUser(uniqueUserId), HttpStatus.OK);
    }

    @GetMapping("userLoginStatus")
    public ResponseEntity<Map<String,String>> userLoginStatus(@RequestParam String username,
                                                               @RequestParam String jwtToken)  {
        Map<String,String> response=userManagementService.userLoginStatus(username,jwtToken);
                String status=response.get("Status");
        return new ResponseEntity<>(response
                , status.equals("401")?HttpStatus.UNAUTHORIZED:HttpStatus.OK);
    }

    @GetMapping("userLogout")
    public ResponseEntity<Map<Integer,String>> userLogout(@RequestParam String username)  {
        return new ResponseEntity<>(
                userManagementService.userLogout(username), HttpStatus.OK);
    }

    @GetMapping("getPublicKey")
    public ResponseEntity<Map<Integer,String>> getPublicKey(@RequestParam String username) throws Exception {
        return new ResponseEntity<>(
                userManagementService.getPublicKey(username), HttpStatus.OK);
    }


    @GetMapping("getSecretKey")
    public ResponseEntity<Map<String,String>> getSecretKey(@RequestParam String username) throws Exception {
        return new ResponseEntity<>(
                userManagementService.getSecretKey(username), HttpStatus.OK);
    }

    @GetMapping("deleteSecretKey")
    public ResponseEntity<Map<String,String>> deleteSecretKey(@RequestParam String username) throws Exception {
        return new ResponseEntity<>(
                userManagementService.deleteSecretKey(username), HttpStatus.OK);
    }
    @GetMapping("setSecretKey")
    public ResponseEntity<Map<String,String>> setSecretKey(@RequestParam String username,@RequestParam String key) throws Exception {
        return new ResponseEntity<>(
                userManagementService.setSecretKey(username,key), HttpStatus.OK);
    }


    @GetMapping("updatedSSOConfiguration")
    public ResponseEntity<Map<String,String>> updatedSSOConfiguration(@RequestParam(required = false)  String oldCleintID,
                                                                      @RequestParam String cleintID,
                                                          @RequestParam(required = false) String tenetID,
                                                          @RequestParam(required = false) String loginUrl,
                                                          @RequestParam(required = false) String logoutUrl,

                                                          @RequestParam String filePath) throws Exception {
        return new ResponseEntity<>(
                userManagementService.updatedSSOConfiguration(cleintID,oldCleintID,filePath), HttpStatus.OK);
    }
}
