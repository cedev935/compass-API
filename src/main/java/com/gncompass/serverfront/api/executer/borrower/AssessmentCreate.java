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

// TODO:
//   [1] The assessment create should make sure that you are permitted to make an assesment. For
//       example, a new one is permitted only every 3 months on failure and only after loan reaches
//       cap on success. It should return 403 as per documentation

public class AssessmentCreate extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public AssessmentCreate(String borrowerUuid) {
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
          1501, "The borrower information failed to be fetched from the repository");
    }

    // Create the assessment
    if (next) {
      Assessment assessment = new Assessment();
      if (assessment.addToDatabase(borrower)) {
        // Created
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED,
                                      assessment.getApiInfo().toJson());
      } else {
        // Failed to create
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            1502, "The assessment failed to be created for the selected borrower");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1500;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating an assessment";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
