package com.gncompass.serverfront.util;

import com.google.apphosting.api.ApiProxy;

import java.util.Map;

public class StateHelper {
  private static final String ATTR_HOSTNAME = "com.google.appengine.runtime.default_version_hostname";

  /**
   * Returns the system property with the given name
   * @param name The name of the property
   * @return The value of the property
   */
  public static String getProperty(String name) {
    return System.getProperty(name);
  }

  /**
   * Returns the production property if in production or the development if in production. This
   * function calls the isProduction() within this class to determine which to use.
   * @param productionName The name of the production property
   * @param developmentName The name of the development property
   * @return The value of the property
   */
  public static String getProperty(String productionName, String developmentName) {
    if(isProduction()) {
      return getProperty(productionName);
    } else {
      return getProperty(developmentName);
    }
  }

  /**
   * Is the current build a production build or a development build
   * @return TRUE if production. FALSE if development
   */
  public static boolean isProduction() {
    ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
    Map<String,Object> attr = env.getAttributes();
    String hostname = (String) attr.get(ATTR_HOSTNAME);

    return !hostname.contains("localhost:");
  }
}
