import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class RestService {
  constructor(private http: HttpClient) {}

  public startSimulation(options: SimulationOptions): Observable<any> {
    return this.http.post(
      environment.server_url + "/restaurant/start",
      options
    );
  }

  public getRestaurantView(): Observable<any> {
    return this.http.get(environment.server_url + "/restaurant/read");
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
