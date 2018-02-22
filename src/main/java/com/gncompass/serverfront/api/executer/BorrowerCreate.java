package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.model.RegisterRequest;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerCreate extends AbstractExecuter {
  private RegisterRequest mRegisterRequest = null;
  private Boolean mValidated = null;

  public BorrowerCreate() {
    mRegisterRequest = new RegisterRequest();
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    // TODO! Implement
    JsonObject jsonResponse = Json.createObjectBuilder()
        .add("code", 4)
        .add("type", "ok")
        .add("message", "magic!")
        .build();
    HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED, jsonResponse);
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating a borrower";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mRegisterRequest.parse(request);
    return mRegisterRequest.isValid();
  }
}
