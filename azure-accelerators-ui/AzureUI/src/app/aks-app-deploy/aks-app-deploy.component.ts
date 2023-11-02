import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';
import { fileExtensionValidator } from '../upload-config/file-extension-validator.directive';

@Component({
  selector: 'app-aks-app-deploy',
  templateUrl: './aks-app-deploy.component.html',
  styleUrls: [],
})
export class AksAppDeployComponent implements OnInit {
  aksAppForm: FormGroup;
  config: any;
  config2: any;
  associatedResourceGroup: any = [];
  aksList: any = [];
  selectedResourceGroup = '';
  selectedAks: any;
  loadingMsg: string = '';
  fileData: any;
  acceptedExtension = 'yml,yaml';
  fileHolder: File;
  showRgModal: boolean = false;
  createdResGrpName: string;
  sizeflag = false;

  constructor(
    private adminService: AdminService,
    private _formBuilder: FormBuilder,
    public sharedService: SharedServiceService,
    private toastr: ToastrService,
    private spinner: NgxSpinnerService
  ) {
    this.config = {
      displayKey: 'description',
      search: true,
      height: '400px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.config2 = {
      displayKey: 'aksName',
      search: true,
      height: '400px',
      placeholder: 'Select AKS Cluster',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.toastr.clear();
  }

  ngOnInit(): void {
    this.getResourcegroup();

    this.aksAppForm = this._formBuilder.group({
      resourceGroup: ['', Validators.required],
      aksApp: ['', Validators.required],
      fileUpload: [
        '',
        [Validators.required, fileExtensionValidator(this.acceptedExtension)],
      ],
    });

    this.aksAppForm.controls['aksApp'].disable();
    this.aksAppForm.controls['fileUpload'].disable();
  }

  get aksFormControls() {
    return this.aksAppForm.controls;
  }

  getResourcegroup() {
    this.adminService.getAzureResourceGroupList().subscribe((res) => {
      this.associatedResourceGroup = res;
    });
  }

  selectResourceGroup() {
    this.aksList = [];
    this.selectedResourceGroup =
      this.aksAppForm.controls['resourceGroup'].value;
      this.aksAppForm.patchValue({
        aksApp:''
      })
    this.adminService
      .getAKSClusterList(this.selectedResourceGroup)
      .subscribe((res) => {
        this.aksList = res;
      });
    this.aksAppForm.controls['aksApp'].enable();
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
    this.aksAppForm.patchValue({
      resourceGroup: this.createdResGrpName,
    });
    this.selectResourceGroup();
  }

  selectAks() {
    this.selectedAks = this.aksAppForm.controls['aksApp'].value;
    if(this.sharedService.userRole != 'Auditor'){
    this.aksAppForm.controls['fileUpload'].enable();
    }
  }

  chooseFile(files) {
    this.sizeflag = false;
    if(files[0].size <= 5000000){
      if (files && files.length) {
        this.fileHolder = files[0];
      }
    }else{
this.sizeflag = true;
this.fileHolder = undefined
    }
   
  }

  uploadYml() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else if(!this.sizeflag){
      this.loadingMsg = 'Deploying...';
      this.spinner.show();
      const formData = new FormData();
      formData.append('aksName', this.selectedAks.aksName);
      formData.append('resourceGroupName', this.selectedResourceGroup);
      formData.append('file', this.fileHolder);
  
      this.adminService.uploadAksData(formData).subscribe(
        (res: any) => {
          this.spinner.hide();
          this.sharedService.showSuccess(res.name);
          this.aksAppForm.reset();
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
  
    }
  }
}
