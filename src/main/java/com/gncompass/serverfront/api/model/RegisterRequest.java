package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class RegisterRequest extends AbstractModel {
  private static final String KEY_COUNTRY = "country";
  private static final String KEY_DEVICE_ID = "device_id";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_NAME = "name";
  private static final String KEY_PASSWORD = "password";
  private static final Logger LOG = Logger.getLogger(RegisterRequest.class.getName());
  private static final int PASSWORD_MIN_LENGTH = 8;

  public String mCountry = null;
  public String mDeviceId = null;
  public String mEmail = null;
  public String mName = null;
  public String mPassword = null;

  public RegisterRequest() {
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_COUNTRY, mCountry);
    jsonBuilder.add(KEY_DEVICE_ID, mDeviceId);
    jsonBuilder.add(KEY_EMAIL, mEmail);
    jsonBuilder.add(KEY_NAME, mName);
    jsonBuilder.add(KEY_PASSWORD, mPassword);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mCountry != null && mCountry.length() == Country.CODE_LENGTH &&
            mDeviceId != null && StringHelper.isUuid(mDeviceId) &&
            mEmail != null && StringHelper.isEmail(mEmail) &&
            mName != null && mName.length() > 0 &&
            mPassword != null && mPassword.length() >= PASSWORD_MIN_LENGTH);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mCountry = jsonObject.getString(KEY_COUNTRY, null);
      mDeviceId = jsonObject.getString(KEY_DEVICE_ID, null);
      mEmail = jsonObject.getString(KEY_EMAIL, null);
      mName = jsonObject.getString(KEY_NAME, null);
      mPassword = jsonObject.getString(KEY_PASSWORD, null);
    }
  }
}
