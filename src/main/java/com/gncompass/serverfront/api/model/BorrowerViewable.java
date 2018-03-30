package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.util.StringHelper;

import java.util.List;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BorrowerViewable extends UserViewable {
  private static final String KEY_ASSESSMENTS = "assessments";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_EMPLOYER = "employer";
  private static final String KEY_JOB_TITLE = "job_title";
  private static final String KEY_LOAN_CAP = "loan_cap";
  private static final String KEY_PHONE = "phone";
  private static final Logger LOG = Logger.getLogger(BorrowerViewable.class.getName());

  public String mEmail = null;
  public String mEmployer = null;
  public String mJobTitle = null;
  public float mLoanCap = 0.0f;
  public String mPhone = null;

  // Send only parameters
  private List<Assessment> mAssessments = null;

  public BorrowerViewable() {
  }

  public BorrowerViewable(String email, String phone, String employer, String jobTitle,
                          float loanCap) {
    mEmail = email;
    mEmployer = employer;
    mJobTitle = jobTitle;
    mLoanCap = loanCap;
    mPhone = phone;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    super.addToJson(jsonBuilder);

    jsonBuilder.add(KEY_EMAIL, mEmail);
    jsonBuilder.add(KEY_EMPLOYER, mEmployer);
    jsonBuilder.add(KEY_JOB_TITLE, mJobTitle);
    if (mLoanCap > 0.0f) {
      jsonBuilder.add(KEY_LOAN_CAP, mLoanCap);
    }
    jsonBuilder.add(KEY_PHONE, mPhone);

    if (mAssessments != null) {
      JsonArrayBuilder assessmentArrayBuilder = Json.createArrayBuilder();
      for (Assessment a : mAssessments) {
        JsonObjectBuilder objectBuilder = a.getApiSummary().toJsonBuilder();
        if (objectBuilder != null) {
          assessmentArrayBuilder.add(objectBuilder);
        }
      }
      jsonBuilder.add(KEY_ASSESSMENTS, assessmentArrayBuilder);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (super.isValid() &&
            mEmail != null && StringHelper.isEmail(mEmail) &&
            mEmployer != null &&
            mJobTitle != null &&
            mPhone != null);
  }

  @Override
  public void parse(HttpServletRequest request) {
    super.parse(request);

    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mEmail = jsonObject.getString(KEY_EMAIL, null);
      mEmployer = jsonObject.getString(KEY_EMPLOYER, null);
      mJobTitle = jsonObject.getString(KEY_JOB_TITLE, null);
      try {
        mLoanCap = (float) jsonObject.getJsonNumber(KEY_LOAN_CAP).doubleValue();
      } catch (ClassCastException cce) {
        mLoanCap = 0.0f;
      }
      mPhone = jsonObject.getString(KEY_PHONE, null);
    }
  }

  public void setAssessmentData(List<Assessment> assessments) {
    mAssessments = assessments;
  }
}
