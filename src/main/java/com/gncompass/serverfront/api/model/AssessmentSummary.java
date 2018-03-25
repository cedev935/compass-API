package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class AssessmentSummary extends AbstractModel {
  private static final String KEY_DATE = "date";
  private static final String KEY_RATING = "rating";
  private static final String KEY_REFERENCE = "reference";
  private static final String KEY_STATUS = "status";
  private static final Logger LOG = Logger.getLogger(AssessmentSummary.class.getName());

  public long mDateTime = 0L;
  public int mRatingId = 0;
  public String mReference = null;
  public int mStatusId = 0;

  public AssessmentSummary() {
  }

  public AssessmentSummary(String reference, long dateTime, int statusId, int ratingId) {
    mReference = reference;
    mDateTime = dateTime;
    mStatusId = statusId;
    mRatingId = ratingId;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_REFERENCE, mReference);
    jsonBuilder.add(KEY_DATE, mDateTime);
    jsonBuilder.add(KEY_STATUS, mStatusId);
    if (mRatingId > 0) {
      jsonBuilder.add(KEY_RATING, mRatingId);
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mReference != null && StringHelper.isUuid(mReference)
            && mDateTime > 0 && mStatusId > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mReference = jsonObject.getString(KEY_REFERENCE, null);
      mDateTime = getLongFromJson(jsonObject, KEY_DATE, 0L);
      mStatusId = jsonObject.getInt(KEY_STATUS, 0);
      mRatingId = jsonObject.getInt(KEY_RATING, 0);
    }
  }
}
