package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class AssessmentInfo extends AbstractExecuter {
  private String mAssessmentUuid = null;
  private String mBorrowerUuid = null;

  public AssessmentInfo(String borrowerUuid, String assessmentUuid) {
    mAssessmentUuid = assessmentUuid;
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
          1701, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the assessment
    if (next) {
      Assessment assessment = new Assessment().getAssessment(borrower, mAssessmentUuid);
      if (assessment != null) {
          HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                        assessment.getApiInfo().toJson());
      } else {
        // Bank connection not found error
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            1702, "The assessment for this borrower could not be found");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1700;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching an assessment";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid)
            && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
