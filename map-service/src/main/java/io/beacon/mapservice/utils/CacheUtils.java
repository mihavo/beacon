package io.beacon.mapservice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheUtils {
  
  public String getFriendshipListKey(String userId) {
    return String.format("users:%s:friends", userId);
  }
}
