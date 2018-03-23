package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class UploadedAssessFile extends AbstractExecuter {
  private String mAssessmentUuid = null;

  public UploadedAssessFile(String assessmentUuid) {
    mAssessmentUuid = assessmentUuid;
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
    return 10000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for informing of an uploaded assessment file";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid));
  }
}
