package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.model.BankConnectionNew;
import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Bank;
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

// TODO:
//   [1] The bank create should make sure there is no existing institution, transit, account for
//       a single user already. Return 409 if there is (maybe return the reference?)

public class BankCreate extends AbstractExecuter {
  private BankConnectionNew mBankRequest = null;
  private String mBorrowerUuid = null;

  public BankCreate(String borrowerUuid) {
    mBankRequest = new BankConnectionNew();
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
          2001, "The borrower information failed to be fetched from the repository");
    }

    // Validate the bank requested
    Bank bank = null;
    if (next) {
      next = false;

      bank = new Bank().getBank(mBankRequest.mBankId, borrower.mCountryId);
      if (bank != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
            2003, "The bank requested is not a valid available bank within the borrowers country");
      }
    }

    // Create the bank connection
    if (next) {
      BankConnection bankConnection = new BankConnection(mBankRequest);
      bankConnection.mBank = bank;
      if (bankConnection.addToDatabase(borrower)) {
        // Created
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED,
                                      bankConnection.getApiSummary().toJson());
      } else {
        // Failed to create
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            2002, "The bank connection failed to be created for the selected borrower");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating a bank";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mBankRequest.parse(request);
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid) && mBankRequest.isValid());
  }
}
