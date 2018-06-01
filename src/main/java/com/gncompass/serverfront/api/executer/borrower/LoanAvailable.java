package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.db.model.Loan;
import com.gncompass.serverfront.db.model.LoanAmortization;
import com.gncompass.serverfront.db.model.LoanFrequency;
import com.gncompass.serverfront.util.Currency;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class LoanAvailable extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public LoanAvailable(String borrowerUuid) {
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
          2701, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the borrower active assessment to make sure it is permitted to create loans
    Assessment activeAssessment = null;
    if (next) {
      next = false;

      if (borrower.mLoanCap > 0.0f) {
        activeAssessment = new Assessment().getLastApproved(borrower);
      }
      if (activeAssessment != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
                      2702, "The borrower has not been approved to request new loans");
      }
    }

    // Fetch the available loan capacity
    // TODO: This should be using the loan status to determine this total
    Currency loanCap = null;
    if (next) {
      // Current loan cap
      loanCap = new Currency(borrower.mLoanCap);

      // Determine the total borrowed
      Currency totalLoaned = new Currency();
      List<Loan> loans = Loan.getAllForBorrower(borrower);
      for (Loan l : loans) {
        totalLoaned = totalLoaned.add(l.mPrincipal);
      }

      // Calculate the loan cap with the total borrowed removed. If less than zero, zero out
      loanCap = loanCap.subtract(totalLoaned);
      if (loanCap.lessThanZero()) {
        loanCap.setToZero();
      }
    }

    // Final fetches and return the result
    if (next) {
      // Fetch the amortizations and the frequencies
      List<com.gncompass.serverfront.api.model.LoanAmortization> loanAmortizations
          = LoanAmortization.getAllAsModel();
      List<com.gncompass.serverfront.api.model.LoanFrequency> loanFrequencies
          = LoanFrequency.getAllAsModel();

      // Assemble and return
      JsonObject loanAvailableJson = new com.gncompass.serverfront.api.model.LoanAvailable(
          loanCap.floatValue(), activeAssessment.getApiInfo(true),
          loanAmortizations, loanFrequencies).toJson();
      if (loanAvailableJson != null) {
        HttpHelper.setResponseSuccess(response, loanAvailableJson);
      } else {
        // This is a server error. Should never fail since all the data should be valid here
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            2703, "The loan available information could not be fetched and validated");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2700;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for checking on the loan available info";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
