package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.HttpHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractModel {
  private JsonObject content = null;

  protected abstract void addToJson(JsonObjectBuilder jsonBuilder);
  protected abstract Logger getLogger();
  public abstract boolean isValid();
  public abstract void parse(HttpServletRequest request);

  protected JsonObject getContent(HttpServletRequest request) {
    if (content == null && HttpHelper.isContentJson(request)) {
      try (InputStream inputStream = request.getInputStream()) {
        try (JsonReader jsonReader = Json.createReader(inputStream)) {
          content = jsonReader.readObject();
        } catch (JsonException je) {
          // Just warn and fall through
          getLogger().log(Level.WARNING, "Failed to parse the JSON content request body", je);
        }
      } catch (IOException ie) {
        // Just warn and fall through
        getLogger().log(Level.WARNING, "Failed to fetch stream of content request body", ie);
      }
    }
    return content;
  }

  protected long getLongFromJson(JsonObject json, String key, long defaultValue) {
    try {
      JsonNumber jsonValue = json.getJsonNumber(key);
      if (jsonValue != null) {
        return jsonValue.longValue();
      }
    } catch (ClassCastException cce) {
      // Ignore and fall through
    }
    return defaultValue;
  }

  public JsonObject toJson() {
    JsonObjectBuilder jsonBuilder = toJsonBuilder();
    if (jsonBuilder != null) {
      return jsonBuilder.build();
    }
    return null;
  }

  public JsonObjectBuilder toJsonBuilder() {
    if(isValid()) {
      JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
      addToJson(jsonBuilder);
      return jsonBuilder;
    }
    return null;
  }
}
