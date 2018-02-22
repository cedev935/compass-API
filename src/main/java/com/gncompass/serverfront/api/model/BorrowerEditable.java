package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BorrowerEditable extends AbstractModel {
  private static final String KEY_ADDRESS1 = "address1";
  private static final String KEY_ADDRESS2 = "address2";
  private static final String KEY_ADDRESS3 = "address3";
  private static final String KEY_CITY = "city";
  private static final String KEY_EMPLOYER = "employer";
  private static final String KEY_JOB_TITLE = "job_title";
  private static final String KEY_NAME = "name";
  private static final String KEY_PHONE = "phone";
  private static final String KEY_POST_CODE = "post_code";
  private static final String KEY_PROVINCE = "province";
  private static final Logger LOG = Logger.getLogger(BorrowerEditable.class.getName());

  public String mAddress1 = null;
  public String mAddress2 = null;
  public String mAddress3 = null;
  public String mCity = null;
  public String mEmployer = null;
  public String mJobTitle = null;
  public String mName = null;
  public String mPhone = null;
  public String mPostCode = null;
  public String mProvince = null;

  public BorrowerEditable() {
  }

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
    jsonBuilder.add(KEY_EMPLOYER, mEmployer);
    jsonBuilder.add(KEY_JOB_TITLE, mJobTitle);
    jsonBuilder.add(KEY_NAME, mName);
    jsonBuilder.add(KEY_PHONE, mPhone);
    if (mPostCode != null) {
      jsonBuilder.add(KEY_POST_CODE, mPostCode);
    }
    if (mProvince != null) {
      jsonBuilder.add(KEY_PROVINCE, mProvince);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mName != null && mName.length() > 0 &&
            mAddress1 != null && mAddress1.length() > 0 &&
            mCity != null && mCity.length() > 0 &&
            mPhone != null && mPhone.length() > 0 &&
            mEmployer != null && mEmployer.length() > 0 &&
            mJobTitle != null && mJobTitle.length() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mAddress1 = jsonObject.getString(KEY_ADDRESS1, null);
      mAddress2 = jsonObject.getString(KEY_ADDRESS2, null);
      mAddress3 = jsonObject.getString(KEY_ADDRESS3, null);
      mCity = jsonObject.getString(KEY_CITY, null);
      mEmployer = jsonObject.getString(KEY_EMPLOYER, null);
      mJobTitle = jsonObject.getString(KEY_JOB_TITLE, null);
      mName = jsonObject.getString(KEY_NAME, null);
      mPhone = jsonObject.getString(KEY_PHONE, null);
      mPostCode = jsonObject.getString(KEY_POST_CODE, null);
      mProvince = jsonObject.getString(KEY_PROVINCE, null);
    }
  }
}
