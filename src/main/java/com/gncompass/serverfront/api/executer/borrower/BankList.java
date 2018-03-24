package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.BankConnection;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BankList extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public BankList(String borrowerUuid) {
    mBorrowerUuid = borrowerUuid;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    boolean next = false;

    // Fetch the borrower
    Borrower borrower = new Borrower().getBorrower(mBorrowerUuid);
    if (borrower != null) {
      next = true;
    } else {
      // This is a server error. Should never fail since this user was authenticated
      HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          2201, "The borrower information failed to be fetched from the repository");
    }

    // Fetch all the banks for the borrower
    if (next) {
      List<BankConnection> bankConnections = BankConnection.getAllForUser(borrower);
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      for (BankConnection bc : bankConnections) {
        JsonObjectBuilder objectBuilder = bc.getApiSummary().toJsonBuilder();
        if (objectBuilder != null) {
          arrayBuilder.add(objectBuilder);
        }
      }
      HttpHelper.setResponseSuccess(response, arrayBuilder.build());
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2200;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching all banks";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
