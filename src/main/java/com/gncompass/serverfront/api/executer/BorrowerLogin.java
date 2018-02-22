package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.model.AuthRequest;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerLogin extends AbstractExecuter {
  private AuthRequest mAuthRequest = null;

  public BorrowerLogin() {
    mAuthRequest = new AuthRequest();
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
    return 1100;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for logging into a borrower account";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mAuthRequest.parse(request);
    return mAuthRequest.isValid();
  }
}
