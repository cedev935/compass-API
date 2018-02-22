package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.model.BorrowerEditable;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerUpdate extends AbstractExecuter {
  private BorrowerEditable mBorrowerEditable = null;
  private String mBorrowerUuid = null;

  public BorrowerUpdate(String borrowerUuid) {
    mBorrowerEditable = new BorrowerEditable();
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
    return 1300;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for update borrower info";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    if (mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid)) {
      mBorrowerEditable.parse(request);
      return mBorrowerEditable.isValid();
    }
    return false;
  }
}
