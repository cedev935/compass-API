package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class Bank extends AbstractModel {
  private static final String KEY_CODE = "code";
  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final Logger LOG = Logger.getLogger(Bank.class.getName());

  public long mId = 0;
  public int mCode = 0;
  public String mName = null;

  public Bank() {
  }

  public Bank(long id, int code, String name) {
    mId = id;
    mCode = code;
    mName = name;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ID, mId);
    jsonBuilder.add(KEY_CODE, mCode);
    jsonBuilder.add(KEY_NAME, mName);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mId > 0 && mCode > 0 &&
            mName != null && mName.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mId = getLongFromJson(jsonObject, KEY_ID, 0L);
      mCode = jsonObject.getInt(KEY_CODE, 0);
      mName = jsonObject.getString(KEY_NAME, null);
    }
  }
}
