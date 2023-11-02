package com.azureAccelerator.repository;

import com.azureAccelerator.entity.AzureInstanceTypes;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AzureInstanceTypesRepository extends JpaRepository<AzureInstanceTypes,Long> {

  List<AzureInstanceTypes> findAllByVCoreGreaterThanEqualAndMemoryInGBGreaterThanEqual(int vCore, int memoryInGB);

  List<AzureInstanceTypes> findByVmSizeIn(List<String> vmSize);
}
