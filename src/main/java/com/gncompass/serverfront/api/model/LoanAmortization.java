package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanAmortization extends AbstractModel {
  private static final String KEY_ID = "id";
  private static final String KEY_MONTHS = "months";
  private static final String KEY_NAME = "name";
  private static final Logger LOG = Logger.getLogger(LoanAmortization.class.getName());

  public long mId = 0;
  public int mMonths = 0;
  public String mName = null;

  public LoanAmortization() {
  }

  public LoanAmortization(JsonObject jsonObject) {
    parse(jsonObject);
  }

  public LoanAmortization(long id, String name, int months) {
    mId = id;
    mName = name;
    mMonths = months;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ID, mId);
    jsonBuilder.add(KEY_NAME, mName);
    jsonBuilder.add(KEY_MONTHS, mMonths);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mId > 0 && mName != null && mName.length() > 0 && mMonths > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    parse(getContent(request));
  }

  private void parse(JsonObject jsonObject) {
    if(jsonObject != null) {
      mId = getLongFromJson(jsonObject, KEY_ID, 0L);
      mName = jsonObject.getString(KEY_NAME, null);
      mMonths = jsonObject.getInt(KEY_MONTHS, 0);
    }
  }
}
