import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DevopsService {
  private sourceProject: any;
  private org:any
  token:any
  private releaseId:any;
  private releasename:any;
  private pipelineId:any;
  private pipelinename:any;
  enableReleaseView = false;
  enablePipelineView = false;
  definitionId = ''
  constructor() { }
  saveSourceProject(org:any,pro:any) {
    this.sourceProject = pro;
    this.org = org;
}

getSourceProject() {
  let data = {
    "org":this.org,
    "pro":this.sourceProject

  }
    return  data;
}

getReleaseDetails() {
  let data = {
    "id":this.releaseId,
    "name":this.releasename

  }
    return  data;
}

saveRelease(releaseId:any,releasename:any) {
  this.releaseId = releaseId;
  this.releasename = releasename;
}

getPipelineDetails() {
  let data = {
    "id":this.pipelineId,
    "name":this.pipelinename

  }
    return  data;
}

savePipeline(pipelineId:any,pipelinename:any) {
  this.pipelineId = pipelineId;
  this.pipelinename = pipelinename;
}

saveDefinitionId(definitionId:any) {
  this.definitionId = definitionId;
  
}

getDefinitionId() {
  return this.definitionId;
  
}
}
