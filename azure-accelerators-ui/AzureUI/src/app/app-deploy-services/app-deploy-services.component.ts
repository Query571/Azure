import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-app-deploy-services',
  templateUrl: './app-deploy-services.component.html',
  styleUrls: ['./app-deploy-services.component.css']
})
export class AppDeployServicesComponent implements OnInit {

  constructor(private router: Router, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.toastr.clear();
  }

  navigateToAppServices() {
    this.router.navigateByUrl('app-services');
  }


  navigateToUpload() {
    this.router.navigateByUrl('upload-config');
  }
}
