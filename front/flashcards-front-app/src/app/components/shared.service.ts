import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {MODE} from './mode';

@Injectable({
  providedIn: 'root',
})
export class SharedService {
  mode$ = new Subject<MODE | number>();

  constructor() {}

  setData(data: MODE | number) {
    this.mode$.next(data);
  }
}
