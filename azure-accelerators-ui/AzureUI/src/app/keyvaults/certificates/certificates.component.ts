import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from 'src/app/services/admin.service';
import { KeyVaultService } from 'src/app/services/keyvault.service';
import { SharedServiceService } from 'src/app/services/shared-service.service';
@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.css']
})
export class CertificatesComponent implements OnInit {
  KeysForm: FormGroup;
  loadingMsg;
  sourceKeyVault ='';
  sourceKeyVaultName = ''
  showNew: boolean = true;
  disableNew:boolean;
  displayTable = true;
  enableAddSecretsForm = false;
  config:any;
  listofIssuer =["Self"]
  listofCertificate = [];
  invalidCerName:boolean;
  invalidCertificateErrMsg =''
  validationstring:string;
  constructor(private keyVaultService:KeyVaultService,
    private _formBuilder: FormBuilder,
    private sharedService: SharedServiceService,
    private spinner: NgxSpinnerService,
    private toastr: ToastrService,
    private adminService: AdminService,) { }

  ngOnInit(): void {
    this.sourceKeyVault = this.keyVaultService.getSourceKeyVault();
    this.KeysForm = this._formBuilder.group({
      sourcekeyvault: ['', Validators.required],
      certificateName: ['', [
        Validators.required,
        Validators.pattern("^(?=.*[a-zA-Z])[A-Za-z0-9-]+$"),
        Validators.minLength(2),Validators.maxLength(32)
    ]],
      subject: ['', [
        Validators.required,
        Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/),
        Validators.minLength(2),Validators.maxLength(32)
    ]],
      issuer:['',Validators.required]
    });
    this.displayTable = true;
    this.config = {
      displayKey: 'description',
      search: true,
      height: '300px',
      placeholder: 'Select Issuer',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.getListofCertificate();
  }

  get certificateFormControl() {
    return this.KeysForm.controls;
  }

  displayForms(){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.enableAddSecretsForm = true;
      this.displayTable = false
      this.sourceKeyVaultName = this.sourceKeyVault;
      this.KeysForm.reset();
      this.KeysForm.patchValue({
        sourcekeyvault:this.sourceKeyVault
      })
    }
  }

  refresh(){
   
   
this.getListofCertificate()
   
   

  }

  getListofCertificate(){
    this.loadingMsg = 'Loading Certificates...'
    this.spinner.show();
    this.keyVaultService
    .getListOfCertificate(this.sourceKeyVault)
    .subscribe((response:any) => {
      this.listofCertificate = response;
      this.spinner.hide();
      this.enableAddSecretsForm = false;
      this.displayTable = true
    },
    (error: HttpErrorResponse) => {
      this.spinner.hide();
      this.sharedService.showFail(error.error.message);
    });
  }

  certificateChange(){
    const certificateName: string =  this.KeysForm.controls['certificateName'].value;
    if (certificateName != null) {
      let isNamePresent = this.listofCertificate.find(
        (element) => element.certificateName.toLowerCase() === certificateName.toLowerCase()
      );
      this.invalidCerName = isNamePresent == undefined ? false : true;
      //console.log('invalid',isNamePresent);
    this.invalidCertificateErrMsg = certificateName + ' is already present';
    }
  }
  AddCertificate(){
    this.spinner.show();
    this.loadingMsg = 'Creating...';
    let certificatObj = {
      keyvaultName: this.sourceKeyVault,
      certificateName: this.KeysForm.controls['certificateName'].value,
      subject: this.KeysForm.controls['subject'].value,
      issuer2:this.KeysForm.controls['issuer'].value
    };
    this.keyVaultService.addCertificate(certificatObj).subscribe(
      (res:any) => {
        this.spinner.hide();
        if(res != null ){
          this.enableAddSecretsForm = false;
        this.displayTable = true;
      
        this.KeysForm.reset();
      
        this.sharedService.showSuccess(
         res.status_details
        );
        this.getListofCertificate();
    
        }
        
        else{
          this.sharedService.showFail(
             'Failed to create certificate'
          );
        }
        
      },
      (error: HttpErrorResponse) => {
        this.spinner.hide();
        this.sharedService.showFail(error.error.message);
      }
    );
  }

  

  cancel(){
    this.displayTable = true;
    this.enableAddSecretsForm = false;
  }

  delete(certificate){
    if(this.sharedService.userRole == "Auditor"){
      this.sharedService.showFail('Bad Request');
    }else{
      this.spinner.show();
      this.loadingMsg = 'Deleting...'
      this.keyVaultService.deleteCertificate(this.sourceKeyVault,certificate.certificateName).subscribe(
        (res) => {
          this.spinner.hide();
          this.sharedService.showSuccess(
            certificate.certificateName +  ' deleted successfully'
          );
          this.getListofCertificate();
        },
        (error: HttpErrorResponse) => {
        // console.log('call3')
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  certificatevalidation(){
    let certificatesubject=this.KeysForm.controls['subject'].value;
    let certificatesubjectlength=certificatesubject.length;
    if(certificatesubjectlength >=1){
      if(!(certificatesubject.charAt(0)=="C" && certificatesubject.charAt(1)=="N" && certificatesubject.charAt(2)=="=")){
        this.validationstring="The subject must specify a common name (use 'CN=')";
        //console.log("error");
      }
      else{
        this.validationstring=" ";
       }
    }

  }
}

