package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
public class UpgrKuberNetVerDto {

    private String resourceGroupName;
    private String aksName;
    private String version;
}
