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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@Singleton
public class LfsAuthTokenHandler {
  private static final Logger log =
      LoggerFactory.getLogger(LfsAuthTokenHandler.class);
  private static final int IV_LENGTH = 16;
  private static final String ALGORITHM = "AES";
  private static final int KEY_SIZE = 128;
  static final DateTimeFormatter DATE_TIME =
      DateTimeFormat.forPattern("YYYYMMDDHHmmss");

  private final SecureRandom random;
  private final SecretKey key;

  @Inject
  public LfsAuthTokenHandler() {
    this.random = new SecureRandom();
    this.key = generateKey();
  }

  public String generateToken(int expirationSeconds,
      String...params) {
    try {
      byte[] initVector = new byte[IV_LENGTH];
      random.nextBytes(initVector);
      Cipher cipher = cipher(initVector, Cipher.ENCRYPT_MODE);
      return Base64.encodeBytes(Bytes.concat(initVector,
          cipher.doFinal(String.format("%s~%s", Joiner.on('~').join(params),
              timeout(expirationSeconds)).getBytes(UTF_8))));
    } catch (GeneralSecurityException e) {
      log.error("Token generation failed with error", e);
      throw new RuntimeException(e);
    }
  }

  public boolean verifyAgainstToken(String token, String... params) {
    Optional<String> data = decrypt(token);
    if (!data.isPresent()) {
      return false;
    }

    String summary = Joiner.on('~').join(params);
    String prefix = String.format("%s~", summary);
    return data.get().startsWith(prefix)
        && onTime(data.get().substring(prefix.length()), summary);
  }

  public Optional<List<String>> verifyTokenOnTime(String token) {
    Optional<String> data = decrypt(token);
    if (!data.isPresent()) {
      return Optional.absent();
    }

    List<String> values =
        Lists.newArrayList(Splitter.on('~').split(data.get()));
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

  private String timeout(int expirationSeconds) {
    return DATE_TIME.print(now().plusSeconds(expirationSeconds));
  }

  private DateTime now() {
    return DateTime.now().toDateTime(DateTimeZone.UTC);
  }

  private Cipher cipher(byte[] initVector, int mode) throws NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidParameterSpecException,
      InvalidKeyException, InvalidAlgorithmParameterException {
    IvParameterSpec spec = new IvParameterSpec(initVector);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM);
    params.init(spec);
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
