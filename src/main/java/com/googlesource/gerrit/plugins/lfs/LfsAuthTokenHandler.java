// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.lfs;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.inject.Singleton;

import org.eclipse.jgit.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@Singleton
public class LfsAuthTokenHandler {
  public static abstract class Token {
    protected static final char DELIMETER = '~';

    protected abstract String getValue();

    String getValue(DateTime expiresAt) {
      return new StringBuilder(getValue()).append(DELIMETER)
          .append(DATE_TIME.print(expiresAt)).toString();
    }
  }

  public static class AuthInfo {
    public final String authToken;
    public final DateTime expiresAt;

    AuthInfo(String authToken, DateTime expiresAt) {
      this.authToken = authToken;
      this.expiresAt = expiresAt;
    }
  }

  private static final Logger log =
      LoggerFactory.getLogger(LfsAuthTokenHandler.class);
  private static final int IV_LENGTH = 16;
  private static final String ALGORITHM = "AES";
  private static final String CIPHER_TYPE = ALGORITHM + "/CBC/PKCS5PADDING";
  private static final int KEY_SIZE = 128;
  static final DateTimeFormatter DATE_TIME =
      DateTimeFormat.forPattern("YYYYMMDDHHmmss");

  private final SecureRandom random;
  private final SecretKey key;

  public LfsAuthTokenHandler() {
    this.random = new SecureRandom();
    this.key = generateKey();
  }

  public AuthInfo generateAuthInfo(int expirationSeconds, Token input) {
    try {
      byte[] initVector = new byte[IV_LENGTH];
      random.nextBytes(initVector);
      Cipher cipher = cipher(initVector, Cipher.ENCRYPT_MODE);
      DateTime expiresAt = timeout(expirationSeconds);
      return new AuthInfo(Base64.encodeBytes(Bytes.concat(initVector,
              cipher.doFinal(input.getValue(expiresAt).getBytes(UTF_8)))),
          expiresAt);
    } catch (GeneralSecurityException e) {
      log.error("Token generation failed with error", e);
      throw new RuntimeException(e);
    }
  }

  public boolean verifyAgainstToken(String authToken, Token token) {
    Optional<String> data = decrypt(authToken);
    if (!data.isPresent()) {
      return false;
    }

    String summary = token.getValue();
    String prefix =
        new StringBuilder(summary).append(Token.DELIMETER).toString();
    return data.get().startsWith(prefix)
        && onTime(data.get().substring(prefix.length()), summary);
  }

  public Optional<List<String>> verifyTokenOnTime(String authToken) {
    Optional<String> data = decrypt(authToken);
    if (!data.isPresent()) {
      return Optional.absent();
    }

    List<String> values =
        Lists.newArrayList(Splitter.on(Token.DELIMETER).split(data.get()));
    if (onTime(values.get(values.size() - 1), data.get())) {
      return Optional.of(values);
    }

    return Optional.absent();
  }

  boolean onTime(String dateTime, String summary) {
    String now = DATE_TIME.print(now());
    if (now.compareTo(dateTime) > 0) {
      log.info("Operation {} timed out", summary);
      return false;
    }

    return true;
  }

  private Optional<String> decrypt(String token) {
    if (Strings.isNullOrEmpty(token)) {
      return Optional.absent();
    }

    byte[] bytes = Base64.decode(token);
    byte[] initVector = Arrays.copyOf(bytes, IV_LENGTH);
    try {
      Cipher cipher = cipher(initVector, Cipher.DECRYPT_MODE);
      String data = new String(
          cipher.doFinal(Arrays.copyOfRange(bytes, IV_LENGTH, bytes.length)),
          UTF_8);
      return Optional.of(data);
    } catch (GeneralSecurityException e) {
      log.error("Exception was thrown during token verification", e);
      return Optional.absent();
    }
  }

  private DateTime timeout(int expirationSeconds) {
    return now().plusSeconds(expirationSeconds);
  }

  private DateTime now() {
    return DateTime.now().toDateTime(DateTimeZone.UTC);
  }

  private Cipher cipher(byte[] initVector, int mode)
      throws GeneralSecurityException {
    IvParameterSpec spec = new IvParameterSpec(initVector);
    AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM);
    params.init(spec);
    Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
    cipher.init(mode, key, params);
    return cipher;
  }

  private SecretKey generateKey() {
    try {
      KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
      generator.init(KEY_SIZE, random);
      return generator.generateKey();
    } catch (NoSuchAlgorithmException e) {
      log.error("Generating key failed with error", e);
      throw new RuntimeException(e);
    }
  }
}
