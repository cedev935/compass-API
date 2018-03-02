package com.gncompass.serverfront.api.model;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public abstract class UserEditable extends AbstractModel {
  private static final String KEY_ADDRESS1 = "address1";
  private static final String KEY_ADDRESS2 = "address2";
  private static final String KEY_ADDRESS3 = "address3";
  private static final String KEY_CITY = "city";
  private static final String KEY_NAME = "name";
  private static final String KEY_POST_CODE = "post_code";
  private static final String KEY_PROVINCE = "province";

  public String mAddress1 = null;
  public String mAddress2 = null;
  public String mAddress3 = null;
  public String mCity = null;
  public String mName = null;
  public String mPostCode = null;
  public String mProvince = null;

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ADDRESS1, mAddress1);
    if (mAddress2 != null) {
      jsonBuilder.add(KEY_ADDRESS2, mAddress2);
    }
    if (mAddress3 != null) {
      jsonBuilder.add(KEY_ADDRESS3, mAddress3);
    }
    jsonBuilder.add(KEY_CITY, mCity);
    jsonBuilder.add(KEY_NAME, mName);
    if (mPostCode != null) {
      jsonBuilder.add(KEY_POST_CODE, mPostCode);
    }
    if (mProvince != null) {
      jsonBuilder.add(KEY_PROVINCE, mProvince);
    }
  }

  @Override
  public boolean isValid() {
    return (mName != null && mName.length() > 0 &&
            mAddress1 != null && mAddress1.length() > 0 &&
            mCity != null && mCity.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mAddress1 = jsonObject.getString(KEY_ADDRESS1, null);
      mAddress2 = jsonObject.getString(KEY_ADDRESS2, null);
      mAddress3 = jsonObject.getString(KEY_ADDRESS3, null);
      mCity = jsonObject.getString(KEY_CITY, null);
      mName = jsonObject.getString(KEY_NAME, null);
      mPostCode = jsonObject.getString(KEY_POST_CODE, null);
      mProvince = jsonObject.getString(KEY_PROVINCE, null);
    }
  }
}
