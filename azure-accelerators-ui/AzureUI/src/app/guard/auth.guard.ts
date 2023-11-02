import { Injectable } from '@angular/core';
import {
  Router,
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (sessionStorage.getItem('userName')) {
      if (sessionStorage.getItem('userId')) {
        if((sessionStorage.getItem('role') == 'Operator' || sessionStorage.getItem('role') == 'Auditor') && state.url == '/onboarding'){
          this.router.navigate(['login']);
          return false;
        }else if((sessionStorage.getItem('role') == 'Operator' || sessionStorage.getItem('role') == 'Auditor') && state.url == '/user-management'){
          this.router.navigate(['login']);
          return false;
      }else{
        return true;
      }
   
      }
    } else {
      this.router.navigate(['login']);
      return false;
    }
  }
}
