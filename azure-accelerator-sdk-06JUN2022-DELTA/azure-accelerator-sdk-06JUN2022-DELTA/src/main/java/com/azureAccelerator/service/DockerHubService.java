package com.azureAccelerator.service;

import java.io.IOException;

public interface DockerHubService {

    Object loginDockerHub(String userName, String password) throws Exception;

    Object getRepositories(String userNameSpace,String token) throws Exception;

    Object getRepositoriesImagesAndTags(String userNameSpace,String repositories,String token) throws Exception;
}
