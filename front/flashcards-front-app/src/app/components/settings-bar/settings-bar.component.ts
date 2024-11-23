import {Component} from '@angular/core';
import {MODE} from '../mode';
import {SharedService} from '../shared.service';
import {MatSelectChange} from '@angular/material/select';

@Component({
  selector: 'app-settings-bar',
  templateUrl: './settings-bar.component.html',
  styleUrl: './settings-bar.component.css',
})
export class SettingsBarComponent {
  learningIsOn: boolean = false;
  rating: number[] = [1, 2, 3, 4, 5];

  constructor(private sharedDataService: SharedService) {}

  toggleLearningMode() {
    console.log('Toggle state:', this.learningIsOn);
    if (this.learningIsOn) {
      this.sharedDataService.setData(MODE.LEARNING);
    } else {
      this.sharedDataService.setData(MODE.PREVIEW);
    }
  }

  showFlashcardsByGrade($event: MatSelectChange) {
    // this.fetchFlashcards($event.value as number)
  }
}
