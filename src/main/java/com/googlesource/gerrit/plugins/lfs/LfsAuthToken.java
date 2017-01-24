package com.googlesource.gerrit.plugins.lfs;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;

public abstract class LfsAuthToken {
  public static abstract class LfsAuthTokenProcessor<T extends LfsAuthToken> {
    private static final char DELIMETER = '~';

    protected final LfsCipher cipher;

    protected LfsAuthTokenProcessor(LfsCipher cipher) {
      this.cipher = cipher;
    }

    public String serialize(T token) {
      return cipher.encode(Joiner.on(DELIMETER).join(getValues(token)));
    }

    public Optional<T> deserialize(String input) {
      Optional<String> decoded = cipher.decode(input);
      if (!decoded.isPresent()) {
        return Optional.absent();
      }

      return createToken(Splitter.on(DELIMETER).splitToList(decoded.get()));
    }

    protected abstract List<String> getValues(T token);
    protected abstract Optional<T> createToken(List<String> values);
  }

  public static abstract class LfsAuthTokenVerifier<T extends LfsAuthToken> {
    static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

    protected final T token;

    protected LfsAuthTokenVerifier(T token) {
      this.token = token;
    }

    public boolean verify() {
      return onTime(token.expiresAt)
          && verifyTokenValues();
    }

    protected abstract boolean verifyTokenValues();

    static boolean onTime(String dateTime) {
      String now = ISO.print(now());
      return now.compareTo(dateTime) <= 0;
    }
  }

  public final String expiresAt;

  protected LfsAuthToken(int expirationSeconds) {
    this(timeout(expirationSeconds));
  }

  protected LfsAuthToken(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  static String timeout(int expirationSeconds) {
    return LfsAuthTokenVerifier.ISO.print(now().plusSeconds(expirationSeconds));
  }

  static DateTime now() {
    return DateTime.now().toDateTime(DateTimeZone.UTC);
  }
}