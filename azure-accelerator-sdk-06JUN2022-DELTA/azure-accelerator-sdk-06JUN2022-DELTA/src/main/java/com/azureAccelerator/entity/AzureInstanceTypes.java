package com.azureAccelerator.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "azure_instance_types")
@Getter
@Setter
public class AzureInstanceTypes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "vm_size")
  private String vmSize;
  @Column(name = "vCore")
  private int vCore;
  @Column(name = "memory_In_GB")
  private int memoryInGB;

}
