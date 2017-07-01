import { Component, OnInit, OnDestroy, Input, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { EventManager, ParseLinks, PaginationUtil, JhiLanguageService, AlertService } from 'ng-jhipster';

import { StatusMessage, StatusMessageDTO } from './status-message.model';
import { StatusMessageService } from './status-message.service';
import { ITEMS_PER_PAGE, Principal, ResponseWrapper } from '../../shared';
import { PaginationConfig } from '../../blocks/config/uib-pagination.config';
import {LocationDialogComponent} from '../location/location-dialog.component';
import {LocationService} from '../location/location.service';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import {Location} from '../location/location.model';
import {Observable} from 'rxjs/Observable';
import {Poll} from '../poll/poll.model';
import {PollAnswer} from '../poll-answer/poll-answer.model';
import {PollService} from '../poll/poll.service';
import {PollAnswerService} from '../poll-answer/poll-answer.service';
import { Http, Response } from '@angular/http';
import {UploadService} from './upload.service';

@Component({
    selector: 'jhi-status-message',
    templateUrl: './status-message.component.html',
    styleUrls: ['../../../content/scss/sm.css'],
    providers: [UploadService]
})

export class StatusMessageComponent implements OnInit, OnDestroy {

    //#region Variables
    @Input() multiple = false;
    @ViewChild('fileinput') inputEl: ElementRef;
    private resourceUrl = '/api/file';
    @ViewChild('fileInput') fileInput;
    profile = {};
    statusM: StatusMessage = new StatusMessage();
    statusMessage: StatusMessageDTO = new StatusMessageDTO();
    inputAnswers: string[] = ['', ''];
    statusMessages: StatusMessage[];
    currentAccount: any;
    eventSubscriber: Subscription;
    currentSearch: string;
    isLocation = true;
    isPhoto = true;
    isPoll = true;
    lat: number;
    lng: number;
    isSaving: boolean;
    isShareDisabled = false;
    options = {
        enableHighAccuracy: true,
        timeout: 3000,
        maximumAge: 0
    };

    //#endregion

    //#region Photos

    onChange(event) {
        console.log('onChange');
        const files = event.srcElement.files;
        console.log(files);
        this.subscribeToSaveResponse(this.fileChange(event), true);
    }

    fileChange(event) {
        const fileList: FileList = event.target.files;
        if (fileList.length > 0) {
            const formData: FormData = new FormData();
            for (let i = 0; i < fileList.length; i++) {
                formData.append('file', fileList[i], fileList[i].name);
            }
            this.http.post(this.resourceUrl, formData)
                .map((res: Response) => res.json())
                .catch((error) => Observable.throw(error))
                .subscribe(
                    (data) => console.log('success'),
                    (error) => console.log(error)
                );
        }
    }

    //#endregion

    //#region Constructor
    constructor(
        private statusMessageService: StatusMessageService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private activatedRoute: ActivatedRoute,
        private principal: Principal,
        private http: Http,
        private service: UploadService
) {
        this.currentSearch = activatedRoute.snapshot.params['search'] ? activatedRoute.snapshot.params['search'] : '';
        this.service.progress$.subscribe((data) => {
                console.log('progress = ' + data);
            });
    }
    //#endregion

    //#region Location
    getAdress() {
        return this.http.get('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + this.lat +
            '&lon=' + this.lng + '&addressdetails=3');
        }

    removeLocation() {
        this.statusMessage.location_address = null;
        this.isLocation = true;
    }

    getLocation() {
        navigator.geolocation.getCurrentPosition(this.successCallback, this.errorCallback, this.options);
    }

    successCallback = (position) => {
        this.lat = position.coords.latitude;
        this.lng = position.coords.longitude;
        console.log(this.lng, this.lat);
        this.subscribeToSaveResponse(this.getAdress(), true);
    }

    errorCallback = (error) => {
        let errorMessage = 'Unknown error';
        switch (error.code) {
            case 1:
                errorMessage = 'Permission denied';
                break;
            case 2:
                errorMessage = 'Position unavailable';
                break;
            case 3:
                errorMessage = 'Timeout';
                break;
        }
        console.log(errorMessage);
    }

    private subscribeToSaveResponse(result: any, isCreated: boolean) {
        result.subscribe((res: any) =>
            this.onSaveSuccess(res, isCreated), (res: Response) => this.onSaveError(res));
    }

    private onSaveSuccess(result: any, isCreated: boolean) {
        this.alertService.success(
            isCreated ? 'jDiasApp.location.created'
                : 'jDiasApp.location.updated',
            {param: result['display_name']}, null);

        this.statusMessage.location_address = result['display_name'];
        this.isLocation = false;
        this.eventManager.broadcast({name: 'locationListModification', content: 'OK'});
        this.isSaving = false;
    }
    //#endregion

    //#regionStatusMessage

    saveStatusMessage() {
        for (let i = 0; i < this.inputAnswers.length; i++) {
            if (this.inputAnswers[i] === '') {
                this.inputAnswers.splice(i, 1);
            }
        }
        this.statusMessage.poll_answers = this.inputAnswers;
        this.statusMessage.location_coords = this.lat + ', ' + this.lng;
        this.statusMessage.photos = [];
        this.statusMessage.aspect_ids = [];
        this.statusMessage.status_message = this.statusM;
        this.isSaving = true;
       // this.statusMessage.text = 'test';
        if (this.statusMessage.status_message.id !== undefined) {
            this.subscribeToSaveStatusMessageResponse(
                this.statusMessageService.update(this.statusMessage), false);
        } else {
            this.subscribeToSaveStatusMessageResponse(
                this.statusMessageService.create(this.statusMessage), true);
        }
    }

    private subscribeToSaveStatusMessageResponse(result: Observable<StatusMessageDTO>, isCreated: boolean) {
        result.subscribe((res: StatusMessageDTO) =>
            this.onSaveStatusMessageSuccess(res, isCreated), (res: Response) => this.onSaveError(res));
    }

    private onSaveStatusMessageSuccess(result: StatusMessage, isCreated: boolean) {
        this.alertService.success(
            isCreated ? 'jDiasApp.statusMessage.created'
                : 'jDiasApp.statusMessage.updated',
            { param : result.id }, null);
        this.statusMessageService.find(result.id).subscribe((statusmessage) => {});
        this.eventManager.broadcast({ name: 'statusMessageListModification', content: 'OK'});
        this.isSaving = false;
    }

    //#endregion

    //#region Poll Question

    createPoll() {
        this.isPoll = !this.isPoll;
        if (this.isPoll) {
            this.inputAnswers = ['', ''];
        }
    }

    pushArrayValue() {
       const length = this.inputAnswers.length - 1;
       if (this.inputAnswers[length] !== '') {
           this.inputAnswers.push('');
       }
    }

    trackPoll(index: any, item: any) {
        return index;
    }

    //#endregion

    //#region Other

    loadAll() {
        if (this.currentSearch) {
            this.statusMessageService.search({
                query: this.currentSearch,
                }).subscribe(
                    (res: ResponseWrapper) => this.statusMessages = res.json,
                    (res: ResponseWrapper) => this.onError(res.json)
                );
            return;
       }
        this.statusMessageService.query().subscribe(
            (res: ResponseWrapper) => {
                this.statusMessages = res.json;
                this.currentSearch = '';
            },
            (res: ResponseWrapper) => this.onError(res.json)
        );
    }

    search(query) {
        if (!query) {
            return this.clear();
        }
        this.currentSearch = query;
        this.loadAll();
    }

    clear() {
        this.currentSearch = '';
        this.loadAll();
    }

    ngOnInit() {
        this.isSaving = false;
        this.loadAll();
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });
        this.registerChangeInStatusMessages();
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    trackId(index: number, item: StatusMessage) {
        return item.id;
    }

    registerChangeInStatusMessages() {
        this.eventSubscriber = this.eventManager.subscribe('statusMessageListModification', (response) => this.loadAll());
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
    //#endregion
}
