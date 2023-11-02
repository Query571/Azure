import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SharedServiceService } from './shared-service.service';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  constructor(
    protected httpClient: HttpClient,
    protected sharedService: SharedServiceService
  ) {}

  updatePassword(data: { id: any;userUID:any, oldPassword: any; newPassword: any }) {
    return this.httpClient.put(this.sharedService.changePwdUrl, data);
  }

  storeCloudCreds(data: {
    subscriptionId: any;
    clientId: any;
    clientSecret: any;
    tenantId: any;
  }) {
    return this.httpClient.post(this.sharedService.storeCloudCreds, data);
  }

  getCloudCreds() {
    return this.httpClient.get(this.sharedService.getCloudCreds);
  }

  getSavedAzureCreds() {
    return this.httpClient.get(this.sharedService.azureCreds);
  }

  siteList() {
    return this.httpClient.get(this.sharedService.getSiteListUrl);
  }

  getServiceToDeviceAssociationUrl() {
    return this.httpClient.get(
      this.sharedService.serviceToDeviceAssociationMasterDataUrl
    );
  }

  getAzureResourceGroupList() {
    return this.httpClient.get(this.sharedService.resourceGroupsUrl);
  }
  getLocationWiseVnetGroupList(resourceGroupName: string,region:string) {
    return this.httpClient.get(
      this.sharedService.vnetGroupsUrl + resourceGroupName + '&region=' + region
    );
  }

  getAzureVnetGroupList(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.vnetGroupsUrl + resourceGroupName
    );
  }

  storeVnetData(data: {
    name: any;
    resourceGroupName: any;
    addressSpace: any;
    subnetName: any;
    subnetAddressRange: any;
  }) {
    return this.httpClient.post(this.sharedService.createVnetUrl, data);
  }

  getsubnet(vnet, resourceGroupName) {
    const params = new HttpParams()
      .set('vNetName', vnet)
      .set('resourceGroupName', resourceGroupName);
    return this.httpClient.get(this.sharedService.getSubnetUrl, { params });
  }

  deleteVnetData(subId: string) {
    return this.httpClient.delete(this.sharedService.deleteVnetUrl + subId);
  }

  getKeyVaultGroupList(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.keyVaultsGroupUrl + resourceGroupName
    );
  }

  createKeyVaultData(data: { name: any; resourceGroupName: string }) {
    return this.httpClient.post(this.sharedService.createKeyVaultUrl, data);
  }

  deleteKeyVaultData(subId: string) {
    return this.httpClient.delete(this.sharedService.deleteKeyVaultUrl + subId);
  }

  getAzureSqlGroupList(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.sqlGroupsUrl + resourceGroupName
    );
  }

  getAzureSqlDatabaseList(servername: string, resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.listSqlDbUrl +
        servername +
        '&resourceGroupName=' +
        resourceGroupName
    );
  }

  storeSQLData(data: {
    sqlDBName: any;
    resourceGroupName: string;
    sqlServerName: string;
    configurable: boolean;
  }) {
    return this.httpClient.post(this.sharedService.createSqlUrl, data);
  }

  deleteSQLData(data: any) {
    return this.httpClient.post(this.sharedService.deleteSqlUrl, data);
  }
  createAzureSqlServer(data: {
    location: string;
    resourceGroupName: any;
    serverName: any;
    adminUser: any;
    adminPassword: any;
  }) {
    return this.httpClient.post(this.sharedService.createSqlServerUrl, data);
  }

  getListOfStorageAccount(resourceGroupNam: string) {
    return this.httpClient.get(
      this.sharedService.listStorageAccountUrl + resourceGroupNam
    );
  }
  createStorageAccount(data: {
    location: string;
    resourceGroupName: string;
    storageName: any;
    storageKind: string;
  }) {
    return this.httpClient.post(
      this.sharedService.createStorageAccountUrl,
      data
    );
  }

  deleteStorageAccount(id: string) {
    return this.httpClient.delete(
      this.sharedService.deleteStorageAccountUrl + id
    );
  }

  getAksLocationList() {
    return this.httpClient.get(this.sharedService.akslocationsUrl);
  }

  getResourceGroupLocation(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.resgrplocationUrl + resourceGroupName,
      { responseType: 'text' }
    );
  }

  getAKSClusterList(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.aksListDownUrl + resourceGroupName
    );
  }

  decodeVersion(res:any){
    res.forEach(element => {
      element['kubernetesVersion'] = window.atob(element.kubernetesVersion)
    });
    return res;
  }

  getVmsize() {
    return this.httpClient.get(this.sharedService.vmSizeUrl);
  }

  getUpdatedVmsize() {
    return this.httpClient.get(this.sharedService.vmnewSize);
  }


  deployAKSCluster(data: {
    aksName: any;
    resourceGroup: any;
    location: string;
    vmSize: string;
    vmCount: any;
    agentPoolMode: string;
    systemType: any;
    diskSizeInGB: any;
    maxPodsCount: any;
  }) {
    return this.httpClient.post(this.sharedService.deployAKSUrl, data);
  }

  deleteAKSCluster(id: string) {
    return this.httpClient.delete(this.sharedService.deleteAKSUrl + id);
  }

  createNodePool(nodePool: {
    aksName: string;
    nodePoolName: any;
    resourceGroupName: string;
    nodeCount: any;
    systemType: any;
    nodeSize: string;
    maxPods: any;
    nodeDiskSize: any;
  }) {
    return this.httpClient.post(this.sharedService.createNodepoolUrl, nodePool);
  }

  getKubernetVersion(resourceGroupName: string, aksName: string) {
    return this.httpClient.get(
      this.sharedService.getKubernetUrl +
        resourceGroupName +
        '&aksName=' +
        aksName
    );
  }

  getKubernetDashboard(resourceGroupName: string, aksName: string) {
    return this.httpClient.get(
      this.sharedService.getKubernetDasboardUrl +
        resourceGroupName +
        '&aksName=' +
        aksName
    );
  }

  setKubernetVersion(upgradeAksObj: {
    resourceGroupName: string;
    aksName: string;
    version: string;
  }) {
    return this.httpClient.post(
      this.sharedService.setKubernetUrl,
      upgradeAksObj
    );
  }

  scaleNodePool(scaleobj: {
    resourceGroupName: string;
    aksName: string;
    nodePoolName: string;
    nodeCount: any;
  }) {
    return this.httpClient.post(this.sharedService.scaleNodeUrl, scaleobj);
  }

  startAks(rg: any,aksname:any) {
    return this.httpClient.post(this.sharedService.startaks + rg + '&aksName=' + aksname ,{});
  }

  stopAks(rg: any,aksname:any) {
    return this.httpClient.post(this.sharedService.stopaks + rg + '&aksName=' + aksname ,{});
  }
  createPolicy(data: any) {
    return this.httpClient.post(this.sharedService.createPolicyUrl, data);
  }

  listPolicy(groupType:any) {
    return this.httpClient.get(this.sharedService.listPolicUrl + groupType);
  }

  listAlertRules(id: string) {
    return this.httpClient.get(this.sharedService.listAlertRulesUrl + id);
  }

  createAlertRules(reqObject: {
    resourceGroup: string;
    resourceId: string;
    actionGroupId: string;
    targetResourceName: any;
  }) {
    return this.httpClient.post(this.sharedService.createAlertsUrl, reqObject);
  }

  listActionGroup(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.listActionGroupUrl + resourceGroupName
    );
  }

  createActiongroup(reqObject: {
    name: any;
    resourceGroup: string;
    receiver: any;
    emailId: any;
    countyCode: any;
    phoneNumber: any;
  }) {
    return this.httpClient.post(this.sharedService.createAgUrl, reqObject);
  }

  listAppAervices(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.listAppServicesUrl + resourceGroupName
    );
  }

  appServiceStart(id: string) {
    return this.httpClient.get(this.sharedService.appStartUrl + id);
  }

  appServiceStop(id: string) {
    return this.httpClient.get(this.sharedService.appStopUrl + id);
  }

  appServiceRestart(id: string) {
    return this.httpClient.get(this.sharedService.appRestartUrl + id);
  }

  appServiceDelete(id: string) {
    return this.httpClient.get(this.sharedService.appDeleteUrl + id);
  }

  createBlobStorage(data: any) {
    return this.httpClient.post(this.sharedService.createBlobStorageUrl, data);
  }

  deleteBlobStorage(id: string) {
    return this.httpClient.delete(this.sharedService.deleteBlobStorageUrl + id);
  }

  getAppRegList() {
    return this.httpClient.get(this.sharedService.getAppListUrl);
  }

  createAppReg(name: string, uri: string) {
    return this.httpClient.post(
      this.sharedService.createAppRegUrl + name + '&appUri=' + uri,
      null
    );
  }

  exportService(serviceObj: any) {
    return this.httpClient.post(
      this.sharedService.exportServiceUrl,
      serviceObj
    );
  }

  exportkeyvalutARMtemplate(rg,keyval){
    return this.httpClient.get(
      this.sharedService.exportkeyvaultARMTemplate + rg + '&keyVault=' + keyval,
    );
  }

  exportARMtemplate(rg,vnet){
    return this.httpClient.get(
      this.sharedService.exportvnetarMtemplate + rg + '&vNet=' + vnet,
    );
  }

  uploadFormData(formData: FormData, rg: string) {
    const params = new HttpParams().set('resourceGroup', rg);
    return this.httpClient.post(this.sharedService.uploadUrl, formData, {
      params,
    });
  }

  uploadAksData(formData: FormData) {
    return this.httpClient.post(this.sharedService.aksAppDeployUrl, formData);
  }

  getContainerRegistryList(resourceGroupName: string) {
    const params = new HttpParams().set('resourceGroupName', resourceGroupName);
    return this.httpClient.get(this.sharedService.listRegistryUrl, { params });
  }

  createRegistry(reqObj: { name: any; resourceGroupName: any ;region:any;acrSize:any}) {
    return this.httpClient.post(
      this.sharedService.createRegistryUrl +
        reqObj.name +
        '&resourceGroupName=' +
        reqObj.resourceGroupName + '&region=' + reqObj.region + '&acrSize=' +reqObj.acrSize,
      null
    );
  }

  getRepositoryList(containerName: string) {
    const params = new HttpParams().set('containerRegName', containerName);
    return this.httpClient.get(this.sharedService.repositoryUrl, { params });
  }

  createResourceGroup(resGrpObj: { resGroupName: any; location: any }) {
    return this.httpClient.post(
      this.sharedService.createResourceGroupUrl +
        resGrpObj.resGroupName +
        '&&rigonName=' +
        resGrpObj.location,
      null
    );
  }

  getAzureSSHKeyGroupList(resourceGroupName: string) {
    return this.httpClient.get(
      this.sharedService.sshKeyGroup + resourceGroupName
    );
  }
  getAzureSubnetGroupList(resourceGroupName: string ,vnetName : string) {
    return this.httpClient.get(
      this.sharedService.subnetGroup + resourceGroupName + '&vNetName=' + vnetName
    );
  }

  getAzureAssociatedSubnetGroupList(resourceGroupName: string ,vnetName : string) {
    return this.httpClient.get( 
      this.sharedService.subnetGroup + resourceGroupName + '&vNetName=' + vnetName + '&activeFlag=Y'
    );
  }

  createAzureSSHkey(data){
      return this.httpClient.post(this.sharedService.sshkeyUrl, data);
    }

  getAllVmGroup(){
    return this.httpClient.get(
      this.sharedService.vmGroup 
    );
  }

  getAssociatedVmGroup(resourceGroupName: string){
    return this.httpClient.get(
      this.sharedService.vmGroup + resourceGroupName
    );
  }
  
  getOSType(os: any){
    return this.httpClient.get(
      this.sharedService.osType + os
    );
  }
  storeVmData(data:any) {
    return this.httpClient.post(this.sharedService.createVmData + data.length, data);
  }

  stopVM(resourceGroupName: string,VmName:string){
    return this.httpClient.post(this.sharedService.stopvm + resourceGroupName + '&myVM=' + VmName,{});
  }

  startVM(resourceGroupName: string,VmName:string){
    return this.httpClient.post(this.sharedService.startvm + resourceGroupName + '&myVM=' + VmName,{},{responseType: 'text'});
  }

  restartVM(resourceGroupName: string,VmName:string){
    return this.httpClient.post(this.sharedService.restartvm + resourceGroupName + '&myVM=' + VmName,{});
  }


  deleteVM(resourceGroupName: string,VmName:string){
    return this.httpClient.delete(this.sharedService.deletevm + resourceGroupName + '&myVM=' + VmName,{});
  }

  resizeVM(resourceGroupName: string,VmName:string,size:string){
    return this.httpClient.put(this.sharedService.resizevm + resourceGroupName + '&myVM=' + VmName + '&size=' + size,{});
  }

  getvm(resourceGroupName: string,VmName:string){
    return this.httpClient.get(this.sharedService.getvm + resourceGroupName + '&myVm=' +VmName);
    }
    
  getVMSshKey(resourceGroupName: string, sshPublickKeyName: string) {
    return this.httpClient.get(this.sharedService.getSSHKey + resourceGroupName + '&sshPublicKeyName=' + sshPublickKeyName,{});
  }

  addDisktoVM(resourceGroupName: string,VmName:string,lun:number,gb:number,type:string,storageType:string,diskName:any){
    return this.httpClient.put(this.sharedService.addDiskSize + resourceGroupName + '&myVM=' + VmName  + '&lun=' + lun + '&gb=' + gb + '&disk_cachingType=' + type + '&storageAccountTypes=' +storageType + '&diskName=' + diskName,{});
  }

  detachDisktoVM(resourceGroupName: string,VmName:string,lun:number,){
    return this.httpClient.delete(this.sharedService.detachdisk + resourceGroupName + '&myVM=' + VmName  + '&lun=' + lun,{});
  }

  createNSG(data:any) {
    return this.httpClient.post(this.sharedService.createNSG, data);
  }

  addNewNsgRule(data:any) {
    return this.httpClient.put(this.sharedService.addNewNsgRule, data);
  }

  getALLNSGList(){
    return this.httpClient.get(this.sharedService.NSGGroup);
  }

  getAssociatedSubnet(vnet:any,resourceGroupName: string,nsgName:string){
    return this.httpClient.get(this.sharedService.associatedSubnet + vnet + '&resourceGroupName='  + resourceGroupName + '&activeFlag=N&nsgName=' + nsgName);

  }
  getSubnetListforNSG(resourceGroupName: string,nsgName:string){
    return this.httpClient.get(this.sharedService.NSGSubnetDetails + resourceGroupName + '&nsg=' + nsgName);
  }

  getAssociatedNSGList(resourceGroupName: string){
    return this.httpClient.get(this.sharedService.NSGGroup + resourceGroupName);
  }

  associatedNSG(resourceGroupName: string,Vnet:string,subnet:string,nsg:string){
    return this.httpClient.put(this.sharedService.associatensg + resourceGroupName + '&vNetName=' + Vnet + '&subnetName=' + subnet + '&nsg=' + nsg,{});
  }

  disassociatedNSG(resourceGroupName: string,Vnet:string,subnet:string){
    return this.httpClient.put(this.sharedService.disassociatensg + resourceGroupName + '&vNetName=' + Vnet + '&subnetName=' + subnet,{});
  }

  deleteNsgRule(resourceGroupName: string,nsg:string,rule:string){
    return this.httpClient.delete(this.sharedService.deleteRule + resourceGroupName + '&&myNsg=' + nsg + '&ruleName=' + rule);
  }

  getNsgRule(resourceGroupName: string,nsg:string){
    return this.httpClient.get(this.sharedService.getnsgrules + resourceGroupName + '&networkSecurityGroupName=' + nsg);
  }

  updateNsgRule(data:any){
    return this.httpClient.put(this.sharedService.updatensgrule ,data);
  }

  getdockerimages(){
    return this.httpClient.get(this.sharedService.dockerimages)
   }
  importdockerimage(acrName:string,version:string,image:any){
   return this.httpClient.post(this.sharedService.importdockerimage + acrName + '&version=' + version + '&image=' +image,'')
  }

  importprivatedockerimage(acrName:string,version:string,image:any,userName: string,password:string){
    return this.httpClient.post(this.sharedService.importprivatedockerimage + acrName + '&version=' + version + '&image=' +image + '&userName='+ userName +'&password='+password,'')
  }

  importacrimage(acrResourceName:string,version:string,image:any,destinationAcrName:string){
    return this.httpClient.post(this.sharedService.importacrimage + acrResourceName + '&version=' + version + '&imageName=' + image + '&destinationAcrName=' +destinationAcrName,'')

  }

  deleteacrimages(acrName:string,imageName:string){
    return this.httpClient.delete(this.sharedService.deletedocimage + acrName + '&imageName=' + imageName);
  }

  deleteacr(resourceGroupName:string,acrName:string){
    return this.httpClient.delete(this.sharedService.deleteacr + resourceGroupName + '&registryName=' + acrName);
  }

  getimagetags(acrName:any,image:any){
    return this.httpClient.get(this.sharedService.acrimagetag + acrName + '&image=' + image);
  }
  enableKeystoreForAks(resourceGroupName:string,aksName:string){
    return this.httpClient.put(this.sharedService.enableKeystoreForAks + resourceGroupName + '&aksName=' + aksName,{});

  }

  disableKeystoreForAks(resourceGroupName:string,aksName:string){
    return this.httpClient.put(this.sharedService.disableKeystoreForAks + resourceGroupName + '&aksName=' + aksName,{});
  }
  createUserMangData(data: { userName: string; email:string; passWord:string ;firstName:string;lastName:string; contactNumber:string; department:string; role:any}) {
    return this.httpClient.post(this.sharedService.createUserMangData, data);
  }

  getRoles(){
    return this.httpClient.get(this.sharedService.getRoles);
  }

  getuserlist(){
    return this.httpClient.get(this.sharedService.getuserlist);
  }
  edituser(data: {userName: string; email:string;firstName:string;lastName:string; contactNumber:string; department:string; role:any; id:any;}) {
    return this.httpClient.put(this.sharedService.edituser, data);
  }
  deleteusermang(Id: any){
    return this.httpClient.delete(this.sharedService.deleteusermang+Id);
  }
  usernamevalidation(username:string){
    return this.httpClient.get(this.sharedService.usernamevalidation+ username);
  }
  emailvalidation(email:string){
    return this.httpClient.get(this.sharedService.emailvalidation+email);
  }

  unlockuser(userid:string){
    return this.httpClient.get(this.sharedService.unlockuser + userid);
  }
  lockuser(userid:string){
    return this.httpClient.post(this.sharedService.lockuser + userid ,{});
  }

  loginstatus(userid:any,token:any){
    return this.httpClient.get(this.sharedService.validateloginstatus + userid  + '&jwtToken=' + token);
  }

  logout(userId:any){
    return this.httpClient.get(this.sharedService.logout + userId );

  }

  getprojectList(token:any,org:any){
    return this.httpClient.get(this.sharedService.projectList + token + '&organization=' + org);
  }

  getorgnizationList(token:any){
    return this.httpClient.get(this.sharedService.organizationList + token);
  }

  getpipelineList(token:any,org:any,projectName:any){
    return this.httpClient.get(this.sharedService.pipelinelist + token + '&organization=' + org + '&projectName=' + projectName);
  }
  getreleaseList(projectName:any,org:any,token:any){
   
    return this.httpClient.get(this.sharedService.releaseList + projectName + '&organization=' + org + '&token=' + token);
  }
  getreleaselog(org:any,token:any,releaseId:any,projectName:any){
   
    return this.httpClient.get(this.sharedService.releaselog  + org +  '&token=' + token +  '&releaseId=' +  releaseId + '&projectName=' + projectName );
  }
  deploystages(org:any,token:any,releaseId:any,projectName:any,stageId:any){
    
    return this.httpClient.patch(this.sharedService.deploystage + org +  '&token=' + token +  '&releaseId=' +  releaseId + '&projectName=' + projectName + '&stageId=' + stageId,{});
  }

  getstagesList(org:any,token:any,projectName:any,releaseId:any){
  
    return this.httpClient.get(this.sharedService.getstageslist + org +  '&token=' + token + '&projectName=' + projectName + '&releaseId=' +  releaseId);
    
  }


  deletepipelineList(projectName:any,org:any,token:any,definitionId:any){
    return this.httpClient.delete(this.sharedService.deletepipeline + projectName + '&organization=' + org + '&token=' + token + '&definitionId=' + definitionId);
  }
  
  runpipelineList(projectName:any,pipelineId:any,org:any,token:any,branch:any){
    return this.httpClient.post(this.sharedService.runpipeline + projectName + '&pipelineId=' + pipelineId + '&organization=' + org + '&token=' + token + '&branch=' +branch ,{});
  }

  repoList(projectName:any,pipelineId:any,org:any,token:any,){
    return this.httpClient.get(this.sharedService.repolist + projectName + '&pipelineId=' + pipelineId + '&organization=' + org + '&token=' + token );
  }
  getpipelinestatus(projectName:any,pipelineId:any,org:any,token:any,runId:any){
    return this.httpClient.get(this.sharedService.pipelinestatus + projectName + '&pipelineId=' + pipelineId + '&organization=' + org + '&token=' + token + '&runId=' + runId);
  }
  renamepipeline(projectName:any,org:any,token:any,pipelineId:any,newName :any,oldname:any){
    return this.httpClient.post(this.sharedService.rename + projectName +  '&organization=' + org + '&token=' + token + '&definitionId=' + pipelineId + '&newName=' + newName + '&oldName=' + oldname ,{});
  }
  getdevopstoken(username:any){
    return this.httpClient.get(this.sharedService.devopstoken + username);
  }
  storedevopstoken(token:any,username:any){
    return this.httpClient.post(this.sharedService.storetoekn + token + '&username=' + username,{});
  }
  checkdevopstoken(token){
    return this.httpClient.get(this.sharedService.checktoken + token);
  }
  checkSpecificOrgToken(token:any,org:any){
    return this.httpClient.get(this.sharedService.checkSpecificOrgToken + token +  '&organization=' + org );
  }
  getpipelinelogs(org:any,token:any,projectName:any,runId:any,pipelineId:any){
    return this.httpClient.get(this.sharedService.getAllBuildLogsList + token);
  }

  getAllBuildLogsList(token:any,projectName:any,org:any,){
    return this.httpClient.get(this.sharedService.getAllBuildLogsList + token + '&projectName='+projectName + '&organization=' + org);
  }
  
  getAllLogsOfBuild(token:any,buildId:any,projectName:any,org:any){
    return this.httpClient.get(this.sharedService.getAllLogsOfBuild + token + '&buildId=' + buildId + '&projectName='+projectName + '&organization=' + org);
  }

  getLogsById(token:any,buildId:any,logId:any,projectName:any,org:any){
    return this.httpClient.get(this.sharedService.getLogsById + token + '&buildId=' + buildId + '&logId=' + logId + '&projectName='+projectName + '&organization=' + org);
  }
  getAllReleaseTasklog(token:any,releaseId:any,projectName:any,org:any ,environmentID :any,deployPhasesId:any){
    return this.httpClient.get(this.sharedService.getAllReleaseTasklog + token + '&releaseId=' + releaseId +  '&projectName='+projectName  + '&organization=' + org + '&environmentID=' + environmentID + '&deployPhasesId='+ deployPhasesId );
  }

   getReleaseLogListById(token:any,releaseId:any,projectName:any,org:any){
    return this.httpClient.get(this.sharedService.getReleaseLogListById + token + '&releaseId=' + releaseId +  '&projectName='+projectName + '&organization=' + org);
  }
  getReleaseLogTaskById(token:any,releaseId:any,projectName:any,environmentID :any,deployPhasesId:any,taskId:any,org:any){
    return this.httpClient.get(this.sharedService.getReleaseLogTaskById + token + '&releaseId=' + releaseId +  '&projectName='+projectName   + '&environmentID=' + environmentID + '&deployPhasesId='+ deployPhasesId + '&taskId=' +taskId + '&organization=' + org);
  }

  getPipelineReleaseLog(token:any,defintionId:any,projectName:any,org:any){
    return this.httpClient.get(this.sharedService.pipelinereleaselog + token + '&definitionId=' + defintionId +  '&projectName='+projectName   +  '&organization=' + org);
  }

  
  getAllreleaseDefinition(token:any,projectName:any,org:any){
    return this.httpClient.get(this.sharedService.getAllreleaseDefinition + token +  '&projectName='+projectName   +  '&organization=' + org);
  }
  downloadtemplate(resource:any,count:any){
    return this.httpClient.get(this.sharedService.tempdownload + resource + '&count=' + count);

  }

  getsecretlist(){
    return this.httpClient.get(this.sharedService.getsecretlist);
  }

  savedsubscrption(details:any){
    return this.httpClient.post(this.sharedService.savesubscription,details);
  }

  deletesubscrption(secureId:any){
    return this.httpClient.delete(this.sharedService.removesubscription + secureId);
  }

  updatesubscrption(secureId:any,azureList:any){
    return this.httpClient.put(this.sharedService.updatesubscription + secureId,azureList);
  }

  enablesubscrption(secureId:any){
    return this.httpClient.put(this.sharedService.enablesubscription + secureId,{});
  }
  
  logindockerhub(userName: string,password:string){
    return this.httpClient.post(this.sharedService.logindockerhub + userName +'&password='+ password,{});
   
  }
  getusernamespacedocker(userNameSpace:string, token:any){
    return this.httpClient.get(this.sharedService.usernamespacedocker + userNameSpace  + '&token=' + token); 
  }
  getprivatedockerimages(userNameSpace:string, token:any,repositories:any){
    return this.httpClient.get(this.sharedService.privatedockerimages + userNameSpace  + '&token=' + token +'&repositories='+repositories);
  }

  gettagdockerimages(image:any){
    return this.httpClient.get(this.sharedService.dockerimagetags + image);
  }
  
}
