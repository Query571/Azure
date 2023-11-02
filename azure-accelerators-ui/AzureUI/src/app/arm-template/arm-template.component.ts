import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, MinLengthValidator, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../services/admin.service';
import { SharedServiceService } from '../services/shared-service.service';
import {saveAs} from 'file-saver';

@Component({
  selector: 'app-arm-template',
  templateUrl: './arm-template.component.html',
  styleUrls: ['./arm-template.component.css']
})
export class ArmTemplateComponent implements OnInit {
  associatedResource :any 
  ARMTemplateForm :FormGroup
  config:any;
  loadingMsg = ''
  constructor( private _formBuilder: FormBuilder,
    private adminService: AdminService,
    public sharedService: SharedServiceService,
    private spinner: NgxSpinnerService,
    private toastr: ToastrService,
    ) { }

  ngOnInit(): void {
    this.ARMTemplateForm  = this._formBuilder.group({
      resource: ['',Validators.required],
      count :[
        '',
        [Validators.required, Validators.min(1), Validators.max(5)],
      ],
    });
    this.config = {
      displayKey: 'key',
      search: true,
      height: '400px',
      placeholder: 'Select Resource',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.associatedResource =[{"key":"Keyvault","value":"keyvault"},{"key":"AKS Cluster","value":"cluster"},
    {"key":"SQL Server","value":"SqlServer"},{"key":"Virtual Network","value":"VirtualNetwork"},
    {"key":"Storage Account","value":"StorageAccount"},{"key":"SQL Database","value":"SqlDb"},{"key":"Network Security Group","value":"NetworkSecurityGroup"}]
  }

  get ARMTemplateFormControl() {
    return this.ARMTemplateForm.controls;
  }

  // get templateFormControl() {
  //   return this.templateForm.controls;
  // }
  downloadtemplate(){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }
    else{
      let resource = this.ARMTemplateForm.controls['resource'].value.value
      let count =this.ARMTemplateForm.controls['count'].value;
      this.adminService.downloadtemplate(resource,count).subscribe(
        (res: any) => {
          let jsonfile = JSON.stringify(res, null, 4)
          const blob = new Blob([jsonfile], {type : 'application/json'});
          saveAs(blob,this.ARMTemplateForm.controls['resource'].value.key);
        },
        (error: HttpErrorResponse) => {
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }
  selectResource(){

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

  cancel(){
this.ARMTemplateForm.patchValue({
  "resource":'',
  "count":''
})
  }
}
