package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class AssessmentFile extends AbstractExecuter {
  private String mAssessmentUuid = null;
  private String mBorrowerUuid = null;
  private String mFileName = null;

  public AssessmentFile(String borrowerUuid, String assessmentUuid, String fileName) {
    mAssessmentUuid = assessmentUuid;
    mBorrowerUuid = borrowerUuid;
    mFileName = fileName;
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
    return 1600;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching an assessment file";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid)
            && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid)
            && mFileName != null && mFileName.length() > 0);
  }
}
