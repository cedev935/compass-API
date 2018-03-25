package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class AssessmentFile extends AbstractModel {
  private static final String KEY_CONTENT_TYPE = "content_type";
  private static final String KEY_FILE_NAME = "file_name";
  private static final String KEY_UPLOADED = "uploaded";
  private static final Logger LOG = Logger.getLogger(AssessmentFile.class.getName());

  public String mContentType = null;
  public String mFileName = null;
  public long mUploadedTime = 0L;

  public AssessmentFile() {
  }

  public AssessmentFile(JsonObject json) {
    parseJson(json);
  }

  public AssessmentFile(String fileName, String contentType, long uploadedTime) {
    mFileName = fileName;
    mContentType = contentType;
    mUploadedTime = uploadedTime;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_FILE_NAME, mFileName);
    jsonBuilder.add(KEY_CONTENT_TYPE, mContentType);
    jsonBuilder.add(KEY_UPLOADED, mUploadedTime);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mFileName != null && mFileName.length() > 0 && mContentType != null
            && StringHelper.isMime(mContentType) && mUploadedTime > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    parseJson(jsonObject);
  }

  private void parseJson(JsonObject jsonObject) {
    if(jsonObject != null) {
      mFileName = jsonObject.getString(KEY_FILE_NAME, null);
      mContentType = jsonObject.getString(KEY_CONTENT_TYPE, null);
      mUploadedTime = getLongFromJson(jsonObject, KEY_UPLOADED, 0L);
    }
  }
}
