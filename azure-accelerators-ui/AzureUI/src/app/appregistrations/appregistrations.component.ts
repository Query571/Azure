import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';

@Component({
  selector: 'app-appregistrations',
  templateUrl: './appregistrations.component.html',
  styleUrls: ['./appregistrations.component.css'],
})
export class AppregistrationsComponent implements OnInit {
  createAppRegistrationForm: FormGroup;
  listOfApps: any = [];
  appFilter: any = { name: '' };
  loadingMsg = '';
  config: any;
  types = ['http', 'https'];
  selectedProtocol: string = '';
  constructor(
    private _formBuilder: FormBuilder,
    private adminService: AdminService,
    private toastr: ToastrService,
    private spinner: NgxSpinnerService,
    public sharedService: SharedServiceService
  ) {
    this.config = {
      displayKey: 'description',
      placeholder: 'Select',
    };
  }

  ngOnInit(): void {
    this.getApps();
    this.createAppRegistrationForm = this._formBuilder.group({
      appName: ['', [
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)
    ]],
      protocolType: ['', Validators.required],
      uri:['', [
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)
    ]],
    });
    this.toastr.clear();
  }

  getApps() {
    this.appFilter = { name: '' };
    this.loadingMsg = 'Loading...';
    this.spinner.show();
    this.adminService.getAppRegList().subscribe(
      (res) => {
        this.listOfApps = res;
        this.spinner.hide();
      },
      (error: HttpErrorResponse) => {
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  createAppReg() {
    this.loadingMsg = 'Creating...';
    this.spinner.show();
    let responeseObject: any;
    let exstr = '';
    let appName = this.createAppRegistrationForm.controls['appName'].value;
    let URI =
      this.selectedProtocol +
      '://' +
      this.createAppRegistrationForm.controls['uri'].value;
    this.adminService.createAppReg(appName, URI).subscribe(
      (res) => {
        responeseObject = res;
        this.spinner.hide();
        this.sharedService.showSuccess('App created successfully!!!');
        this.showInfo(responeseObject.clientSecret);
        this.getApps();
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  selectProtocol(protocol) {
    this.selectedProtocol = protocol.value;
  }

  showInfo(msg) {
    this.toastr.info(msg, 'Please copy and save Client Secret', {
      positionClass: 'toast-top-right',
      closeButton: true,
      disableTimeOut: true,
      tapToDismiss: false,
    });
  }
}
