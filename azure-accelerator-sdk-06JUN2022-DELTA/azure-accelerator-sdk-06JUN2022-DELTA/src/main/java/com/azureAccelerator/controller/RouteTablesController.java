package com.azureAccelerator.controller;


import com.azureAccelerator.dto.RouteTablesDto;
import com.azureAccelerator.dto.RouteTablesResponseDto;
import com.azureAccelerator.service.RouteTablesService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class RouteTablesController {

    private RouteTablesService routeTablesService;

    RouteTablesController(RouteTablesService routeTablesService){this.routeTablesService=routeTablesService;}

    @PostMapping("createRouteTable")
    public  RouteTablesResponseDto createRouteTable(HttpServletRequest request, @RequestBody RouteTablesDto routeTablesDto) throws IOException {

        return routeTablesService.createRouteTable(request,routeTablesDto);
    }

    @GetMapping("getRouteTables")
    public List<RouteTablesResponseDto> getRouteTables(HttpServletRequest request,@RequestParam String resourceGroupName){

        return routeTablesService.getRouteTables(request,resourceGroupName);
    }

}
