package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.BankConnection;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BankInfo extends AbstractExecuter {
  private String mBankUuid = null;
  private String mBorrowerUuid = null;

  public BankInfo(String borrowerUuid, String bankUuid) {
    mBankUuid = bankUuid;
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
          2101, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the bank connection
    if (next) {
      BankConnection bankConnection = new BankConnection().getBankConnection(borrower, mBankUuid);
      if (bankConnection != null) {
          HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                        bankConnection.getApiInfo().toJson());
      } else {
        // Bank connection not found error
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            2102, "The bank for this borrower could not be found");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2100;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching info on a bank";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBankUuid != null && StringHelper.isUuid(mBankUuid)
            && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
