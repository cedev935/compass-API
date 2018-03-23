package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerAssessmentSubmit extends AbstractExecuter {
  private String mAssessmentUuid = null;
  private String mBorrowerUuid = null;

  public BorrowerAssessmentSubmit(String borrowerUuid, String assessmentUuid) {
    mAssessmentUuid = assessmentUuid;
    mBorrowerUuid = borrowerUuid;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    // TODO! Implement
    JsonObject jsonResponse = Json.createObjectBuilder()
        .add("code", 4)
        .add("type", "ok")
        .add("message", "magic!")
        .build();
    HttpHelper.setResponseSuccess(response, jsonResponse);
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
