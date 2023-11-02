import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';

@Component({
  selector: 'app-aks-cluster',
  templateUrl: './aks-cluster.component.html',
  styleUrls: ['./aks-cluster.component.css'],
})
export class AksClusterComponent implements OnInit {
  resourceClusterForm: FormGroup;
  aksClusterForm: FormGroup;
  addNodePoolForm: FormGroup;
  upgradeForm: FormGroup;
  scaleForm: FormGroup;
  dashboardForm: FormGroup;
  enableAddAksClusterForm: boolean = false;
  selectedResourceGroup: string;
  selectedLocation: string;
  selectedVersion: string;
  selectedMode: string;
  selectedVmSize: string;
  selectedOsType: string;
  associatedResourceGroup: any = [];
  associatedLocations: any = [];
  assosiateVmSizes: any = [];
  loadingMsg: string;
  aksClusterList: any = [];
  agentPoolMode: any = [];
  osType: any = [];
  nodePoolList: any = [];
  nodePoolListArray: any = [];
  displayTable: boolean = true;
  nodeDetails: boolean = false;
  details: any;
  viewDetails: boolean;
  config: any;
  locationConfig: any;
  modeConfig: any;
  osConfig: any;
  acrConfig:any;
  vmConfig: any;
  disableAddbtn: boolean = true;
  submitted: boolean = false;
  exportDisabled: boolean = true;
  enableAddNodePoolForm: boolean = false;
  aksId: any;
  exportData: any;
  multipleResourcesId: any = [];
  dynamicURL: string;
  aksName = '';
  upgradeAksName: string = '';
  kubernetesConfig: any;
  kubernetesVersion: any = [];
  showMessage: boolean;
  infoMessage: string = '';
  upgradeMessage: string = '';
  uniqueNameErrMsg: string = '';
  disableUpgrade: boolean;
  showRgModal: boolean = false;
  disableRefresh: boolean = true;
  loader: boolean = false;
  displayEmptyRow: boolean;
  createdResGrpName: string;
  nodePoolName = '';
  existingVmCount: any;
  dashboardObj: any;
  disRedirect: boolean;
  invalidNodeName: boolean;
  lengthErr: boolean = true;
  showDeleteModal: boolean;
  acrdata :any=[]
  showmsg = false;
  enablesecreteKey = false;
  enableACR = false;
  selectedModePool:string;
  nodepoolchar:number;
  refreshTime = '';
  disablerg = false;
  constructor(
    private _formBuilder: FormBuilder,
    private adminService: AdminService,
    public sharedService: SharedServiceService,
    private spinner: NgxSpinnerService,
    private toastr: ToastrService,
    public datepipe:DatePipe
  ) {
    this.kubernetesConfig = {
      displayKey: 'description',
      search: true,
      height: '400px',
      placeholder: 'Select Kubernetes Version',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
  }

  ngOnInit() {
    this.getResourcegroup();
    this.resourceClusterForm = this._formBuilder.group({
      resourceGroup: [''],
    });

    this.dashboardForm = this._formBuilder.group({
      dashboardtoken: ['',Validators.required],
    });

    this.aksClusterForm = this._formBuilder.group({
      aksName: ['', [
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/),
        Validators.minLength(2),Validators.maxLength(32)
    ]],
      location: ['', Validators.required],
      vmCount: ['', [Validators.required, Validators.min(1),Validators.max(100),Validators.minLength(1),Validators.maxLength(3)]],
      vmSize: ['', Validators.required],
      apMode: ['', Validators.required],
      osType: ['', Validators.required],
      acrselect:[false],
      csidriver:[false],
      acr:[''],
      diskSize: [
        '',
        [Validators.required, Validators.min(30), Validators.max(2048),Validators.minLength(1),Validators.maxLength(4)],
      ],
      podCount: [
        '',
        [Validators.required, Validators.min(30), Validators.max(250),Validators.minLength(1),Validators.maxLength(3)],
      ],
    });

    this.addNodePoolForm = this._formBuilder.group({
      aksName: ['', Validators.required],
      nodePoolName: ['', [Validators.required,Validators.pattern(/^[a-z]*$/)
                        
    ]],
      vmCount: ['', [Validators.required, Validators.min(0),Validators.max(100),Validators.minLength(1),Validators.maxLength(3)]],
      vmSize: ['', Validators.required],
      osType: ['', Validators.required],
      diskSize: [
        '',
        [Validators.required, Validators.min(30), Validators.max(2048),Validators.minLength(1),Validators.maxLength(4)],
      ],
      podCount: [
        '',
        [Validators.required, Validators.min(30), Validators.max(250),Validators.minLength(1),Validators.maxLength(3)],
      ],
    });

    this.upgradeForm = this._formBuilder.group({
      resourceGroup: [{ value: '', disabled: true }, Validators.required],
      aksName: [{ value: '', disabled: true }, Validators.required],
      kubernetesVersion: ['', Validators.required],
    });

    this.scaleForm = this._formBuilder.group({
      resourceGroup: [{ value: '', disabled: true }, Validators.required],
      aksName: [{ value: '', disabled: true }, Validators.required],
      nodePoolName: [{ value: '', disabled: true }, Validators.required],
      scaleRange: [],
      scaleInput: [
        '',
        [Validators.required, Validators.min(1), Validators.max(30),Validators.minLength(1),Validators.maxLength(2)],
      ],
    });
    this.agentPoolMode = ['System', 'User'];
    this.osType = ['Linux', 'Windows'];
    this.config = {
      displayKey: 'description',
      search: true,
      height: '350px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };

    this.locationConfig = {
      displayKey: 'description',
      placeholder: 'Select Location',
      height: '350px',
      search: true,
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search Location',
      clearOnSelection: true,
    };

    this.modeConfig = {
      displayKey: 'description',
      placeholder: 'Select Mode',
    };

    this.osConfig = {
      displayKey: 'description',
      placeholder: 'Select OS Type',
    };

    this.acrConfig = {
      displayKey: 'name',
      placeholder: 'Select ACR',
    };

    this.vmConfig = {
      displayKey: 'custom',
      search: true,
      height: '300px',
      placeholder: 'Select Node Size',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search VM',
      clearOnSelection: true,
    };
    this.dynamicURL = this.sharedService.getDynamicURL();
    this.showMessage = false;
    this.disableUpgrade = this.disRedirect = true;
    this.toastr.clear();
    this.showRgModal = this.showDeleteModal = false;
  }

  get aksClusterFormControl() {
    return this.aksClusterForm.controls;
  }

  get addNodePoolFormControl() {
    return this.addNodePoolForm.controls;
  }

  get scaleFormControls() {
    return this.scaleForm.controls;
  }
  getResourcegroup() {
    this.adminService.getAzureResourceGroupList().subscribe((res) => {
      this.associatedResourceGroup = res;
    });
  }
  getLocations() {
    this.adminService.getAksLocationList().subscribe((res) => {
      this.associatedLocations = res;
    });
  }

  getVmsizes() {
    this.adminService.getUpdatedVmsize().subscribe((res:any) => {
      res.splice(0,1);
      this.assosiateVmSizes = res;
      for (let vm of this.assosiateVmSizes) {
        vm['custom'] =
          vm.vmSize + ' Size: ' + vm.memoryInGB + '(GB) Core:' + vm.vcore;
      }
    });
    // Standard_B1s Size: 1(GB) Core:1
  }

  refresh() {
    this.loadingMsg = 'Refreshing...';
    this.loader = true;
    this.disableRefresh = true;
    this.spinner.show();
    this.adminService
      .getAKSClusterList(this.selectedResourceGroup)
      .subscribe((response) => {
        this.aksClusterList = this.adminService.decodeVersion(response);
        this.spinner.hide();
        if (this.aksClusterList.length == 0) {
          this.displayEmptyRow = true;
        } else {
          this.displayEmptyRow = false;
          this.nodePoolList = this.aksClusterList[0].aksNodePool;
        }
        if (this.aksClusterList.length <= 1) {
          this.exportDisabled = true
        } else{
          this.exportDisabled = false
          }
        this.loader = false;
        this.disableRefresh = false;
        this.displayTable =true;
        this.displayTable = true;
        this.nodeDetails = false;
        this.resourceClusterForm.controls['resourceGroup'].enable();
        this.disablerg = false;
        this.disableAddbtn = false;
        this.enableAddAksClusterForm = false;
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      });

   
   
  }

  selectResourceGroup() {
    this.disableAddbtn = false;
    this.disableRefresh = false;
    this.selectedResourceGroup =
      this.resourceClusterForm.controls['resourceGroup'].value;
    this.adminService
      .getAKSClusterList(this.selectedResourceGroup)
      .subscribe((response) => {
        this.aksClusterList = this.adminService.decodeVersion(response);
        this.displayEmptyRow = this.aksClusterList.length == 0 ? true : false;
        if (this.aksClusterList.length == 0) {
          this.displayEmptyRow = true;
        } else {
          this.displayEmptyRow = false;
          this.nodePoolList = this.aksClusterList[0].aksNodePool;
          this.nodePoolListArray = [
            ...this.nodePoolList.map((node) => node.name),
          ];
        }
        if (this.aksClusterList.length <= 1) {
          this.exportDisabled = true
        } else {
          this.exportDisabled = false
        }
      });

      this.adminService
      .getContainerRegistryList(this.selectedResourceGroup)
      .subscribe((res) => {
        this.acrdata = res;
      });
  }

  addReg() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.showRgModal = true;
    }
  }
  closeResourceGroupModal() {
    this.showRgModal = false;
  }
  resourceGrpResponse() {
    this.createdResGrpName = this.sharedService.getResourceGroup();
    this.resourceClusterForm.patchValue({
      resourceGroup: this.createdResGrpName,
    });
    this.selectResourceGroup();
  }
  resourceGroupLocation() {
    this.adminService
      .getResourceGroupLocation(this.selectedResourceGroup)
      .subscribe(
        (response) => {
          //console.log('loc',response)
          this.selectedLocation = response;
          this.aksClusterForm.patchValue({
            location: this.selectedLocation,
          });
        },
        (error: HttpErrorResponse) => {
          //console.log(error);
        }
      );
  }

  selectLocation() {
    this.selectedLocation = this.aksClusterForm.controls['location'].value;
  }

  selectMode() {
    this.selectedMode = this.aksClusterForm.controls['apMode'].value;
    if (this.selectedMode == 'System') {
      this.osType = ['Linux'];
    } else {
      this.osType = ['Linux', 'Windows'];
    }
    this.aksClusterForm.controls['osType'].reset();
  }

  selectVmSize(event: any) {
    this.selectedVmSize = event.value.vmSize.toString();
  }

  displayForm() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.enableAddAksClusterForm = true;
      this.displayTable = false;
      this.nodeDetails = false;
      this.enablesecreteKey = false;
      this.enableACR = false;
      this.aksClusterForm.reset()
      this.getLocations();
      this.getVmsizes();
      this.resourceGroupLocation();
      this.aksClusterForm.patchValue({
        acr: '',
      }); 
    }
  }

  deployCluster() {
    let Acrname = '';
    if(this.enableACR){
      Acrname = this.aksClusterForm.controls['acr'].value.name
    }else{
      Acrname = null;
    }
    if (this.aksClusterForm.invalid) {
      this.submitted = true;
    }
    if (this.aksClusterForm.valid) {
      this.spinner.show();
      this.loadingMsg = 'Deploying AKS cluster, Please wait for sometime...';
      let deployObj = {
        aksName: this.aksClusterForm.controls['aksName'].value,
        resourceGroup: this.resourceClusterForm.controls['resourceGroup'].value,
        location: this.selectedLocation,
        vmSize: this.selectedVmSize,
        vmCount: this.aksClusterForm.controls['vmCount'].value,
        agentPoolMode: this.selectedMode,
        systemType: this.aksClusterForm.controls['osType'].value,
        diskSizeInGB: this.aksClusterForm.controls['diskSize'].value,
        maxPodsCount: this.aksClusterForm.controls['podCount'].value,
        acrName:Acrname,
        addOnProfileMap: {
          azureKeyvaultSecretsProvider: {
              enabled: this.enablesecreteKey,
              config: null
            }
        }
      };
      this.adminService.deployAKSCluster(deployObj).subscribe(
        (res) => {
          this.spinner.hide();
          this.enableAddAksClusterForm = false;
          this.displayTable = true;
          this.details = res;
          this.viewDetails = true;
          this.aksClusterForm.reset();
          this.sharedService.showSuccess(
            deployObj.aksName + ' AKS Cluster deployed successfully'
          );
          this.selectResourceGroup();
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  cancel() {
    this.enableAddAksClusterForm = false;
    this.displayTable = true;
    this.selectResourceGroup();
    this.aksClusterForm.reset();
  }

  done() {
    this.viewDetails = false;
  }

  more() {
    this.nodeDetails = !this.nodeDetails;
  }

  delete(aksObj) {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.aksId = aksObj.aksId;
      this.aksName = aksObj.aksName;
      this.showDeleteModal = true;
    }
  }

  selectKubernetesVersion() {
    this.selectedVersion = this.upgradeForm.controls['kubernetesVersion'].value;
    this.disableUpgrade = false;
  }

  upgrade(aksClusterObj) {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.disableUpgrade = true;
      this.showMessage = false;
      this.upgradeAksName = aksClusterObj.aksName;
      this.upgradeMessage =
        'Upgrading the Kubernetes Version will also upgrade its associated pool node version.';
      this.spinner.show('modalSpinner-upgrade');

      this.adminService
        .getKubernetVersion(this.selectedResourceGroup, this.upgradeAksName)
        .subscribe(
          (res) => {
            this.kubernetesVersion = res;
            this.kubernetesVersion = this.kubernetesVersion.filter(
              (ver) => ver != aksClusterObj.kubernetesVersion
            );
            this.spinner.hide('modalSpinner-upgrade');
            if (this.kubernetesVersion.length == 0) {
              this.upgradeMessage =
                'You are already on latest version' +
                '(' +
                aksClusterObj.kubernetesVersion +
                ')';
              this.upgradeForm.controls['kubernetesVersion'].disable();
            }
          },
          (error: HttpErrorResponse) => {
            this.spinner.hide('modalSpinner-upgrade');
          }
        );
    }
  }

  cancelUpgrade() {
    this.upgradeForm.controls['kubernetesVersion'].reset();
  }

  confirmUpgrade() {
    this.showMessage = true;
    this.infoMessage =
      'AKS Cluster would not be available during upgrade process, Please confirm';
  }

  upgradeAKSVersion() {
    this.loadingMsg = 'Updating';
    this.loader = true;
    this.spinner.show('refreshSpinner');
    let upgradeObj = {
      resourceGroupName: this.selectedResourceGroup,
      aksName: this.upgradeAksName,
      version: this.selectedVersion,
    };

    this.adminService.setKubernetVersion(upgradeObj).subscribe(
      (res) => {
        this.selectResourceGroup();
        this.spinner.hide('refreshSpinner');
        this.sharedService.showSuccess(
          'The process to upgrade ' +
            upgradeObj.aksName +
            ' to ' +
            this.selectedVersion +
            ' is initiated successfully.'
        );
        this.loader = false;
      },
      (error: HttpErrorResponse) => {
        this.sharedService.showFail(error.error.message);
        this.spinner.hide('refreshSpinner');
      }
    );
  }

  deleteAKS() {
    this.spinner.show();
    this.loadingMsg = 'Deleting...';

    this.adminService.deleteAKSCluster(this.aksId).subscribe(
      (res) => {
        this.spinner.hide();
        this.enableAddAksClusterForm = false;
        this.sharedService.showSuccess(
          this.aksName + ' AKS Cluster deleted successfully'
        );
        this.selectResourceGroup();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  startAKS(aksCluster:any){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
     this.spinner.show()
     this.loadingMsg = 'Starting...';
      this.adminService
        .startAks(this.selectedResourceGroup, aksCluster.aksName)
        .subscribe(
          (res) => {
            this.spinner.hide();
            this.sharedService.showSuccess(
              aksCluster.aksName + ' AKS Cluster started successfully'
            );
            this.selectResourceGroup();
          },
          (error: HttpErrorResponse) => {
            this.spinner.hide();
          }
        );
    }

  }

  stopAKS(aksCluster:any){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
     this.spinner.show()
     this.loadingMsg = 'Stopping...';
      this.adminService
        .stopAks(this.selectedResourceGroup, aksCluster.aksName)
        .subscribe(
          (res) => {
            this.spinner.hide();
            this.sharedService.showSuccess(
              aksCluster.aksName + ' AKS Cluster stopped successfully'
            );
            this.selectResourceGroup();
          },
          (error: HttpErrorResponse) => {
            this.spinner.hide();
          }
        );
    }
  }
  closeDeleteModal() {
    this.showDeleteModal = false;
  }
  showNodePooldetails(nodePoolObj) {
    this.aksName = nodePoolObj.aksName;
    this.displayTable = false;
    this.nodeDetails = true;
    this.nodePoolList = nodePoolObj.aksNodePool;
    this.resourceClusterForm.controls['resourceGroup'].disable();
    this.disablerg = true;
    this.disableAddbtn = true;
  }

  hideNodePooldetails() {
    this.displayTable = true;
    this.nodeDetails = false;
    this.resourceClusterForm.controls['resourceGroup'].enable();
    this.disablerg = false;
    this.disableAddbtn = false;
  }

  onClickAddNodepool() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.enableAddNodePoolForm = true;
      this.nodeDetails = false;
      this.getVmsizes();
      this.addNodePoolForm.patchValue({
        aksName: this.aksName,
      });
      this.addNodePoolForm.controls['aksName'].disable();
    }
  }

  isUnique() {
    const nodeName: string =
      this.addNodePoolForm.controls['nodePoolName'].value;

    let isNamePresent = this.nodePoolListArray.find(
      (element) => element === nodeName.toLowerCase()
    );
    this.invalidNodeName = isNamePresent == undefined ? false : true;
    if (this.invalidNodeName) {
      this.lengthErr = false;
    } else {
      this.lengthErr = true
    }
    this.uniqueNameErrMsg = nodeName + ' is already present';
  }
  addNodePool() {
    this.loadingMsg = 'Creating Nodepool...';
    this.spinner.show();
    let nodePool = {
      aksName: this.aksName,
      nodePoolName:
        this.addNodePoolForm.controls['nodePoolName'].value.toLowerCase(),
      resourceGroupName: this.selectedResourceGroup,
      nodeCount: this.addNodePoolForm.controls['vmCount'].value,
      systemType: this.addNodePoolForm.controls['osType'].value,
      nodeSize: this.selectedVmSize,
      maxPods: this.addNodePoolForm.controls['podCount'].value,
      nodeDiskSize: this.addNodePoolForm.controls['diskSize'].value,
    };
    this.adminService.createNodePool(nodePool).subscribe(
      (res: any) => {
        this.spinner.hide();
        this.sharedService.showSuccess(res.name);
        this.selectResourceGroup();
        this.addNodePoolCancel();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      }
    );
  }
  addNodePoolCancel() {
    this.addNodePoolForm.reset();
    this.enableAddNodePoolForm = false;
    this.nodeDetails = true;
  }

  onClickUp(node) {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.existingVmCount = node.vmCount;
      this.nodePoolName = node.name;
      this.scaleForm.patchValue({
        scaleRange: node.vmCount,
        scaleInput: node.vmCount,
      });
    }
  }
  onChangeScaleInput(value) {
    if (value > 30) {
      this.scaleForm.patchValue({
        scaleRange: 30,
        scaleInput: 30,
      });
    }
    if (value <= 0) {
      this.scaleForm.patchValue({
        scaleRange: 1,
        scaleInput: 1,
      });
    }
  }
  nodeScale() {
    this.loadingMsg = 'Scaling';
    this.loader = true;
    this.spinner.show('refreshSpinner');
    let scaleobj = {
      resourceGroupName: this.selectedResourceGroup,
      aksName: this.aksName,
      nodePoolName: this.nodePoolName,
      nodeCount: this.scaleForm.controls['scaleInput'].value,
    };
    this.adminService.scaleNodePool(scaleobj).subscribe(
      (res) => {
        this.sharedService.showSuccess(
          'The process of scaling node ' +
            this.nodePoolName +
            '  is initiated successfully.'
        );
        this.spinner.hide('refreshSpinner');
        this.loader = false;
        this.selectResourceGroup();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide('refreshSpinner');
        this.loader = false;
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  kubeDashboardPopUp(aksObj) {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.aksName = aksObj.aksName;
      this.spinner.show('tokenSpinner');
      this.adminService
        .getKubernetDashboard(this.selectedResourceGroup, this.aksName)
        .subscribe((res) => {
          this.dashboardObj = res;
          this.disRedirect = true;
          this.spinner.hide('tokenSpinner');
          this.dashboardForm.patchValue({
            dashboardtoken: this.dashboardObj.token,
          });
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide('tokenSpinner');
          this.sharedService.showFail(error.error.message);
        });
    }
  }

  copyToken(inputElement) {
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);
    this.disRedirect = false;
  }

  redirectToDashboard() {
    window.open(this.dashboardObj.dashboardIp);
  }

  poolnamevalidation(){
    this.selectedModePool = this.addNodePoolForm.controls['osType'].value;
    if (this.selectedModePool == 'Linux') { 
      this.addNodePoolForm.controls['nodePoolName'].setValidators([Validators.maxLength(12),Validators.pattern(/^[a-z]*$/)]);
      this.nodepoolchar=12;
    }
    else if(this.selectedModePool == 'Windows'){
      this.addNodePoolForm.controls['nodePoolName'].setValidators([Validators.maxLength(6),Validators.pattern(/^[a-z]*$/)]); 
      this.nodepoolchar=6;
    }
  }
  

  // downloadAll(){
  // 	this.multipleResourcesId = [...this.aksClusterList.map(item=>item.aksId)];
  // 	this.exportData = {
  // 	  "resourceGroupName": this.selectedResourceGroup,
  // 	  "resourceId": this.multipleResourcesId,
  //     "resourceName":"AKS_CLUSTER"
  // 	}
  // 	this.adminService.exportService(this.exportData).subscribe(res => {
  // 	  window.open(this.dynamicURL + "/" + res);
  // 	  this.sharedService.showSuccess("Download successful!!!");
  // 	},(error:HttpErrorResponse)=>{
  // 		this.sharedService.showFail(error.error.message);
  // 	});
  //   }

  //   download(aksCluster){
  // 	this.aksId = aksCluster.aksId;
  // 	this.exportData = {
  // 	  "resourceGroupName": this.selectedResourceGroup,
  // 	  "resourceId": [this.aksId],
  //     "resourceName":aksCluster.aksName
  // 	}
  // 	this.adminService.exportService(this.exportData).subscribe(res => {
  //     window.open(this.dynamicURL + "/" + res);
  // 	  this.sharedService.showSuccess("Download successful!!!");
  // 	},(error:HttpErrorResponse)=>{
  // 		this.sharedService.showFail(error.error.message);
  // 	});
  //   }

  showRepoDetails(){
      
      this.showmsg = true;
  }

  onChange(value:any){
    this.enablesecreteKey = value;
  }
  onSelectACRChange(value:any){
    this.enableACR = value;
    this.aksClusterForm.patchValue({
      acr: '',
    });
  }
  enablesecrets(aks:any){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.spinner.show();
      this.loadingMsg = 'Enabling...';
      this.adminService.enableKeystoreForAks(aks.resourceGroup,aks.aksName).subscribe(
        (response: any) => {
          this.spinner.hide();
          this.sharedService.showSuccess(
          'Enabled successfully'
          );
          this.refresh()
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          //console.log(error);
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }
  disablesecrets(aks:any){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.spinner.show();
      this.loadingMsg = 'Disabling...';
      this.adminService.disableKeystoreForAks(aks.resourceGroup,aks.aksName).subscribe(
        (response: any) => {
          this.spinner.hide();
          this.sharedService.showSuccess(
          'Disabled successfully '
          );
          this.refresh()
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          //console.log(error);
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  keyPressAlphanumeric(event) {

    var inp = String.fromCharCode(event.keyCode);
  
    if (/[0-9]/.test(inp)) {
      return true;
    } else {
      event.preventDefault();
      return false;
    }
  }

  onRefreshClick(){
   
  this.adminService
    .getAKSClusterList(this.selectedResourceGroup)
    .subscribe((response) => {
      this.aksClusterList = this.adminService.decodeVersion(response);
      var refreshTime = new Date();
      this.refreshTime = 'As of ' + this.datepipe.transform(refreshTime, 'MM/dd/yyyy h:mm a')
      this.displayEmptyRow = this.aksClusterList.length == 0 ? true : false;
      if (this.aksClusterList.length == 0) {
        this.displayEmptyRow = true;
      } else {
        this.displayEmptyRow = false;
        this.nodePoolList = this.aksClusterList[0].aksNodePool;
        this.nodePoolListArray = [
          ...this.nodePoolList.map((node) => node.name),
        ];
       
      }
      if (this.aksClusterList.length <= 1) {
        this.exportDisabled = true
      } else {
        this.exportDisabled = false
      }
    });
  }
}
