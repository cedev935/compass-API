package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.model.LoanNew;
import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.db.model.BankConnection;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.db.model.Loan;
import com.gncompass.serverfront.db.model.LoanAmortization;
import com.gncompass.serverfront.db.model.LoanFrequency;
import com.gncompass.serverfront.util.Currency;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class LoanCreate extends AbstractExecuter {
  private String mBorrowerUuid = null;
  private LoanNew mLoanRequest = null;

  public LoanCreate(String borrowerUuid) {
    mBorrowerUuid = borrowerUuid;
    mLoanRequest = new LoanNew();
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
          2601, "The borrower information failed to be fetched from the repository");
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
                      2607, "The borrower has not been approved to request new loans");
      }
    }

    // Validate the bank connection requested
    BankConnection bankConnection = null;
    if (next) {
      next = false;

      bankConnection = new BankConnection().getBankConnection(borrower, mLoanRequest.mBankUuid);
      if (bankConnection != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
                      2604, "The bank connection requested is not a valid bank for the borrower");
      }
    }

    // Validate the amortization requested
    LoanAmortization loanAmortization = null;
    if (next) {
      next = false;

      loanAmortization = new LoanAmortization().getForId(mLoanRequest.mAmortizationId);
      if (loanAmortization != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
                  2605, "The amortization selected for this loan is not a valid available choice");
      }
    }

    // Validate the frequency requested
    LoanFrequency loanFrequency = null;
    if (next) {
      next = false;

      loanFrequency = new LoanFrequency().getForId(mLoanRequest.mFrequencyId);
      if (loanFrequency != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
            2606, "The frequency selected for this loan is not a valid available frequecy choice");
      }
    }

    // Validate the principal amount requested
    // TODO: This should be using the loan status to determine this total
    Currency loanAmount = null;
    if (next) {
      next = false;

      // Determine the total borrowed
      Currency totalLoaned = new Currency();
      List<Loan> loans = Loan.getAllForBorrower(borrower);
      for (Loan l : loans) {
        totalLoaned = totalLoaned.add(l.mPrincipal);
      }

      // Determine if it has been exceeded
      loanAmount = new Currency(mLoanRequest.mPrincipal);
      Currency loanCap = new Currency(borrower.mLoanCap);
      if (!loanCap.subtract(totalLoaned).subtract(loanAmount).lessThanZero()) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
                      2603, "The loan amount is higher than the permitted range for the borrower");
      }
    }

    // Create the loan. The reason this doesn't need to be a transaction is due to the temp code
    // below is just for during the MVP stage. It will be removed and a loan will just be made
    // pending until fulfillment. [ It also sets the started date to now (TEMP) ]
    Loan createdLoan = null;
    if (next) {
      next = false;

      // Create the loan widget
      createdLoan = new Loan(loanAmount, activeAssessment.mRatingId,
                             activeAssessment.mRating.mLoanRate);
      createdLoan.mBankConnection = bankConnection;
      createdLoan.mLoanAmortization = loanAmortization;
      createdLoan.mLoanFrequency = loanFrequency;

      // Add to the database
      if (createdLoan.addForBorrower(borrower)) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                2602, "The loan failed to be created for the selected borrower");
      }
    }

    // Calculate and generate the first loan payment (TEMP)
    if (next) {
      if (createdLoan.generateNextPayment()) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED,
                                      createdLoan.getApiInfo().toJson());
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 2602,
              "The loan payment failed to be generated for the new loan for the selected borrower");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2600;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating a loan";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mLoanRequest.parse(request);
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid) && mLoanRequest.isValid());
  }
}
