import { Component, OnInit } from "@angular/core";
import { RestService, SimulationOptions } from "./rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
import { interval } from "rxjs";
import { StateServiceService } from "./state-service.service";

@Component({
  selector: "app-main-view",
  templateUrl: "./main-view.component.html",
  styleUrls: ["./main-view.component.css"],
})
export class MainViewComponent implements OnInit {
  options: SimulationOptions = {
    cooksNumber: 5,
    cookersNumber: 5,
    waitersNumber: 5,
    timeScale: 100,
    seatsToNumberOfTablesMap: {},
  };

  seatsToNumberOfTablesRaw: String = '{ \n \t"4": 10,\n \t"2": 15 \n}';

  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateService: StateServiceService
  ) {}

  startSimulation() {
    try {
      this.options.seatsToNumberOfTablesMap = JSON.parse(
        this.seatsToNumberOfTablesRaw.toString()
      );
    } catch (e) {
      console.log(e);
      this.notifyService.showErrorMessage("Bład parsowania json - " + e);
      return;
    }

    this.restService.startSimulation(this.options).subscribe(
      (res) => {
        this.notifyService.success();
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }

  ngOnInit() {
    this.restService.getOptions().subscribe(
      (res) => {
        if (res == null) return;
        this.options = res;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );

    interval(100).subscribe((tmp) => {
      this.refreshView();
    });
  }

  currentResteurantState: any;

  getCurrentResteurantState() {
    return JSON.stringify(this.currentResteurantState);
  }

  refreshView() {
    this.restService.getRestaurantView().subscribe(
      (res) => {
        this.currentResteurantState = res;
        this.stateService.currentResteurantState = res;
      },
      (err) => {
        this.notifyService.failure(err);
      }
    );
  }
}
