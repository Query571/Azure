import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';

@Component({
  selector: 'app-app-services',
  templateUrl: './app-services.component.html',
  styleUrls: ['./app-services.component.css'],
})
export class AppServicesComponent implements OnInit {
  appServicesForm: FormGroup;
  associatedResourceGroup: any = [];
  listOfAppServices: any = [];
  selectedResourceGroup: string = '';
  loadingMsg: string = '';
  appServiceId: string = '';
  hostName: string = '';
  appServiceName: string = '';
  appServiceStatus: string = '';
  disableStart: boolean;
  disableStop: boolean;
  disableRestart: boolean;
  disableBrowse: boolean;
  disableDelete: boolean;
  disableRefresh: boolean = true;
  displayEmptyRow: boolean = false;
  config: any = [];
  showRgModal: boolean = false;
  createdResGrpName: string;
  displayName = '';
  showDeleteModal: boolean = false;
  constructor(
    private adminService: AdminService,
    private _formBuilder: FormBuilder,
    private toastr: ToastrService,
    public sharedService: SharedServiceService,
    private spinner: NgxSpinnerService
  ) {
    this.config = {
      displayKey: 'description',
      search: true,
      height: '350px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
  }

  ngOnInit(): void {
    this.appServicesForm = this._formBuilder.group({
      resourceGroup: [''],
      appService: [''],
    });
    this.getResourcegroup();
    this.endisbuttons();
    this.toastr.clear();
  }

  getResourcegroup() {
    this.adminService.getAzureResourceGroupList().subscribe((res) => {
      this.associatedResourceGroup = res;
    });
  }

  endisbuttons() {
    if (this.appServiceStatus == '') {
      this.disableStart = true;
      this.disableStop = true;
      this.disableRestart = true;
      this.disableBrowse = true;
      this.disableDelete = true;
    }
    if (this.appServiceStatus == 'Running') {
      this.disableStart = true;
      this.disableStop = false;
      this.disableRestart = false;
      this.disableBrowse = false;
      this.disableDelete = false;
    }
    if (this.appServiceStatus == 'Stopped') {
      this.disableStart = false;
      this.disableStop = true;
      this.disableRestart = true;
      this.disableBrowse = true;
      this.disableDelete = false;
    }
  }

  selectResourceGroup() {
    this.loadingMsg = 'Loading...';
    this.appServiceName = '';
    this.appServiceStatus = '';
    this.displayEmptyRow = false;
    this.spinner.show();
    this.endisbuttons();
    this.selectedResourceGroup =
      this.appServicesForm.controls['resourceGroup'].value;
    this.adminService.listAppAervices(this.selectedResourceGroup).subscribe(
      (res) => {
        this.disableRefresh = false;
        this.listOfAppServices = res;
        if (Object.keys(res).length == 0) {
          this.displayEmptyRow = true;
        }
        this.spinner.hide();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
        this.endisbuttons();
      }
    );
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
    this.appServicesForm.patchValue({
      resourceGroup: this.createdResGrpName,
    });
    this.selectResourceGroup();
  }
  getApp(app) {
    this.appServiceName = app.webAppName;
    this.displayName = app.webAppName + ' | ';
    this.hostName = app.hostName[0];
    this.appServiceStatus = app.status;
    this.appServiceId = app.webAppId;
    this.endisbuttons();
  }

  startApp() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.loadingMsg = 'Starting App..';
      this.spinner.show();
      return this.adminService.appServiceStart(this.appServiceId).subscribe(
        (res) => {
          this.commonStart();
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  stopApp() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.loadingMsg = 'Stopping App..';
      this.spinner.show();
      return this.adminService.appServiceStop(this.appServiceId).subscribe(
        (res) => {
          this.spinner.hide();
          this.sharedService.showSuccess(
            this.appServiceName + ' App is now Stopped'
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

  restartApp() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.loadingMsg = 'Restarting App..';
      this.spinner.show();
      return this.adminService.appServiceRestart(this.appServiceId).subscribe(
        (res) => {
          this.commonStart();
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  commonStart(){
    this.spinner.hide();
    this.sharedService.showSuccess(this.appServiceName + ' App is Running');
    this.selectResourceGroup();
  }

  clickOnDelete() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.showDeleteModal = true;
    }
  }

  deleteApp() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      this.loadingMsg = 'Deleting app...';
      this.spinner.show();
      this.adminService.appServiceDelete(this.appServiceId).subscribe(
        (res) => {
          this.spinner.hide();
          this.sharedService.showSuccess(this.appServiceName + ' App is Deleted');
          this.selectResourceGroup();
        },
        (error: HttpErrorResponse) => {
          this.sharedService.showFail(error.error.message);
          this.spinner.hide();
        }
      );
    }
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
  }

  browseApp() {
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      let url = 'https://' + this.hostName;
      window.open(url,'_blank');
    }
    
  }
}
