package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.db.model.Rating;
import com.gncompass.serverfront.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class AssessmentInfo extends AbstractModel {
  private static final String KEY_REFERENCE = "reference";
  private static final String KEY_REGISTERED = "registered";
  private static final String KEY_UPDATED = "updated";
  private static final String KEY_STATUS = "status";
  private static final String KEY_RATING = "rating";
  private static final String KEY_RATE = "rate";
  private static final String KEY_UPLOAD_PATH = "upload_path";
  private static final String KEY_FILES = "files";
  private static final Logger LOG = Logger.getLogger(AssessmentInfo.class.getName());

  public String mReference = null;
  public long mRegisteredTime = 0L;
  public long mUpdatedTime = 0L;
  public int mStatusId = 0;
  public int mRatingId = 0;
  public double mRate = 0.0d;
  public String mUploadPath = null;
  private List<AssessmentFile> mFiles = new ArrayList<>();

  public AssessmentInfo() {
  }

  public AssessmentInfo(long registeredTime, long updatedTime, int statusId,
                        int ratingId, String uploadPath) {
    this(null, registeredTime, updatedTime, statusId, ratingId, uploadPath);
  }

  public AssessmentInfo(String reference, long registeredTime, long updatedTime, int statusId,
                        int ratingId, String uploadPath) {
    mReference = reference;
    mRegisteredTime = registeredTime;
    mUpdatedTime = updatedTime;
    mStatusId = statusId;
    mRatingId = ratingId;
    mUploadPath = uploadPath;
  }

  public boolean addFile(AssessmentFile assessmentFile) {
    if (assessmentFile.isValid()) {
      mFiles.add(assessmentFile);
      return true;
    }
    return false;
  }

  public void addRatingInfo(Rating rating) {
    mRate = rating.mLoanRate;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    if (mReference != null) {
      jsonBuilder.add(KEY_REFERENCE, mReference);
    }
    jsonBuilder.add(KEY_REGISTERED, mRegisteredTime);
    jsonBuilder.add(KEY_UPDATED, mUpdatedTime);
    jsonBuilder.add(KEY_STATUS, mStatusId);
    if (mRatingId > 0) {
      jsonBuilder.add(KEY_RATING, mRatingId);
    }
    if (mRate > 0.0d) {
      jsonBuilder.add(KEY_RATE, mRate);
    }
    if (mUploadPath != null) {
      jsonBuilder.add(KEY_UPLOAD_PATH, mUploadPath);
    }

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (AssessmentFile assessmentFile : mFiles) {
      arrayBuilder.add(assessmentFile.toJsonBuilder());
    }
    jsonBuilder.add(KEY_FILES, arrayBuilder);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mRegisteredTime > 0 && mUpdatedTime > 0 && mStatusId > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mReference = jsonObject.getString(KEY_REFERENCE, null);
      mRegisteredTime = getLongFromJson(jsonObject, KEY_REGISTERED, 0L);
      mUpdatedTime = getLongFromJson(jsonObject, KEY_UPDATED, 0L);
      mStatusId = jsonObject.getInt(KEY_STATUS, 0);
      mRatingId = jsonObject.getInt(KEY_RATING, 0);
      mRate = getDoubleFromJson(jsonObject, KEY_RATE, 0.0d);
      mUploadPath = jsonObject.getString(KEY_UPLOAD_PATH, null);

      mFiles.clear();
      try {
        JsonArray filesArray = jsonObject.getJsonArray(KEY_FILES);
        if (filesArray != null) {
          for (int i = 0; i < filesArray.size(); i++) {
            addFile(new AssessmentFile(filesArray.getJsonObject(i)));
          }
        }
      } catch (ClassCastException cce) {
        // Ignore. Fall through
      }
    }
  }
}
