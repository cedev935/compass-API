package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class ErrorResult extends AbstractModel {
  private static final String KEY_ERROR_CODE = "error_code";
  private static final String KEY_ERROR_STRING = "error_string";
  private static final Logger LOG = Logger.getLogger(ErrorResult.class.getName());

  public int mErrorCode = 0;
  public String mErrorString = null;

  public ErrorResult(int code, String description) {
    mErrorCode = code;
    mErrorString = description;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ERROR_CODE, mErrorCode);
    jsonBuilder.add(KEY_ERROR_STRING, mErrorString);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mErrorCode >= 0 &&
            mErrorString != null && mErrorString.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mErrorCode = jsonObject.getInt(KEY_ERROR_CODE, 0);
      mErrorString = jsonObject.getString(KEY_ERROR_STRING, null);
    }
  }
}
