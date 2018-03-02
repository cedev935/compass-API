package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.auth.Session;
import com.gncompass.serverfront.db.model.User.UserType;
import com.gncompass.serverfront.db.model.UserSession;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;
import com.gncompass.serverfront.util.StringHelper.AccessKey;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class BorrowerLogout extends AbstractExecuter {
  private static final Logger LOG = Logger.getLogger(BorrowerLogout.class.getName());

  private AccessKey mAccessKey = null;
  private String mBorrowerUuid = null;

  public BorrowerLogout(String borrowerUuid) {
    mBorrowerUuid = borrowerUuid;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    // Fetch the session and delete
    UserSession userSession = new UserSession()
                                    .getSession(UserType.BORROWER, mBorrowerUuid, mAccessKey);
    if (userSession != null) {
      if(!userSession.deleteSession()) {
        // Just warn
        LOG.log(Level.WARNING, "Failed to delete any session during borrower logout");
      }
    } else {
      // Just warn
      LOG.log(Level.WARNING, "Failed to fetch any valid session during borrower logout");
    }

    // Clean all cached session for user
    Session.uncacheBorrower(mBorrowerUuid);

    HttpHelper.setResponseSuccess(response, null);
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1400;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for log out of borrower";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mAccessKey = Session.getAccessKey(request);
    return (mAccessKey != null && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid));
  }
}
