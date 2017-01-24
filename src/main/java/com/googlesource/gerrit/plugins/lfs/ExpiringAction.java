package com.googlesource.gerrit.plugins.lfs;

import static org.eclipse.jgit.util.HttpSupport.HDR_AUTHORIZATION;

import org.eclipse.jgit.lfs.server.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collections;

public class ExpiringAction extends Response.Action {
  private static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  public final String expiresAt;

  public ExpiringAction(String href, LfsAuthTokenHandler.AuthInfo token) {
    this(href, token.authToken, token.expiresAt);
  }

  public ExpiringAction(String href, String value, DateTime expiresAt) {
    this.href = href;
    this.header = Collections.singletonMap(HDR_AUTHORIZATION,
        value);
    this.expiresAt = ExpiringAction.ISO.print(expiresAt);
  }
}