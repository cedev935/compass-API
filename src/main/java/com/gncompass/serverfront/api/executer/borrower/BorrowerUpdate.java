package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.model.BorrowerEditable;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerUpdate extends AbstractExecuter {
  private BorrowerEditable mBorrowerEditable = null;
  private String mBorrowerUuid = null;

  public BorrowerUpdate(String borrowerUuid) {
    mBorrowerEditable = new BorrowerEditable();
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
          1301, "The borrower information failed to be fetched from the repository");
    }

    // Update the borrower information
    if (next) {
      // Fetch from input
      borrower.updateFromEditable(mBorrowerEditable);

      // Update database
      if(borrower.updateDatabase()) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                      borrower.getViewable(false).toJson());
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            1302, "The borrower failed to be updated to the new information");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1300;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for update borrower info";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    if (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid)) {
      mBorrowerEditable.parse(request);
      return mBorrowerEditable.isValid();
    }
    return false;
  }
}
