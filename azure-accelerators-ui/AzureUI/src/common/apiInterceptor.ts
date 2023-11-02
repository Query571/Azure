import { Injectable } from "@angular/core";
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpHeaders,
    HttpErrorResponse
} from "@angular/common/http";
import { Router } from '@angular/router';
import { Observable, throwError } from "rxjs";
import { retry, catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { BnNgIdleService } from 'bn-ng-idle'; // import it to your component

@Injectable({ providedIn: 'root' })
export class HttpRequestInterceptor implements HttpInterceptor {
    constructor(private router: Router,private BnNgService:BnNgIdleService) { }
    //function which will be called for all http calls
    intercept(
        request: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        //Retrieve accesstoken from local storage
        let data = JSON.parse(sessionStorage.getItem('currentUser'));
        //For login
        if (request.url.endsWith("token")) {

            const cloned = request.clone({
                headers: new HttpHeaders({
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Authorization': 'Basic YXp4LWNsaWVudC1pZDpYWTdrbXpvTnpsMTAw',
                    'Access-Control-Allow-Methods': '*'
                })
            });
            return next.handle(cloned)
        } 
        //For api other than login
        else {
            const accessToken = localStorage.getItem("access_token");
            //Check if accesToken exists, else send request without bearer token
            if (data.access_token) {
                // this.BnNgService.resetTimer();
                let token = "bearer " + data.access_token;
                const cloned = request.clone({
                    headers: request.headers.set("Authorization",
                        token)
                });
                return next.handle(cloned);
            }
            else {
                this.router.navigate(['login']); 
            }
        }
    }
}

@Injectable({ providedIn: 'root' })
export class HttpErrorInterceptor implements HttpInterceptor {
    constructor(private router: Router,private toastr:ToastrService) { }
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      return next.handle(request)
        .pipe(
          retry(1),
          catchError((error: HttpErrorResponse) => {
            if(error.status==401 ) {
                this.router.navigate(['login']);
            }
            return throwError(error);
          })
        )
    }
   }