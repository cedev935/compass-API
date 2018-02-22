package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

public class AuthRequest extends AbstractModel {
  private static final String KEY_DEVICE_ID = "device_id";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_PASSWORD = "password";
  private static final Logger LOG = Logger.getLogger(AuthRequest.class.getName());

  public String mDeviceId = null;
  public String mEmail = null;
  public String mPassword = null;

  public AuthRequest() {
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mDeviceId != null && StringHelper.isUuid(mDeviceId) &&
            mEmail != null && StringHelper.isEmail(mEmail) &&
            mPassword != null && mPassword.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mDeviceId = jsonObject.getString(KEY_DEVICE_ID, null);
      mEmail = jsonObject.getString(KEY_EMAIL, null);
      mPassword = jsonObject.getString(KEY_PASSWORD, null);
    }
  }
}
