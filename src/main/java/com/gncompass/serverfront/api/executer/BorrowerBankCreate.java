package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerBankCreate extends AbstractExecuter {
  private String mBorrowerUuid = null;

  public BorrowerBankCreate(String borrowerUuid) {
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
    HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED, jsonResponse);
  }

  @Override
  protected int getInvalidErrorCode() {
    return 2000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating a bank";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
