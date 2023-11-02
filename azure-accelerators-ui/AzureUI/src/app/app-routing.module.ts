import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AksAppDeployComponent } from './aks-app-deploy/aks-app-deploy.component';
import { AksClusterComponent } from './aks-cluster/aks-cluster.component';
import { AppDeployServicesComponent } from './app-deploy-services/app-deploy-services.component';
import { AppServicesComponent } from './app-services/app-services.component';
import { AppregistrationsComponent } from './appregistrations/appregistrations.component';
import { AzureServicesComponent } from './azure-services/azure-services.component';
import { IaacComponent } from './azure-services/iaac/iaac.component';
import { LaasComponent } from './azure-services/laas/laas.component';
import { PaasComponent } from './azure-services/paas/paas.component';
import { SaasComponent } from './azure-services/saas/saas.component';
import { ContainerRegistriesComponent } from './container-registries/container-registries.component';
import { ContainerServicesComponent } from './container-services/container-services.component';
import { AuthGuard } from './guard/auth.guard';
import { KeyvaultsComponent } from './keyvaults/keyvaults.component';
import { LoginComponent } from './login/login.component';
import { MonitorComponent } from './monitor/monitor.component';
import { Onboarding } from './onboarding/onboarding.component';
import { AssignPolicyComponent } from './policy/assign-policy/assign-policy.component';
import { MainPolicyComponent } from './policy/main-policy/main-policy.component';
import { PolicyComponent } from './policy/policy.component';
import { SqldatabaseComponent } from './sqldatabase/sqldatabase.component';
import { StorageaccountComponent } from './storageaccount/storageaccount.component';
import { UploadConfigComponent } from './upload-config/upload-config.component';
import { VirtualmachineComponent } from './virtualmachine/virtualmachine.component';
import { NetworkSecurityGroupComponent } from './virtualnetwork/network-security-group/network-security-group.component';
import { VirtualnetworkDetailsComponent } from './virtualnetwork/virtualnetwork-details/virtualnetwork-details.component';
import { VirtualnetworkComponent } from './virtualnetwork/virtualnetwork.component';
import { VnetPeeringComponent } from './virtualnetwork/vnet-peering/vnet-peering.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { DocumentComponent } from './document/document.component';
import { AzureDevopsComponent } from './azure-devops/azure-devops.component';
import { ArmTemplateComponent } from './arm-template/arm-template.component';
import { SwitchSubscriptionComponent } from './switch-subscription/switch-subscription.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    pathMatch: 'full',
  },
  {
    path: 'onboarding',
    component: Onboarding,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'azure-services',
    component: AzureServicesComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'vnet-details',
    component: VirtualnetworkDetailsComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'vnet',
    component: VirtualnetworkComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'nsg',
    component: NetworkSecurityGroupComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'sqldb',
    component: SqldatabaseComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'appregistrations',
    component: AppregistrationsComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'keyvaults',
    component: KeyvaultsComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'storageacc',
    component: StorageaccountComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'aks-cluster',
    component: AksClusterComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'vnet-peering',
    component: VnetPeeringComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'policies',
    component: MainPolicyComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'policy',
    component: PolicyComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'assign-policy',
    component: AssignPolicyComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'monitor',
    component: MonitorComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'app-deploy-services',
    component: AppDeployServicesComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'app-services',
    component: AppServicesComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'upload-config',
    component: UploadConfigComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'container-services',
    component: ContainerServicesComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'container-registries',
    component: ContainerRegistriesComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'aks-app-deploy',
    component: AksAppDeployComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  }, {
    path: 'virtual-machine',
    component:  VirtualmachineComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'PaaS',
    component:  PaasComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'SaaS',
    component:  SaasComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'IaaS',
    component:  LaasComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'IaaC',
    component:  IaacComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'user-management',
    component:  UserManagementComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },

  {
    path: 'document',
    component: DocumentComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'devops',
    component: AzureDevopsComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'template',
    component: ArmTemplateComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: 'view-subscription',
    component: SwitchSubscriptionComponent,
    pathMatch: 'full',
    canActivate: [AuthGuard],
  },
  {
    path: '**',
    redirectTo: 'login',
  },


  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
