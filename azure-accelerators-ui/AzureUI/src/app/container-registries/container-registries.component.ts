import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { ToastModel } from '../models/toast.model';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';
import { LoginServiceService } from '../services/login-service.service';

@Component({
  selector: 'app-container-registries',
  templateUrl: './container-registries.component.html',
  styleUrls: []
})
export class ContainerRegistriesComponent implements OnInit {
  ContainerRegistryForm: FormGroup;
  createRegistryForm: FormGroup;
  usernmaespaceForm:FormGroup;
  privatedockerimageForm:FormGroup
  importACRForm :FormGroup;
  importDockerForm:FormGroup;
  resourceGroupName = '';
  loadingMsg = '';
  associatedResourceGroup: any = [];
  listOfRegistries: any = [];
  config: any;
  config1:any;
  tagconfig:any;
  dockerprivateconfig:any;
  dockerprivateconfigimg:any;
  dockerprivateconfigtags:any;
  resconfig:any
  locationConfig:any
  acrconfig:any;
  dockerconfig:any
  acrimageconfig:any
  disableAddbtn: boolean = true;
  containerName: string;
  repositoryList: any = [];
  showRgModal: boolean = false;
  createdResGrpName: string;
  isRepoEmpty: boolean = false;
  displayEmptyRow: boolean;
  invalidregistryName:boolean;
  uniqueNameErrMsg: string;
  regsitryname:string;
  destinationAcr = [];
  radioType =[
    { id: 200, name: 'ACR to ACR' },
    { id: 100, name: 'Dockerhub- Public' },
    // { id: 300, name: 'Dockerhub- Private' }
 
  ]
  acrImageFlag = true;
  dockerImageFlag =false;
  loginflag=false;
  radioSelected:number;
  associatedLocations : any=[]
  sizeconfig:any;
  associatedSize :any=["Basic","Standard","Premium"]
  selectedLocation =''
  dockerImageList =[];
  acrtagsList :any
  repoprivatelist:any =[]
  imageprivatelist:any=[]
  tagsprivatelist:any[]
  resourceACR =''
  acrimageList:any;
  refreshTime = ''
  logindockerForm: any = {};
  isShow: boolean=true;
  isdisplay: boolean=true;
  token:any= {}
  tokenpriavtedocker:any={}
  privatedockerusername=''
  priavtedockerpassword=''
  dockerimagetagconfig :any;
  dockerimagetagsList = []
  constructor(
    private _formBuilder: FormBuilder,
    private adminService: AdminService,
    private spinner: NgxSpinnerService,
    private toastr: ToastrService,
    public sharedService: SharedServiceService,
    private loginService: LoginServiceService,
    public datepipe : DatePipe
  ) {
    this.radioSelected = 200;
    this.getLocations()
  }

  ngOnInit(): void {
    this.getResourcegroup();
    this.getdockerimages()

    this.ContainerRegistryForm = this._formBuilder.group({
      resourceGroup: [''],
    });

    this.createRegistryForm = this._formBuilder.group({
      registryName: 
      [
        '',
        [Validators.required, Validators.minLength(5), Validators.maxLength(50)],
      ],
      resourceGroup: [{value:'', disabled: true}],
      location: ['', Validators.required],
      size: ['', Validators.required],
    });
    this.importACRForm = this._formBuilder.group({
    
      destiresourceGroup: ['', Validators.required],
      acrimage: ['', Validators.required],
      destinationACR: ['', Validators.required],
      acrtags:['',Validators.required]

    });
    this.importDockerForm = this._formBuilder.group({
    
      dockerimage: ['', Validators.required],
      tags:['',[
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)
    ]]

    });
    this.logindockerForm = this._formBuilder.group({
    
      username:['', Validators.required],
      password:['', Validators.required],

    });

    this.usernmaespaceForm=this._formBuilder.group({
      repositoryname:['', Validators.required]

    });
    this.privatedockerimageForm=this._formBuilder.group({
      privatedockerimg:['', Validators.required],
      privatedockertag:['', Validators.required]

    });
    this.config = {
      displayKey: 'description',
      search: true,
      height: '350px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.config1 = {
      displayKey: 'description',
      search: true,
      height: '200px',
      placeholder: 'Select ACR Image',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.tagconfig = {
      displayKey: 'description',
      search: true,
      height: '200px',
      placeholder: 'Select Tags',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.dockerimagetagconfig ={
      displayKey: 'description',
      search: true,
      height: '200px',
      placeholder: 'Select Tags',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    }
    this.locationConfig = {
      displayKey: 'description',
      placeholder: 'Select Location',
      search: true,
      height: '200px',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search Location',
      clearOnSelection: true,
    };
    this.sizeconfig={
      displayKey: 'description',
      placeholder: 'Select Size',
      search: true,
      height: '200px',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search Size',
      clearOnSelection: true,
    }
    this.resconfig = {
      displayKey: 'description',
      search: true,
      height: '200px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.acrconfig = {
      displayKey: 'name',
      search: true,
      height: '200px',
      placeholder: 'Select ACR Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.dockerconfig = {
      displayKey: 'name',
      search: true,
      height: '200px',
      placeholder: 'Select Docker Image',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.dockerprivateconfig={
      displayKey: 'name',
      search: true,
      height: '200px',
      placeholder: 'Select Repository',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,

    }
    this.dockerprivateconfigimg={
      displayKey: 'digest',
      search: true,
      height: '200px',
      placeholder: 'Select Docker Image',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,

    }
    this.dockerprivateconfigtags={
      
      displayKey: 'tag',
      search: true,
      height: '200px',
      placeholder: 'Select Tag',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,

    }

    this.toastr.clear();
  }

  get registryCreationFormControl() {
    return this.createRegistryForm.controls;
  }

  get logindockercreateform(){
    return this.logindockerForm.controls;
  }

  getResourcegroup() {
    this.adminService.getAzureResourceGroupList().subscribe((res) => {
       //console.log("resource",res)
      this.associatedResourceGroup = res;
    });
  }

  getdockerimages() {
    this.adminService.getdockerimages().subscribe((res:any) => {
      this.dockerImageList = res;
    });
  }

  selectResourceGroup() {
    this.disableAddbtn = false;
    this.resourceGroupName =
      this.ContainerRegistryForm.controls['resourceGroup'].value;
    this.adminService
      .getContainerRegistryList(this.resourceGroupName)
      .subscribe((res) => {
        this.listOfRegistries = res;
        this.displayEmptyRow = this.listOfRegistries.length == 0 ? true : false;
      });
  }

  addReg() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.showRgModal = true;
    }
  }
  closeResourceGroupModal() {
    this.showRgModal = false;
  }
  resourceGrpResponse() {
    this.createdResGrpName = this.sharedService.getResourceGroup();
    this.ContainerRegistryForm.patchValue({
      resourceGroup: this.createdResGrpName,
    });
    this.selectResourceGroup();
  }
  showRepoDetails(registryObj) {
    
    this.containerName =registryObj.name;
    this.loadingMsg = 'Loading Repositories...';
    this.spinner.show('modalSpinner');
    this.adminService.getRepositoryList(registryObj.name).subscribe(
      (res) => {
        this.spinner.hide('modalSpinner');
        this.repositoryList = this.sortImages(res);
        this.acrimageList = res;
        //console.log('aa',this.repositoryList)
        this.isRepoEmpty = this.repositoryList.length == 0 ? true : false;
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide('modalSpinner');
        this.sharedService.showFail(error.error.message);
      }
    );
    console.log('selectDockerimagetag',this.repositoryList)
  }

  sortImages(res:any){
    let temparray =[];
  
   res.forEach(obj => {
    let tempobj={
      "name":"",
      "showtag":false,
      "tag":[],
      "load":false
    }
     tempobj.name = obj
     temparray.push(tempobj);
   });
   return temparray;
  }
  


  showtag(rep:any,index:any){
    ;
  if(rep.showtag){
    rep.showtag = false;
  }else{
    rep.load = true;
    this.adminService.getimagetags(this.containerName,rep.name).subscribe(
      (res) => {
        this.spinner.hide('tagspinner');
        this.repositoryList[index].tag=res;
        rep.load = false;

         this.repositoryList[index].showtag =true;
      
      },
      (error: HttpErrorResponse) => {
        rep.load = false;

        this.spinner.hide('tagspinner');
        this.sharedService.showFail(error.error.message);
      }
    );
  }
  }
  selectACRimage(data:any){
//  this.acrtagsList = data.value.tag;
let acrname = this.importACRForm.controls['destinationACR'].value.name;
this.getImageTags(acrname,data.value)
  }

getImageTags(acrname:any,image:any){
  this.loadingMsg = 'Loading Tags...';
  this.spinner.show('modalSpinner');
  this.adminService.getimagetags(acrname,image).subscribe(
    (res) => {
      this.spinner.hide('modalSpinner');
      this.acrtagsList = res;
   
    },
    (error: HttpErrorResponse) => {
      this.spinner.hide('modalSpinner');
      this.sharedService.showFail(error.error.message);
    }
  );
}

// Only AlphaNumeric
keyPressAlphanumeric(event) {

  var inp = String.fromCharCode(event.keyCode);

  if (/[a-zA-Z0-9]/.test(inp)) {
    return true;
  } else {
    event.preventDefault();
    return false;
  }
}
  createRegistry() {
    this.loadingMsg = 'Creating...';
    this.spinner.show('fullSpinner');
    let reqObj = {
      name: this.createRegistryForm.controls['registryName'].value,
      resourceGroupName: this.resourceGroupName,
      region:this.selectedLocation,
      acrSize:this.createRegistryForm.controls['size'].value

    };
    this.adminService.createRegistry(reqObj).subscribe(
      (res) => {
        this.spinner.hide('fullSpinner');
        this.sharedService.showSuccess(
          reqObj.name + ' Container Registry successfully created!!'
         
        );
        this.regsitryname=reqObj.name;
        this.selectResourceGroup();
        
        this.createRegistryForm.reset();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide('fullSpinner');
        // this.createRegistryForm.reset();
        this.sharedService.showFail(error.error.message);
      }
    );
  }
  createRegi(){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.resourceGroupName = this.ContainerRegistryForm.controls['resourceGroup'].value;
      this.createRegistryForm.patchValue({
        'resourceGroup':this.resourceGroupName
      });
    }
  }
  isUnique(data:any) {
    
    const registryName: string = this.createRegistryForm.controls['registryName'].value;
    if (registryName != null) {
      let isNamePresent = this.listOfRegistries.find(
        (element) => element.name.toLowerCase() === registryName.toLowerCase()
      );
      this.invalidregistryName = isNamePresent == undefined ? false : true;
      this.uniqueNameErrMsg = registryName + ' is already present';
    }
  }

  importImages(registryObj){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.importACRForm.reset()
      this.importDockerForm.reset()
      this.logindockerForm.reset()
      this.resourceACR  = registryObj.name;

      //this.showRepoDetails(registryObj) 
    }
  }

  selectACRImages(){

  }

  selectDestiResourceGroup(){
    this.disableAddbtn = false;
    this.resourceGroupName =
      this.importACRForm.controls['destiresourceGroup'].value;
      this.importACRForm.patchValue({
        destinationACR: '',
        acrimage:'',
        acrtags:''
      })
    this.adminService
      .getContainerRegistryList(this.resourceGroupName)
      .subscribe((res:any) => {
        
        this.destinationAcr =this.removeSameACR(res);
        //this.displayEmptyRow = this.destinationAcr.length == 0 ? true : false;
      });
  }

  removeSameACR(data:any){
    
  data.forEach((element:any,index:number) => {
    if(element.name == this.resourceACR){
      data.splice(index,1)
    }
  });
  return data;
  }
  selectDestiacr(data:any){
    
  this.showRepoDetails(data.value)

  }

  loadDestinationAcr(){

  }

  onChangeEvent(data:any){
    
    if(data.id == 200){
      this.acrImageFlag = true;
      this.dockerImageFlag = false;
      this.loginflag=false;
    }
    else if(data.id == 300){
      this.acrImageFlag = false;
      this.dockerImageFlag = false;
      this.loginflag=true;
    }
    
    else{
      this.acrImageFlag = false;
      this.dockerImageFlag = true;
      this.loginflag=false;
    }
    //console.log(data)
  }

  importdockerimage(){
    this.loadingMsg = 'Importing...';
    // this.spinner.show('fullSpinner');
    let messageContent: ToastModel = {
      sticky: true,
      severity: 'info',
      summary: 'Importing Container Images',
      detail: 'Importing Container Images is in progress and this may take a while.',
      life: false
    }
    this.sharedService.messageService$.emit(messageContent);
  
      let image = this.importDockerForm.controls['dockerimage'].value.name.toLowerCase();
      let version = this.importDockerForm.controls['tags'].value;
    
    this.adminService.importdockerimage(this.resourceACR,version,image).subscribe(
      (res) => {
        messageContent = {
          sticky: false,
          severity: 'success',
          summary: 'Importing image is successful',
          detail: 'Importing image is successful',
          life: true
        }
        this.sharedService.messageService$.emit(messageContent);
      },
      (error: HttpErrorResponse) => {
        //console.log("I am in error...->");
        let regex = new RegExp("(conflict).*?(Tag.*?)+$","imsg");
        let match = regex.exec(error.error.message);
        if(match && match.length == 3){
          this.sharedService.showWarning(match[2]);
        }else{
          messageContent = {
            sticky: true,
            severity: 'error',
            summary: 'Error While importing the image',
            detail: error.error.message,
            life: true
          }
          this.sharedService.messageService$.emit(messageContent);
        } 
      }
    );

  }

  importacrimage(){
    this.loadingMsg = 'Importing...';
    let messageContent: ToastModel = {
      sticky: true,
      severity: 'info',
      summary: 'Importing Container Images',
      detail: 'Importing Container Images is in progress and this may take a while.',
      life: false
    }
    this.sharedService.messageService$.emit(messageContent);
  
    let image = this.importACRForm.controls['acrimage'].value;
    let destinationacr = this.importACRForm.controls['destinationACR'].value.name;
    let tag= this.importACRForm.controls['acrtags'].value
    
    this.adminService.importacrimage(destinationacr,tag,image,this.resourceACR).subscribe(
      (res) => {100
        messageContent = {
          sticky: false,
          severity: 'success',
          summary: 'Importing image is successful',
          detail: 'Importing image is successful',
          life: true
        }
        this.sharedService.messageService$.emit(messageContent);
      },
      (error: HttpErrorResponse) => {
        let regex = new RegExp("(conflict).*?(Tag.*?)+$","imsg");
        let match = regex.exec(error.error.message);
        if(match && match.length == 3){
          this.sharedService.showWarning(match[2]);
        }else{
          messageContent = {
            sticky: false,
            severity: 'error',
            summary: 'Error While importing the image',
            detail: error.error.message,
            life: true
          }
          this.sharedService.messageService$.emit(messageContent);
        }
      }
    );

  }

  

  import(){
    if(this.dockerImageFlag){
      this.importdockerimage()
    }
     else if(this.loginflag){
      this.logindocker()
     }
    else{
      this.importacrimage();
    }
  }

  delete(rep:any){
    
    this.spinner.show('modalSpinner');
    this.loadingMsg = 'Deleting ...';
    this.adminService.deleteacrimages(this.containerName,rep.name).subscribe(
      (res) => {
        this.sharedService.showSuccess(
          'deleted successfully'
        );
        this.spinner.hide('modalSpinner');
        let obj= {
          'name':this.containerName
        }
        this.showRepoDetails(obj)
        return;

        
      },
      (error: HttpErrorResponse) => {
        
        this.spinner.hide('modalSpinner');
        this.sharedService.showFail(error.error.message);
      }
    );

  }
  deleteacr(registry){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.spinner.show('fullSpinner');
      this.loadingMsg = 'Deleting ...';
      this.adminService.deleteacr(this.resourceGroupName,registry.name).subscribe(
        (res) => {
          this.sharedService.showSuccess(
            this.regsitryname + ' deleted successfully'
          );
          this.spinner.hide('fullSpinner');
          let obj= {
            'name':this.containerName
          }
        this.adminService
          .getContainerRegistryList(this.resourceGroupName)
          .subscribe((res) => {
            this.listOfRegistries = res;
            this.displayEmptyRow = this.listOfRegistries.length == 0 ? true : false;
          });    
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide('modalSpinner');
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }
  getLocations() {
    this.adminService.getAksLocationList().subscribe((res) => {
      this.associatedLocations = res;
    });
  }

  selectLocation() {
    this.selectedLocation =
      this.createRegistryForm.controls['location'].value;
  }


  selectSize(){

  }

  onRefreshClick(){
    this.resourceGroupName =
      this.ContainerRegistryForm.controls['resourceGroup'].value;
    this.adminService
      .getContainerRegistryList(this.resourceGroupName)
      .subscribe((res) => {
        this.listOfRegistries = res;
        this.displayEmptyRow = this.listOfRegistries.length == 0 ? true : false;
        var refreshTime = new Date();
        this.refreshTime = 'As of ' + this.datepipe.transform(refreshTime, 'MM/dd/yyyy h:mm a')
      });
  }


  // code for private docker login
  logindocker() {
      let userName=this.logindockerForm.controls['username'].value;
       let passowrd= this.logindockerForm.controls['password'].value;
        //this.isdisplay=false;
      this.adminService.logindockerhub(userName,passowrd).subscribe(
        (data:any) => {
          this.sharedService.showSuccess( ' logged in successfully');
          this.tokenpriavtedocker=data.token;
        
          this.adminService.getusernamespacedocker(userName,data.token).subscribe(
            (res:any) => {
             this.repoprivatelist= res.results;
             this.privatedockerusername=userName;
             this.priavtedockerpassword=passowrd;
        
            },
            (error: HttpErrorResponse) => {
              this.sharedService.showFail(error.error.message);
            }
          );
         this.isdisplay=false;    
          this.isShow = false;
        },
        (error: HttpErrorResponse) => {
          //console.log("error");
          this.sharedService.showFail(error.error.message);
        }
      );
    
  }

  
  // code for private docker repository 
  repositoryselection(value: string){
    let repositoryname=this.usernmaespaceForm.controls['repositoryname'].value;
   
    let userName=this.logindockerForm.controls['username'].value;
    this.adminService.getprivatedockerimages(userName,this.tokenpriavtedocker,repositoryname.name).subscribe(
            (res:any) => { 
                                   
              this.imageprivatelist=res.results;
              
        
            },
            
           
            (error: HttpErrorResponse) => {
              this.sharedService.showFail(error.error.message);
            }
          );
  }

  // code for private docker image and tag
  privatedocimg(){
    let privatedocker=this.privatedockerimageForm.controls['privatedockerimg'].value;
    this.tagsprivatelist=privatedocker.tags;
  }
  
// code for import button of private docker login

  imporprivatetdockerimage(){
    this.loadingMsg = 'Importing...';
    // this.spinner.show('fullSpinner');
    let messageContent: ToastModel = {
      sticky: true,
      severity: 'info',
      summary: 'Importing Container Images',
      detail: 'Importing Container Images is in progress and this may take a while.',
      life: false
    }
    this.sharedService.messageService$.emit(messageContent);
      let image = this.privatedockerimageForm.controls['privatedockerimg'].value;
      let tag = this.privatedockerimageForm.controls['privatedockertag'].value;
      let repositoryname=this.usernmaespaceForm.controls['repositoryname'].value;
          
    this.adminService.importprivatedockerimage(this.resourceACR,tag.tag,repositoryname.name,this.privatedockerusername,this.priavtedockerpassword).subscribe(
      (res) => {
        messageContent = {
          sticky: false,
          severity: 'success',
          summary: 'Importing image is successful',
          detail: 'Importing image is successful',
          life: true
        }
        //console.log("this.privatedockerusername-->",this.privatedockerusername);
       
        this.sharedService.messageService$.emit(messageContent);
      },
      (error: HttpErrorResponse) => {
        messageContent = {
          sticky: false,
          severity: 'error',
          summary: 'Error While importing the image',
          detail: error.error.message,
          life: true
        }
        this.sharedService.messageService$.emit(messageContent);
        
      }
    );

  }
  
  selectDockerimage(event:any){
//  this.acrtagsList = data.value.tag;
this.importDockerForm.patchValue({
  'tags':''
})
this.getDockerImageTags(event.value.name)
  }

  getDockerImageTags(image:any){
  //   this.loadingMsg = 'Loading Tags...';
  //   this.spinner.show();
    this.adminService.gettagdockerimages(image).subscribe(
      (res:any) => {
        // this.spinner.hide();
        this.dockerimagetagsList = res;
     
      },
      (error: HttpErrorResponse) => {
        // this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  selectDockerimagetag(data:any){

  }

  closeModal(){
    this.createRegistryForm.reset();
    this.uniqueNameErrMsg=" ";

  }
}
