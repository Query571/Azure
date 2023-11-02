package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Properties2 {
    private AddressSpace addressSpace;
    private List<Subnets> subnets;
}
