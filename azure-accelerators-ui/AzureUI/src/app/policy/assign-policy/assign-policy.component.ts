import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatOption } from '@angular/material/core/option';
import { Router } from '@angular/router';
import { NgxSpinnerService } from 'ngx-spinner';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from 'src/app/services/admin.service';
import { PolicyService } from 'src/app/services/policy.service';
import { IDropdownSettings } from 'ng-multiselect-dropdown';
import { SharedServiceService } from 'src/app/services/shared-service.service';

@Component({
  selector: 'app-assign-policy',
  templateUrl: './assign-policy.component.html',
  styleUrls: ['./assign-policy.component.css'],
})
export class AssignPolicyComponent implements OnInit, OnDestroy {
  policyName: string;
  policyId: string;
  policyDescription: string;
  policyDetails: any;
  paramsType: any;
  arrayParam: boolean;
  stringParam: boolean;
  assignPolicyForm: FormGroup;
  policyParams: any = [];
  selectedResourceGroup: string;
  selectedResources: any = [];
  selectedParams: any = [];
  associatedResourceGroup: any = [];
  associatedResources: any = [];
  selectedparameterList: any = [];
  config: any;
  paramConfig: any;
  loadingMsg: string = '';
  submitted: boolean = false;
  dropdownList: any = [];
  selectedItems: any = [];
  resourceDropdown: IDropdownSettings = {};
  parametersDropdown: IDropdownSettings = {};
  @ViewChild('allSelected') private allSelected: MatOption;
  @ViewChild('allLocSelected') private allLocSelected: MatOption;
  showRgModal: boolean = false;
  createdResGrpName: string;

  constructor(
    private policyService: PolicyService,
    private _formBuilder: FormBuilder,
    private adminService: AdminService,
    private toastr: ToastrService,
    private spinner: NgxSpinnerService,
    private router: Router,
    private sharedService: SharedServiceService
  ) {}

  ngOnInit(): void {
    this.stringParam = false;
    this.arrayParam = false;
    this.policyDetails = this.policyService.getPolicyDetails();
    this.policyName = this.policyDetails.displayName;
    let policyType = this.policyDetails.policyType;
    let policyId = this.policyDetails.id;
    this.policyId = this.policyDetails.id;
    this.policyDescription = this.policyDetails.description;
    this.policyService.getPolicyParam(policyId,policyType).subscribe((res) => {
      this.policyParams = res;
      this.paramsType = this.policyParams.type;
      if (this.paramsType == 'String') {
        this.stringParam = true;
      }
      if (this.paramsType == 'Array') {
        this.arrayParam = true;
      }
    });
    this.assignPolicyForm = this._formBuilder.group({
      resourceGroup: ['', Validators.required],
      resourcesList: [''],
      parameters: ['', Validators.required],
    });
    this.getResourcegroup();
    this.config = {
      displayKey: 'description',
      search: true,
      height: '400px',
      placeholder: 'Select Resource Group',
      noResultsFound: 'No results found!',
      searchPlaceholder: 'Search',
      clearOnSelection: true,
    };
    this.paramConfig = {
      displayKey: 'description',
      search: true,
      height: '400px',
      placeholder: 'Select Parameters',
      searchPlaceholder: 'Search Parameters',
      clearOnSelection: true,
    };
    this.resourceDropdown = {
      singleSelection: false,
      idField: 'id',
      textField: 'name',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      searchPlaceholderText: 'Search Resources',
      noDataAvailablePlaceholderText: 'No Resources available',
      itemsShowLimit: 3,
      allowSearchFilter: true,
    };
    this.parametersDropdown = {
      singleSelection: false,
      idField: 'id',
      textField: 'name',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      searchPlaceholderText: 'Search Parameters',
      itemsShowLimit: 3,
      allowSearchFilter: true,
    };
  }

  ngOnDestroy() {
    this.stringParam = false;
    this.arrayParam = false;
  }

  getResourcegroup() {
    this.adminService.getAzureResourceGroupList().subscribe((res) => {
      this.associatedResourceGroup = res;
    });
  }

  selectResourceGroup() {
    this.selectedResourceGroup =
      this.assignPolicyForm.controls['resourceGroup'].value;
    this.policyService
      .getResources(this.selectedResourceGroup)
      .subscribe((response) => {
        this.dropdownList = response;
      });
  }
  addReg() {
    this.showRgModal = true;
  }
  closeResourceGroupModal() {
    this.showRgModal = false;
  }
  resourceGrpResponse() {
    this.createdResGrpName = this.sharedService.getResourceGroup();
    this.assignPolicyForm.patchValue({
      resourceGroup: this.createdResGrpName,
    });
    this.selectResourceGroup();
  }
  selectParameters() {
    this.selectedParams = this.assignPolicyForm.controls['parameters'].value;
    this.selectedParams = [this.selectedParams];
  }

  assignPolicy() {
    if (confirm('Do you want to assign policy?')) {
      this.submitted = true;
      this.loadingMsg = 'Assigning Policy..';
      this.spinner.show();
      if (this.selectedResources.length === 0) {
        this.selectedResources = [null];
      }
      if (this.paramsType == 'null') {
        this.selectedParams = [null];
      }
      let policyInfo = {
        id: this.policyId,
        name: this.policyName,
        description: this.policyDescription,
        excludedScopes: this.selectedResources,
        parameters: this.selectedParams,
        parameterType: this.paramsType,
        resourceGroupName: this.selectedResourceGroup,
      };

      this.policyService.assignPolicy(policyInfo).subscribe(
        (response) => {
          this.assignPolicyForm.reset();
          this.spinner.hide();
          this.sharedService.showSuccess('Policy assigned successfully');
          this.router.navigateByUrl('policy');
        },
        (error: HttpErrorResponse) => {
          this.spinner.hide();
          this.sharedService.showFail(error.error.message);
        }
      );
    }
  }

  onItemSelect(item: any) {
    this.selectedItems = this.assignPolicyForm.controls['resourcesList'].value;
    this.selectedResources = [...this.selectedItems.map((selectItem) => selectItem.id)];
  }

  onSelectAll(items: any) {
    this.selectedItems = items;
    this.selectedResources = [...this.selectedItems.map((selectAllItem) => selectAllItem.id)];
  }

  onItemDeSelect(item: any) {
    this.selectedItems.filter((deselectItem) => deselectItem != deselectItem);
    this.selectedResources = this.selectedItems;
  }

  onParamSelect(item: any) {
    this.selectedparameterList =
      this.assignPolicyForm.controls['parameters'].value;
    this.selectedParams = [...this.selectedparameterList.map((selectParam) => selectParam)];
  }

  onSelectAllParam(items: any) {
    this.selectedparameterList = items;
    this.selectedParams = this.selectedparameterList;
  }

  onParamDeSelect(item: any) {
    this.selectedparameterList.filter((filerItem) => filerItem != filerItem);
    this.selectedParams = [...this.selectedparameterList.map((deselectParam) => deselectParam)];
  }
}
