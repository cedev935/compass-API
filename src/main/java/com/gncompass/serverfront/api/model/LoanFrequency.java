package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanFrequency extends AbstractModel {
  private static final String KEY_DAYS = "days";
  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final String KEY_PER_MONTH = "per_month";
  private static final Logger LOG = Logger.getLogger(LoanFrequency.class.getName());

  public int mDays = 0;
  public long mId = 0;
  public String mName = null;
  public int mPerMonth = 0;

  public LoanFrequency() {
  }

  public LoanFrequency(JsonObject jsonObject) {
    parse(jsonObject);
  }

  public LoanFrequency(long id, String name, int days, int perMonth) {
    mId = id;
    mName = name;
    mDays = days;
    mPerMonth = perMonth;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ID, mId);
    jsonBuilder.add(KEY_NAME, mName);
    if (mDays > 0) {
      jsonBuilder.add(KEY_DAYS, mDays);
    } else if (mPerMonth > 0) {
      jsonBuilder.add(KEY_PER_MONTH, mPerMonth);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mId > 0 && mName != null && mName.length() > 0 && (mDays > 0 || mPerMonth > 0));
  }

  @Override
  public void parse(HttpServletRequest request) {
    parse(getContent(request));
  }

  private void parse(JsonObject jsonObject) {
    if(jsonObject != null) {
      mId = getLongFromJson(jsonObject, KEY_ID, 0L);
      mName = jsonObject.getString(KEY_NAME, null);
      mDays = jsonObject.getInt(KEY_DAYS, 0);
      mPerMonth = jsonObject.getInt(KEY_PER_MONTH, 0);
    }
  }
}
