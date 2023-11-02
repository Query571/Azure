import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { SharedServiceService } from '../services/shared-service.service';

@Component({
  selector: 'app-azure-services',
  templateUrl: './azure-services.component.html',
  styleUrls: ['./azure-services.component.css'],
})
export class AzureServicesComponent implements OnInit {
  constructor(private router: Router, private toastr: ToastrService,public sharedService:SharedServiceService) {
  }

  ngOnInit(): void {
    this.toastr.clear();
  }

  navigateToSaaS() {
    this.router.navigateByUrl('SaaS');
  }

  navigateToPaaS() {
    this.router.navigateByUrl('PaaS');
  }

  navigateToIaaS() {
    this.router.navigateByUrl('IaaS');
  }

  navigateToIaaC() {
    this.router.navigateByUrl('IaaC');
  }
  navigateToDevops(){
    this.router.navigateByUrl('devops');

  }
  // navigateToTemplate(){
  //   this.router.navigateByUrl('template');

  // }
  navigateToVnet = () => {
    this.router.navigateByUrl('vnet-details');
  };

  navigateToSqlDatabase() {
    this.router.navigateByUrl('sqldb');
  }

  navigateToAppRegistration() {
    this.router.navigateByUrl('appregistrations');
  }

  navigateToKeyVaults() {
    this.router.navigateByUrl('keyvaults');
  }

  navigateToStorageAccount() {
    this.router.navigateByUrl('storageacc');
  }

  navigateToAks() {
    this.router.navigateByUrl('aks-cluster');
  }

  navigateToPolicy() {
    this.router.navigateByUrl('policies');
  }

  navigateToMpnitor() {
    this.router.navigateByUrl('monitor');
  }

  navigateToAppServices() {
    this.router.navigateByUrl('app-deploy-services');
  }

  navigateToConfig() {
    this.router.navigateByUrl('download-config-template');
  }

  navigateToVM() {
    this.router.navigateByUrl('virtual-machine');
  }

  navigateToContainerServices() {
    this.router.navigateByUrl('container-services');
  }
}
