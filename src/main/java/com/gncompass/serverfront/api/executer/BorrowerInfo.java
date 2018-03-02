package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerInfo extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public BorrowerInfo(String borrowerUuid) {
    mBorrowerUuid = borrowerUuid;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    // Fetch the borrower
    Borrower borrower = new Borrower().getBorrower(mBorrowerUuid);
    if (borrower != null) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                      borrower.getViewable().toJson());
    } else {
      // This is a server error. Should never fail since this user was authenticated
      HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          1201, "The borrower information failed to be fetched from the repository");
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1200;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for get borrower info";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
