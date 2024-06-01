import { Injectable } from "@angular/core";
import { ToastrService } from "ngx-toastr";

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  constructor(private toastr: ToastrService) {}

  public success(msg = null) {
    if (msg == null) {
      this.toastr.success("Akcja zakończona sukcesem!");
    } else {
      this.toastr.success("Akcja zakończona sukcesem! " + msg);
    }
  }

  public failure(err = null) {
    console.log(err);
    var msg = null;
    if (err && err.error && err && err.error.message) msg = err.error.message;

    if (msg == null) {
      this.toastr.error("Błąd");
    } else {
      this.toastr.error("Błąd: " + msg);
    }
  }

  public showMessage(message: string) {
    this.toastr.info(message);
  }

  public showErrorMessage(errMsg: string) {
    this.toastr.error(errMsg);
  }
}
