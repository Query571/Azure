import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../services/admin.service';
import { DevopsService } from '../services/devops.service';
import { SharedServiceService } from '../services/shared-service.service';

@Component({
  selector: 'app-azure-devops',
  templateUrl: './azure-devops.component.html',
  styleUrls: ['./azure-devops.component.css']
})
export class AzureDevopsComponent implements OnInit {
  associatedprojectList :any=[]
  displayEmptyRow:boolean;
  enablePipelineView = false;
  enableReleaseView = false;
  enabledevops = false;
  associatedOrgnazation = []
  orgconfig :any;
  org =''
  enabletokenscreen = false
 toeknform:FormGroup;
 tokenfield:boolean;
 radioType =[
  {  name: 'All Organization' },
  {  name: 'Specific Organization' }

]
enableorg = false;
config:any;
selectedorg =''
username = ''
enablelogsView = false;
  constructor( private _formBuilder: FormBuilder,
    private adminService: AdminService,private devopsService:DevopsService,
    private sharedService:SharedServiceService) { 
      this.username = sessionStorage.getItem('userName');
      this.orgconfig ={
        displayKey: 'accountName',
        placeholder: 'Select Organization',
        search: true,
        height: '200px',
        noResultsFound: 'No results found!',
        searchPlaceholder: 'Search Organization',
        clearOnSelection: true,
      };
      this.config ={
        displayKey: 'name',
        placeholder: 'Select Type',
        search: true,
        height: '200px',
        noResultsFound: 'No results found!',
        searchPlaceholder: 'Search Type',
        clearOnSelection: true,
      };

    }

  ngOnInit(): void {
    // this.getdevopstokenId()
   this.enabletokenscreen = true
    this.toeknform  = this._formBuilder.group({
      tokenid: ['',[
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)
      ]],
      type:['',Validators.required],
      org:['', Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)]
    

    });
    
  //   this.associatedprojectList =  [
  //     {
  //         "visibility": "private",
  //         "name": "ARM Templates Deployment",
  //         "description": "Selected ARM Templates Deployment Using Azure CI-CD Pipelines",
  //         "id": "42ea6e42-d51a-4c18-883a-2beb80e92985",
  //         "state": "wellFormed",
  //         "url": "https://dev.azure.com/rathody/_apis/projects/42ea6e42-d51a-4c18-883a-2beb80e92985",
  //         "revision": 19,
  //         "lastUpdateTime": "2022-06-28T13:21:02.187Z"
  //     },
  //     {
  //         "visibility": "private",
  //         "name": "AKS App Deployment",
  //         "id": "a875a125-d7e0-4118-9ea1-8bf8d3b6772e",
  //         "state": "wellFormed",
  //         "url": "https://dev.azure.com/rathody/_apis/projects/a875a125-d7e0-4118-9ea1-8bf8d3b6772e",
  //         "revision": 11,
  //         "lastUpdateTime": "2022-04-05T12:25:11.123Z"
  //     }
  // ]
  }

  getdevopstokenId(){
    this.adminService
    .getdevopstoken(this.username)
    .subscribe((res:any) => {
     if(res?.token && res.token != "null" && res.token != ''){
      this.devopsService.token=window.btoa(res.token);
      this.enabletokenscreen = false;
      this.enabledevops = true;
      this.enablePipelineView = false;
      this.devopsService.enablePipelineView = false;
      if(this.enableorg){
        this.selectedorg = this.toeknform.controls['org'].value;
        this.org = this.selectedorg;
this.getProjectList() 
      }else{
        this.getorganizationList();
      }
    
     }else{
      this.enabletokenscreen = true;
      this.enabledevops = false;
      this.enablePipelineView = false;
      this.devopsService.enablePipelineView = false;
     }
     
    }, (error: HttpErrorResponse) => {
      this.sharedService.showFail(error.error.message);
    });
  }

  checkSpecificOrgToken(){
    let token = window.btoa(this.toeknform.controls['tokenid'].value);
    let org = this.toeknform.controls['org'].value;
    this.adminService
    .checkSpecificOrgToken(token,org)
    .subscribe((res:any) => {
    this.storetoken()
    },(error: HttpErrorResponse) => {
      this.sharedService.showFail('Invalid Token or Organization');
    });
  }

  storetoken(){
    let token = window.btoa(this.toeknform.controls['tokenid'].value)  
    this.adminService
    .storedevopstoken(token,this.username)
    .subscribe((res:any) => {
     this.getdevopstokenId()
     
    },(error: HttpErrorResponse) => {
      this.sharedService.showFail(error.error.message);
    });
  }

  checkdevopstoken(){
    let token =window.btoa(this.toeknform.controls['tokenid'].value)   
    this.adminService
    .checkdevopstoken(token)
    .subscribe((res:any) => {
   if(res.displayName == '' || res.displayName == null){
    this.sharedService.showFail(
      'This token is invalid or not have enough permissions' 
     ); 
   }else{

    this.sharedService.showSuccess(
     'This token is generated in behalf  of ' + res.displayName
    ); 
    this.storetoken()
   }
     
    },(error: HttpErrorResponse) => {
      //console.log('errrr',error.error.status)
      this.sharedService.showFail(error.error.message);
    });
  }
  selectorg(event:any){
this.org = event.value.accountName;
this.getProjectList() 

  }
  getProjectList(){
    this.adminService
    .getprojectList(this.devopsService.token,this.org)
    .subscribe((res:any) => {
      this.associatedprojectList = res.value;
      this.displayEmptyRow =
        this.associatedprojectList.length == 0 ? true : false;
    },(error: HttpErrorResponse) => {
      this.sharedService.showFail(error.error.status);
    });
  }

  getorganizationList(){
    this.adminService
    .getorgnizationList(this.devopsService.token)
    .subscribe((res:any) => {
      this.associatedOrgnazation = res.value;
    },(error: HttpErrorResponse) => {
      //console.log('errrr',error.error.status)
      this.sharedService.showFail(error.error.message);
    });
  }
  showPipeline(pro:any){
  this.enablePipelineView = true;
  this.devopsService.enablePipelineView = true;
  this.enabledevops = false;
  this.devopsService.saveSourceProject(this.org,pro.name)
  }
  showRelease(pro:any){
    this.enableReleaseView = true
    this.devopsService.enableReleaseView = true;
    this.enablePipelineView = false;
    this.devopsService.enablePipelineView = false;
    this.enabledevops = false;
    this.devopsService.saveSourceProject(this.org,pro.name)
  }
  hidePipeline(){
    this.enablePipelineView = false;
    this.enabledevops = true;
  }

  hideRelease(){
    this.enablePipelineView = false;
    this.devopsService.enablePipelineView = false;
    this.enabledevops = true;
    this.enableReleaseView = false;
    this.devopsService.enableReleaseView = false;
  }
  onChangeEvent(event:any){
    if(event.value.name == "All Organization"){
      this.enableorg = false;
      this.toeknform.patchValue({
        org: '',
        tokenid: ''
      });
    }else{
      this.enableorg = true;
      this.toeknform.patchValue({
        org: '',
        tokenid: ''
      });
    }
  }

  validate(){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      if(!this.enableorg){
        this.checkdevopstoken();
      }else{
        // this.storetoken()
        this.checkSpecificOrgToken();
      }
    }
  }

  toggletokenfield(){
    this.tokenfield = !this.tokenfield;
   }
   showbuildlogs(pro:any){
  this.enableReleaseView = false
  this.devopsService.enableReleaseView = false;
  this.enablePipelineView = false;
  this.devopsService.enablePipelineView = false;
  this.enabledevops = false;
  this.enablelogsView = true
  this.devopsService.saveSourceProject(this.org,pro.name)
}
  hidelog(){
    this.enablelogsView = false;
    this.enabledevops = true;
  }
}
