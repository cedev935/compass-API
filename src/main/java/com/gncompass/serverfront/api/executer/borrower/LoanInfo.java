package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.db.model.Loan;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class LoanInfo extends AbstractExecuter {
  private String mBorrowerUuid = null;
  private String mLoanUuid = null;

  public LoanInfo(String borrowerUuid, String loanUuid) {
    mBorrowerUuid = borrowerUuid;
    mLoanUuid = loanUuid;
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
          2501, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the loan info
    if (next) {
      Loan loan = new Loan().getLoan(borrower, mLoanUuid);
      if (loan != null) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                      loan.getApiInfo().toJson());
      } else {
        // Loan not found error
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            2502, "The loan for this borrower could not be found");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2500;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching info on a loan";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid)
            && mLoanUuid != null && StringHelper.isUuid(mLoanUuid));
  }
}
