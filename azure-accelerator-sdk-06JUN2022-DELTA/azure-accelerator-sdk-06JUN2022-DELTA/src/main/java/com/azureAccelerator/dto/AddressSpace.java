package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddressSpace {
    private List<String> addressPrefixes;

}
