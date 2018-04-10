package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class AssessmentApproved extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public AssessmentApproved(String borrowerUuid) {
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
          2301, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the assessment
    if (next) {
      Assessment assessment = new Assessment().getLastApproved(borrower);
      if (assessment != null) {
          HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                        assessment.getApiInfo(true).toJson());
      } else {
        // Bank connection not found error
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            2302, "The borrower has no approved assessments");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2300;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching the last approved assessment";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
