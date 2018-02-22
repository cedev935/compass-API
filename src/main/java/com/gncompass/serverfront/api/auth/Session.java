package com.gncompass.serverfront.api.auth;

import com.gncompass.serverfront.db.model.User.UserType;
import com.gncompass.serverfront.db.model.UserSession;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;
import com.gncompass.serverfront.util.StringHelper.AccessKey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class Session {
  public static final String ACCESS_KEY = "access_key";

  // Static list of all valid session key pairs (for either borrowers or investors)
  private static Map<String, UserSession> sBorrowerSessions = new HashMap<>();
  private static Map<String, UserSession> sInvestorSessions = new HashMap<>();
  private static final Object sSessionLock = new Object();

  // Internals
  private final String mAccessKey;
  private UserSession mUserSession = null;
  private final UserType mType;

  public Session(HttpServletRequest request, UserType type) {
    mAccessKey = request.getHeader(ACCESS_KEY);
    mType = type;

    if(mAccessKey != null) {
      fetchSessionFromCache();
    }
  }

  /*=============================================================
   * PRIVATE FUNCTIONS
   *============================================================*/

  private void addSessionToCache(UserSession session) {
    synchronized (sSessionLock) {
      switch(mType) {
        case BORROWER:
          sBorrowerSessions.put(mAccessKey, session);
          break;
        case INVESTOR:
          sInvestorSessions.put(mAccessKey, session);
          break;
        default:
          throw new RuntimeException("User type not implemented in addSessionToCache()");
      }
    }
  }

  private void fetchSessionFromCache() {
    synchronized (sSessionLock) {
      switch(mType) {
        case BORROWER:
          mUserSession = sBorrowerSessions.get(mAccessKey);
          break;
        case INVESTOR:
          mUserSession = sInvestorSessions.get(mAccessKey);
          break;
        default:
          throw new RuntimeException("User type not implemented in fetchSessionFromCache()");
      }
    }
  }

  /*=============================================================
   * PUBLIC FUNCTIONS
   *============================================================*/

  public boolean isValid() {
    return (mAccessKey != null && mUserSession != null);
  }

  public void updateAccessed() {
    if (isValid()) {
      mUserSession.updateAccessed();
    }
  }

  public boolean validate(HttpServletRequest request) {
    if(mAccessKey != null) {
      // Fetch the user reference from the request
      String userReference = HttpHelper.getUserReference(request, true);
      if(userReference != null && StringHelper.isUuid(userReference)) {
        // If it already thinks its valid from the cache, just verify equivalence
        if(isValid()) {
          // If the user requested doesn't match, nullify session user. Requesting invalid user
          if(!mUserSession.matches(mType, userReference)) {
            mUserSession = null;
          }
        }
        // Otherwise, need to fetch from database
        else {
          AccessKey accessKey = StringHelper.getAccessKey(mAccessKey);
          if(accessKey != null) {
            UserSession userSession = new UserSession().getSession(mType, userReference, accessKey);
            if(userSession != null) {
              addSessionToCache(userSession);
            }
          }
        }
      } else {
        mUserSession = null;
      }
    }

    return isValid();
  }
}
