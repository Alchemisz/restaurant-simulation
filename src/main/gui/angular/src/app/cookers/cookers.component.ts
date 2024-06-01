import { Component, OnInit } from "@angular/core";
import { StateServiceService } from "../main-view/main-view/state-service.service";

@Component({
  selector: "app-cookers",
  templateUrl: "./cookers.component.html",
  styleUrls: ["./cookers.component.css"],
})
export class CookersComponent implements OnInit {
  constructor(private stateService: StateServiceService) {}

  ngOnInit() {}

  public getCurrentResteurantState() {
    return this.stateService.currentResteurantState;
  }
}
