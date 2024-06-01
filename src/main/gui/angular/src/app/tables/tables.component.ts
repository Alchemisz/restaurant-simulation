import { Component, OnInit } from "@angular/core";
import { StateServiceService } from "../main-view/main-view/state-service.service";

@Component({
  selector: "app-tables",
  templateUrl: "./tables.component.html",
  styleUrls: ["./tables.component.css"],
})
export class RightBarComponent implements OnInit {
  constructor(private stateService: StateServiceService) {}

  ngOnInit() {}

  public getCurrentResteurantState() {
    return this.stateService.currentResteurantState;
  }
}
