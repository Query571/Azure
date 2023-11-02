import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, Validators, FormBuilder } from '@angular/forms';
import { Location } from '@angular/common';
import decode from 'jwt-decode';
import { AdminService } from './services/admin.service';
import { SharedServiceService } from './services/shared-service.service';
import { PolicyService } from './services/policy.service';
import { PrimeNGConfig, MessageService } from 'primeng/api';
import { ToastModel } from './models/toast.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [MessageService],
})
export class AppComponent {
  title = 'AzureUI';
  subId = '';
  isDashboard: boolean = true;
  displayId: boolean;
  passwordMismatch: boolean = false;
  changePasswordForm: FormGroup;
  passwordChanged: boolean = false;
  errorMsg: string = '';
  confirmFieldTextType: boolean;
  newFieldTextType: boolean;
  isSimilar: boolean;
  displayControls: boolean;
  showSidebar: boolean;
  oldFieldTextType:boolean;
  username='';
  

  constructor(
    private formBuilder: FormBuilder,
    private adminService: AdminService,
    private router: Router,
    private sharedService: SharedServiceService,
    private location: Location,
    private policyService: PolicyService,
    private primengConfig: PrimeNGConfig,
    private messageService: MessageService,
  ) {

    this.sharedService.messageService$.subscribe(messageContent => {
      this.displayToast(messageContent);
    });
    this.sharedService.userRole = sessionStorage.getItem('role');
    // this.sharedService.showSuccess('success');
    //console.log('role',this.sharedService.userRole)
    router.events.subscribe((val) => {
      this.isDashboard =
        location.path() === '/' ||
        location.path() === '/login' ||
        location.path() === ''
          ? false
          : true;
      this.displayId = location.path() === '/onboarding' ? false : true;
      this.displayControls =
        location.path() === '/onboarding' ||
        location.path() === '/view-subscription' ||
        location.path() === '/azure-services'
          ? false
          : true;
      this.showSidebar =
        location.path() === '/' ||
        location.path() === '/login' ||
        location.path() === '' ||
       ( location.path() === '/onboarding'  && (sessionStorage.getItem('subId') == 'null')) ||
        location.path() === '/view-subscription' ||
        (location.path() === '/user-management' && (sessionStorage.getItem('subId') == 'null')) ||
        location.path() === '/azure-services' 
          ? false
          : true;
    });

    location.onUrlChange((url) => {
      this.subId = sessionStorage.getItem('subId');
    });
    location.onUrlChange((url) => {
      this.username = sessionStorage.getItem('userName');
    });
   
    this.changePasswordForm = this.formBuilder.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  previous() {
    this.location.back();
  }
  doBeforeUnload() {
  //  console.log('ondefoere')

    // Alert the user window is closing 

    return false;

}

doUnload() {
  // Clear session or do something
  // this.auth.getLogout();
//  console.log('route',this.router)
  let routepath :any= this.router;
  if(routepath.location.path() === '/login'){
  }else{
    this.logoutuser()
  }

  //console.log('doUnload')
    
}

logoutuser(){
let userId = sessionStorage.getItem('userName');
let encodeduser = window.btoa(userId)
  this.adminService.logout(encodeduser).subscribe((res:any) => {
    
    this.logout()
      },
      (error) => {
      });
}
  logout() {
    sessionStorage.removeItem('userName');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('role');
    sessionStorage.clear();
    this.router.navigate(['login']);
  }

  switchSubscription() {
    this.router.navigateByUrl('onboarding');
  }

  azxHome() {
    this.router.navigateByUrl('azure-services');
  }

  cancel() {
    this.changePasswordForm.reset();
  }

  get changePasswordFormControl() {
    return this.changePasswordForm.controls;
  }
  
  toggleConfirmFieldTextType() {
    this.confirmFieldTextType = !this.confirmFieldTextType;
  }

  toggleNewFieldTextType() {
    this.newFieldTextType = !this.newFieldTextType;
  }

  toggleOldFieldTextType(){
    this.oldFieldTextType = !this.oldFieldTextType;
   }
 

  // toggle() {
  //   let element = document.getElementById('sidebar');
  //   element.classList.toggle('active');
  // }

  backToTop() {
    window.scrollTo(0, 0);
  }

  redirectTo(uri:string){
    this.router.navigateByUrl('/', {skipLocationChange: true}).then(()=>
    this.router.navigate([uri]));
 }
  changePassword() {
    let token = sessionStorage.getItem('currentUser');
    let t = decode(token);
    let uuid = sessionStorage.getItem('uuid')
    this.errorMsg = '';

    let obj = {
      id: t['id'],
      userUID: uuid,
      oldPassword: window.btoa(this.changePasswordForm.controls['oldPassword'].value),
      newPassword: window.btoa(this.changePasswordForm.controls['newPassword'].value),
    };
    if (
      this.changePasswordForm.controls['newPassword'].value ===
      this.changePasswordForm.controls['confirmPassword'].value
    ) {
      this.adminService.updatePassword(obj).subscribe(
        (res) => {
          // sessionStorage.removeItem('userName');
          // sessionStorage.removeItem('userId');
          // sessionStorage.removeItem('currentUser');
          // sessionStorage.clear();
          this.sharedService.showSuccess('Password changed successfully!');
          this.changePasswordForm.reset();
          // this.router.navigateByUrl('login');
          this.logoutuser()
        },
        (error) => {
          this.errorMsg = error.error.message;
          this.changePasswordForm.reset();
          this.sharedService.showFail(this.errorMsg);
        }
      );
    } else {
      alert('New password and Confirm password should be same');
    }
  }

  displayToast(messageContent: ToastModel) {
    this.messageService.clear();
    const messageDetails = {
      sticky: messageContent.sticky, 
      severity: messageContent.severity, 
      summary: messageContent.summary, 
      detail: messageContent.detail,
      ...messageContent.life && {
        life: 3000
      }
    }
    this.messageService.add(messageDetails);
  }

  ngOnInit() {
    this.primengConfig.ripple = true; 
  }

  rubookclick= function () {
    this.router.navigateByUrl('/document');
};
}
