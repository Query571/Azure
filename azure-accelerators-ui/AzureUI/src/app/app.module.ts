import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NgxSpinnerModule } from 'ngx-spinner';
import { BrowserModule } from '@angular/platform-browser';
import { FilterPipeModule } from 'ngx-filter-pipe';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSelectModule } from '@angular/material/select';
import { TabViewModule } from 'primeng/tabview';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { LoginComponent } from './login/login.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LoginServiceService } from './services/login-service.service';
import { SharedServiceService } from './services/shared-service.service';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import {
  HttpErrorInterceptor,
  HttpRequestInterceptor,
} from 'src/common/apiInterceptor';
import { DatePipe } from "@angular/common";
import { ToastrModule } from 'ngx-toastr';

import {ToastModule} from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { Onboarding } from './onboarding/onboarding.component';
import { AdminService } from './services/admin.service';
import { AzureServicesComponent } from './azure-services/azure-services.component';
import { VirtualnetworkComponent } from './virtualnetwork/virtualnetwork.component';
import { SqldatabaseComponent } from './sqldatabase/sqldatabase.component';
import { AppregistrationsComponent } from './appregistrations/appregistrations.component';
import { KeyvaultsComponent } from './keyvaults/keyvaults.component';
import { StorageaccountComponent } from './storageaccount/storageaccount.component';
import { VnetPeeringComponent } from './virtualnetwork/vnet-peering/vnet-peering.component';
import { VnetPeeringService } from './services/vnet-peering.service';
import { KeyVaultService } from "./services/keyvault.service";
import { AuthGuard } from './guard/auth.guard';
import { AksClusterComponent } from './aks-cluster/aks-cluster.component';
import { PolicyComponent } from './policy/policy.component';
import { AssignPolicyComponent } from './policy/assign-policy/assign-policy.component';
import { SelectDropDownModule } from 'ngx-select-dropdown';
import { MonitorComponent } from './monitor/monitor.component';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { AppDeployServicesComponent } from "./app-deploy-services/app-deploy-services.component";
import { AppServicesComponent } from './app-services/app-services.component';
import { UploadConfigComponent } from './upload-config/upload-config.component';
import { ContainerServicesComponent } from './container-services/container-services.component';
import { ContainerRegistriesComponent } from './container-registries/container-registries.component';
import { ResourcegroupModalComponent } from './resourcegroup-modal/resourcegroup-modal.component';
import { AksAppDeployComponent } from './aks-app-deploy/aks-app-deploy.component';
import { NoRightClickDirective } from '../directives/no-right-click.directive';
import { ConnectionService } from 'ng-connection-service';
import { DeleteModalComponent } from './modals/delete-modal.component';
import { BlockCopyPasteDirective } from '../directives/block-copy-paste.directive';
import { SortDirective } from '../directives/sort.directive';
import { VirtualmachineComponent } from './virtualmachine/virtualmachine.component';
import { NetworkSecurityGroupComponent } from './virtualnetwork/network-security-group/network-security-group.component';
import { VirtualnetworkDetailsComponent } from './virtualnetwork/virtualnetwork-details/virtualnetwork-details.component';
import { MainPolicyComponent } from './policy/main-policy/main-policy.component';
import { SaasComponent } from './azure-services/saas/saas.component';
import { LaasComponent } from './azure-services/laas/laas.component';
import { PaasComponent } from './azure-services/paas/paas.component';
import { IaacComponent } from './azure-services/iaac/iaac.component';
import { KeysComponent } from './keyvaults/keys/keys.component';
import { SecretsComponent } from './keyvaults/secrets/secrets.component';
import { CertificatesComponent } from './keyvaults/certificates/certificates.component';
import { SessionTimeoutComponent } from './session-timeout/session-timeout.component';
import { DialogModule } from "primeng/dialog";
import { ButtonModule } from "primeng/button";
import { RadioButtonModule } from "primeng/radiobutton";
import { UserManagementComponent } from './user-management/user-management.component';
import { DocumentComponent } from './document/document.component';
import { AzureDevopsComponent } from './azure-devops/azure-devops.component';
import { PipelineComponent } from './azure-devops/pipeline/pipeline.component';
import { ArmTemplateComponent } from './arm-template/arm-template.component';
import { SwitchSubscriptionComponent } from './switch-subscription/switch-subscription.component';
import { ReleaseComponent } from './azure-devops/release/release.component';
import { StagesComponent } from './azure-devops/stages/stages.component';
import { BuildLogsComponent } from './azure-devops/build-logs/build-logs.component';
import { ReleaseLogComponent } from './azure-devops/release-log/release-log.component';
import { PipelineRelaeseLogComponent } from './azure-devops/pipeline-relaese-log/pipeline-relaese-log.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    Onboarding,
    AzureServicesComponent,
    VirtualnetworkComponent,
    SqldatabaseComponent,
    AppregistrationsComponent,
    KeyvaultsComponent,
    StorageaccountComponent,
    VnetPeeringComponent,
    AksClusterComponent,
    PolicyComponent,
    AssignPolicyComponent,
    MonitorComponent,
    AppDeployServicesComponent,
    AppServicesComponent,
    UploadConfigComponent,
    ContainerServicesComponent,
    ContainerRegistriesComponent,
    ResourcegroupModalComponent,
    AksAppDeployComponent,
    NoRightClickDirective,
    DeleteModalComponent,
    BlockCopyPasteDirective,
    SortDirective,
    VirtualmachineComponent,
    NetworkSecurityGroupComponent,
    VirtualnetworkDetailsComponent,
    MainPolicyComponent,
    SaasComponent,
    LaasComponent,
    PaasComponent,
    IaacComponent,
    KeysComponent,
    SecretsComponent,
    CertificatesComponent,
    SessionTimeoutComponent,
    UserManagementComponent,
    DocumentComponent,
    AzureDevopsComponent,
    PipelineComponent,
    ArmTemplateComponent,
    SwitchSubscriptionComponent,
    ReleaseComponent,
    StagesComponent,
    BuildLogsComponent,
    ReleaseLogComponent,
    PipelineRelaeseLogComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    NoopAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatSelectModule,
    TabViewModule,
    DropdownModule,
    MultiSelectModule,
    MatTooltipModule,
    MatDividerModule,
    NgxSpinnerModule,
    SelectDropDownModule,
    FilterPipeModule,
    NgMultiSelectDropDownModule.forRoot(),
    ToastModule,
    ToastrModule.forRoot({
      maxOpened: 3,
      autoDismiss: true,
    }),
    DialogModule, 
    ButtonModule,
    RadioButtonModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  providers: [
    LoginServiceService,
    SharedServiceService,
    AdminService,
    VnetPeeringService,
    AuthGuard,
    DatePipe,
    ConnectionService,
    MessageService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpRequestInterceptor,
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
