package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BorrowerEditable extends UserEditable {
  private static final String KEY_EMPLOYER = "employer";
  private static final String KEY_JOB_TITLE = "job_title";
  private static final String KEY_PHONE = "phone";
  private static final Logger LOG = Logger.getLogger(BorrowerEditable.class.getName());

  public String mEmployer = null;
  public String mJobTitle = null;
  public String mPhone = null;

  public BorrowerEditable() {
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_EMPLOYER, mEmployer);
    jsonBuilder.add(KEY_JOB_TITLE, mJobTitle);
    jsonBuilder.add(KEY_PHONE, mPhone);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (super.isValid() &&
            mPhone != null && mPhone.length() > 0 &&
            mEmployer != null && mEmployer.length() > 0 &&
            mJobTitle != null && mJobTitle.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    super.parse(request);

    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mEmployer = jsonObject.getString(KEY_EMPLOYER, null);
      mJobTitle = jsonObject.getString(KEY_JOB_TITLE, null);
      mPhone = jsonObject.getString(KEY_PHONE, null);
    }
  }
}
