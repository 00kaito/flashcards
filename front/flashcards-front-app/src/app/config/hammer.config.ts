import {Injectable} from '@angular/core';
import {HammerGestureConfig} from '@angular/platform-browser';

@Injectable()
export class HammerConfig extends HammerGestureConfig {
  override overrides = {
    swipe: { direction: 31 }, // Enable all directions
  };
}
