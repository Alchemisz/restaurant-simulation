import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, Subject } from "rxjs";
import { environment } from "../../../environments/environment";
import { NotificationService } from "src/app/utils/notificationService.service";

@Injectable({
  providedIn: "root",
})
export class RestService {
  restaurantViewSubject: Subject<any> = new Subject();

  constructor(
    private http: HttpClient,
    private notifyService: NotificationService
  ) {}

  public startSimulation(options: SimulationOptions) {
    this.http
      .post(environment.server_url + "/restaurant/start", options)
      .subscribe(
        (res) => {
          this.notifyService.success();
          this.refreshView();
        },
        (err) => {
          this.notifyService.failure(err);
        }
      );
  }

  public getRestaurantView(): Observable<any> {
    return this.http.get(environment.server_url + "/restaurant/read");
  }

  public refreshView() {
    this.http
      .get(environment.server_url + "/restaurant/read")
      .subscribe((data) => this.restaurantViewSubject.next(data));
  }

  public getOptions(): Observable<any> {
    return this.http.get(environment.server_url + "/restaurant/options");
  }
}

export class SimulationOptions {
  cooksNumber: number;
  cookersNumber: number;
  waitersNumber: number;
  timeScale: number;
  seatsToNumberOfTablesMap: any;
}
