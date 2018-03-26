package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.db.model.Assessment;
import com.gncompass.serverfront.db.model.AssessmentFile;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class UploadedAssessmentFile extends AbstractExecuter {
  private static final Logger LOG = Logger.getLogger(UploadedAssessmentFile.class.getName());

  // Inputs
  private String mAssessmentUuid = null;
  private Map<String, List<BlobKey>> mBlobs = null;

  // Control
  private BlobInfoFactory mInfoFactory = new BlobInfoFactory();
  private BlobstoreService mStoreService = BlobstoreServiceFactory.getBlobstoreService();
  private Storage mStorage = StorageOptions.getDefaultInstance().getService();

  public UploadedAssessmentFile(String assessmentUuid) {
    mAssessmentUuid = assessmentUuid;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    boolean next = false;

    // Validate that the assessment exists
    Assessment assessment = new Assessment().getAssessment(null, mAssessmentUuid);
    if (assessment != null) {
      // Check if the assessment can permit uploads
      if (assessment.canUpload()) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_FORBIDDEN,
            10002, "The assessment for this upload file is not permitted to accept uploads");
      }
    } else {
      HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
          10001, "The assessment for this upload file was not found");
    }

    // Proceed to process the files
    // TODO: Better handling in the event of storage failure. IO stream is one shot to fail
    if (next) {
      for (Map.Entry<String, List<BlobKey>> entry : mBlobs.entrySet()) {
        for (BlobKey bk : entry.getValue()) {
          // Load the blob key info
          com.google.appengine.api.blobstore.BlobInfo info = mInfoFactory.loadBlobInfo(bk);
          boolean validFile = StringHelper.isMime(info.getContentType());

          // Use Storage API to locate the file to the correct location
          String blobName = (entry.getKey() + "-" + info.getFilename()).toLowerCase();
          String blobPath = AssessmentFile.getStoragePath(mAssessmentUuid, blobName);
          if (validFile) {
            BlobstoreInputStream content = new BlobstoreInputStream(bk);
            BlobInfo blobInfo = BlobInfo.newBuilder(HttpHelper.BUCKET_UPLOADS, blobPath)
                                              .setContentType(info.getContentType()).build();
            // TODO: Is there a better option? This function is currently deprecated (see above TODO)
            Blob blob = mStorage.create(blobInfo, content);
          } else {
            LOG.log(Level.WARNING, "Received invalid file content type: " + info.getContentType());
          }

          // Delete the blobkey
          mStoreService.delete(bk);

          // Add or update in database
          if (validFile) {
            AssessmentFile newFile = new AssessmentFile(
                                        HttpHelper.BUCKET_UPLOADS, blobName, info.getContentType());
            AssessmentFile matchingFile = assessment.getFileThatMatches(newFile);
            if (matchingFile != null) {
              matchingFile.updateUploaded();
            } else {
              newFile.addToDatabase(assessment);
            }
          }
        }
      }
    }

    // If it reaches here, note the success to the google API
    HttpHelper.setResponseSuccess(response, null);
  }

  @Override
  protected int getInvalidErrorCode() {
    return 10000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for informing of an uploaded assessment file";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    mBlobs = blobstoreService.getUploads(request);

    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid)
            && mBlobs != null && mBlobs.size() > 0);
  }
}
