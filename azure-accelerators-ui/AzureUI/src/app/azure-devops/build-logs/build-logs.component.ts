import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from 'src/app/services/admin.service';
import { DevopsService } from 'src/app/services/devops.service';
import { SharedServiceService } from 'src/app/services/shared-service.service';

@Component({
  selector: 'app-build-logs',
  templateUrl: './build-logs.component.html',
  styleUrls: ['./build-logs.component.css']
})
export class BuildLogsComponent implements OnInit {
  sourceProjectName = '';
  org = '';
  sorcedata :any;
  displayEmptyRow :boolean;
  associatedlogs = []
  associatedRepoGroup = []
  pipelineForm:FormGroup;
  pipelinerenameForm:FormGroup
  config:any
  pipelineId =''
  pipelinename =''
  pipelinedata:any;
  enablebuildlog = false;
  enablelog = true;
  associatedbuildlog = []
  buildId =''
  logs = []
  buildlogs = false;
 
 
  constructor(private devopsSevice:DevopsService, private sharedService: SharedServiceService,private adminService:AdminService,private _formBuilder:FormBuilder) { 


  }

  ngOnInit(): void {
    
    this.sorcedata = this.devopsSevice.getSourceProject();
    this.sourceProjectName = this.sorcedata.pro;
    this.org = this.sorcedata.org;
    this.pipelinedata = this.devopsSevice.getPipelineDetails();
    this.pipelinename = this.pipelinedata.name;
    this.pipelineId = this.pipelinedata.id;
   
   
   
   
  this.showbuildlogs()
  }

  showbuildlogs(){
    this.adminService
    .getAllBuildLogsList(this.devopsSevice.token,this.sourceProjectName,this.org)
    .subscribe((res:any) => {
      // this.associatedlogs = res.value
      
      this.associatedlogs =  this.filterlogs(res.value)
      this.associatedlogs = this.datechange(this.associatedlogs)
    });
   }
 filterlogs(res:any){
  let data = res.filter(
    (logs) =>
    logs?.definition?.name == this.pipelinename,
    

      // &&
      // s.addressSpace !== this.sourceVnet.addressSpace
  );
  return data;
 }
 datechange(data:any){
  data.forEach(element => {
    if(element.hasOwnProperty('finishTime')){
      element.finishTime = new Date(element.finishTime).toLocaleString(undefined, {timeZone: 'Asia/Kolkata'});
    }else{
      element['finishTime'] = 'Run in progress'
    }
  
  });
  return data;
 }
   getIDlogs(logs:any){
    this.buildId = logs.id;
    this.adminService
    .getAllLogsOfBuild(this.devopsSevice.token,logs.id,this.sourceProjectName,this.org)
    .subscribe((res:any) => {
      
      res.value.forEach(element => {
       
        element.createdOn =  new Date(element.createdOn).toLocaleString(undefined, {timeZone: 'Asia/Kolkata'});
    
      
      });
      this.associatedbuildlog = res.value.reverse();
      this.enablebuildlog = true;
      this.enablelog = false;
     
    });
   }
   showlogs(logs:any){
    this.logs = []
    this.adminService
    .getLogsById(this.devopsSevice.token,this.buildId,logs.id,this.sourceProjectName,this.org)
    .subscribe((res:any) => {
  this.buildlogs = true
      this.logs =res.response.split("\n");
    });
   }
}