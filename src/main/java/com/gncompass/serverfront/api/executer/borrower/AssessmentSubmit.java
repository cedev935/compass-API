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

public class AssessmentSubmit extends AbstractExecuter {
  private String mAssessmentUuid = null;
  private String mBorrowerUuid = null;

  public AssessmentSubmit(String borrowerUuid, String assessmentUuid) {
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
          1901, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the assessment
    Assessment assessment = null;
    if (next) {
      assessment = new Assessment().getAssessment(borrower, mAssessmentUuid);
      if (assessment != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            1902, "The assessment for this borrower could not be found");
      }
    }

    // Validate if the assessment can be submitted and submit it
    if (next) {
      if (assessment.canBeSubmitted()) {
        if (assessment.submit()) {
          // Approve the assessment
          // TODO: REMOVE! This is only for testing. Remove once entering into production
          assessment.approveRandomly();

          // Return a successful response
          HttpHelper.setResponseSuccess(response, null);
        } else {
          HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              1904, "The assessment for this borrower failed to be submitted");
        }
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
            1903, "The assessment for this borrower could not be submitted");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1900;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for submitting an assessment";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid)
            && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
