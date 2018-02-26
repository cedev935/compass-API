package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class AuthResponse extends AbstractModel {
  private static final String KEY_SESSION = "session_key";
  private static final String KEY_USER = "user_key";
  private static final Logger LOG = Logger.getLogger(AuthResponse.class.getName());

  public String mSessionKey = null;
  public String mUserKey = null;

  public AuthResponse() {
  }

  public AuthResponse(String sessionKey, String userKey) {
    mSessionKey = sessionKey;
    mUserKey = userKey;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_SESSION, mSessionKey);
    jsonBuilder.add(KEY_USER, mUserKey);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mSessionKey != null && StringHelper.isUuid(mSessionKey) &&
            mUserKey != null && StringHelper.isUuid(mUserKey));
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mSessionKey = jsonObject.getString(KEY_SESSION, null);
      mUserKey = jsonObject.getString(KEY_USER, null);
    }
  }
}
