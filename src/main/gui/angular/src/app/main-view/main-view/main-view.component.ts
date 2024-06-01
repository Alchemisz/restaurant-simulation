import { Component, OnInit } from "@angular/core";
import { RestService, SimulationOptions } from "./rest.service";
import { NotificationService } from "src/app/utils/notificationService.service";
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
  currentResteurantState: any;

  isSimulationStarted: boolean = false;

  constructor(
    private restService: RestService,
    private notifyService: NotificationService,
    private stateService: StateServiceService
  ) {}

  startSimulation() {
    this.isSimulationStarted = true;
    try {
      this.options.seatsToNumberOfTablesMap = JSON.parse(
        this.seatsToNumberOfTablesRaw.toString()
      );
    } catch (e) {
      this.notifyService.showErrorMessage("Bład parsowania json - " + e);
      return;
    }

    this.restService.startSimulation(this.options);
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

    this.restService.restaurantViewSubject.subscribe((data) => {
      this.currentResteurantState = data;
      this.stateService.currentResteurantState = data;
      if (this.currentResteurantState.nextStepAvailable) {
        setTimeout(() => this.restService.refreshView(), 50);
      } else {
        return;
      }
    });
  }
}
