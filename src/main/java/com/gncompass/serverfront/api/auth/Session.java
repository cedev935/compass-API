package com.gncompass.serverfront.api.auth;

import com.gncompass.serverfront.db.model.User.UserType;
import com.gncompass.serverfront.db.model.UserSession;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;
import com.gncompass.serverfront.util.StringHelper.AccessKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public class Session {
  public static final String ACCESS_KEY = "access_key";

  // Static list of all valid session key pairs (for either borrowers or investors)
  private static Map<String, UserSession.Cache> sBorrowerSessions = new HashMap<>();
  private static Map<String, UserSession.Cache> sInvestorSessions = new HashMap<>();
  private static final Object sSessionLock = new Object();

  // Internals
  private final String mAccessKey;
  private UserSession.Cache mSessionCache = null;
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

  private void addSessionToCache() {
    synchronized (sSessionLock) {
      switch(mType) {
        case BORROWER:
          sBorrowerSessions.put(mAccessKey, mSessionCache);
          break;
        case INVESTOR:
          sInvestorSessions.put(mAccessKey, mSessionCache);
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
          mSessionCache = sBorrowerSessions.get(mAccessKey);
          if (mSessionCache != null && mSessionCache.isExpired()) {
            sBorrowerSessions.remove(mAccessKey);
            mSessionCache = null;
          }
          break;
        case INVESTOR:
          mSessionCache = sInvestorSessions.get(mAccessKey);
          if (mSessionCache != null && mSessionCache.isExpired()) {
            sInvestorSessions.remove(mAccessKey);
            mSessionCache = null;
          }
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
    return (mAccessKey != null && mSessionCache != null);
  }

  public void updateAccessed() {
    if (isValid()) {
      UserSession.updateAccessed(mSessionCache);
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
          if(mSessionCache.matches(userReference)) {
            mSessionCache.updateAccessed();
          } else {
            mSessionCache = null;
          }
        }
        // Otherwise, need to fetch from database
        else {
          AccessKey accessKey = StringHelper.getAccessKey(mAccessKey);
          if(accessKey != null) {
            UserSession userSession = new UserSession().getSession(mType, userReference, accessKey);
            if(userSession != null) {
              mSessionCache = userSession.getCache();
              addSessionToCache();
            }
          }
        }
      } else {
        mSessionCache = null;
      }
    }

    return isValid();
  }

  /*=============================================================
   * STATIC FUNCTIONS
   *============================================================*/

  public static AccessKey getAccessKey(HttpServletRequest request) {
    return StringHelper.getAccessKey(request.getHeader(ACCESS_KEY));
  }

  public static void uncacheBorrower(UUID borrowerReference) {
    synchronized (sSessionLock) {
      for(Iterator<Map.Entry<String, UserSession.Cache>> it = sBorrowerSessions.entrySet().iterator();
          it.hasNext(); ) {
        Map.Entry<String, UserSession.Cache> entry = it.next();
        if(entry.getValue().matches(borrowerReference)) {
          it.remove();
        }
      }
    }
  }

  public static void uncacheBorrower(String borrowerReference) {
    uncacheBorrower(UUID.fromString(borrowerReference));
  }
}
