package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class Country extends AbstractModel {
  public static final int CODE_LENGTH = 2;
  private static final String KEY_CODE = "code";
  private static final String KEY_NAME = "name";
  private static final Logger LOG = Logger.getLogger(Country.class.getName());

  public String mCode = null;
  public String mName = null;

  public Country() {
  }

  public Country(String code, String name) {
    mCode = code;
    mName = name;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_CODE, mCode);
    jsonBuilder.add(KEY_NAME, mName);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mCode != null && mCode.length() == CODE_LENGTH &&
            mName != null && mName.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mCode = jsonObject.getString(KEY_CODE, null);
      mName = jsonObject.getString(KEY_NAME, null);
    }
  }
}
