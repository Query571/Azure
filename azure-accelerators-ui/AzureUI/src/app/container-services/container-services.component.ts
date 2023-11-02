import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-container-services',
  templateUrl: './container-services.component.html',
  styleUrls: ['./container-services.component.css'],
})
export class ContainerServicesComponent {
  
  constructor(private router: Router) {}

  navigateToAks() {
    this.router.navigateByUrl('aks-cluster');
  }

  navigateToContainerRegistry() {
    this.router.navigateByUrl('container-registries');
  }

  navigateToAksApp() {
    this.router.navigateByUrl('aks-app-deploy');
  }
  
}
