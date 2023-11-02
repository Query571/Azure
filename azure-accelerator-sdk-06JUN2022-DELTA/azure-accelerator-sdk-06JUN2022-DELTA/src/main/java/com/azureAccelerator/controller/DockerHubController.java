package com.azureAccelerator.controller;

import com.azureAccelerator.service.DockerHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DockerHubController {
    private final DockerHubService dockerHubService;


    public DockerHubController(DockerHubService dockerHubService) {
        this.dockerHubService = dockerHubService;
    }
    @PostMapping("loginDockerHub")
    public ResponseEntity<Object> loginDockerHub(@RequestParam String userName, @RequestParam String password) throws Exception {
        return new ResponseEntity<>(dockerHubService.loginDockerHub(userName,password), HttpStatus.OK);
    }
    @GetMapping("getRepositories")
    public ResponseEntity<Object> getRepositories(@RequestParam String userNameSpace,@RequestParam String token) throws Exception {
        return new ResponseEntity<>(dockerHubService.getRepositories(userNameSpace,token), HttpStatus.OK);
    }
    @GetMapping("getRepositoriesImagesAndTags")
    public Object getRepositoriesImagesAndTags(@RequestParam String userNameSpace, @RequestParam String repositories, @RequestParam String token) throws Exception {
        return new ResponseEntity<>(dockerHubService.getRepositoriesImagesAndTags(userNameSpace,repositories,token), HttpStatus.OK);
    }
}
