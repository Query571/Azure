package com.azureAccelerator.service;

import com.azureAccelerator.dto.RouteTablesDto;
import com.azureAccelerator.dto.RouteTablesResponseDto;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RouteTablesService {

    RouteTablesResponseDto createRouteTable(HttpServletRequest request, RouteTablesDto routeTablesDto) throws IOException;
    List<RouteTablesResponseDto> getRouteTables(HttpServletRequest request,String resourceGroupName);
}
