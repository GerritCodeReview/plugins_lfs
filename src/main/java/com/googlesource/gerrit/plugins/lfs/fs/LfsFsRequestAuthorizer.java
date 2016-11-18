/*
 * Copyright 2016 CollabNet, Inc. All rights reserved.
 * http://www.collab.net
 */
package com.googlesource.gerrit.plugins.lfs.fs;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@Singleton
public class LfsFsRequestAuthorizer {
  private static final Logger log = LoggerFactory.getLogger(LfsFsRequestAuthorizer.class);
  private static final int IV_LENGTH = 16;
  private static final String ALGORITHM = "AES";

  private final SecureRandom rndm;
  private final Optional<SecretKey> key;
  private final int timeout;

  @Inject
  LfsFsRequestAuthorizer() {
    this.rndm = new SecureRandom();
    this.key = generateKey();
    this.timeout = 10;
  }

  public String generateToken(String operation, AnyLongObjectId id) {
    if (key.isPresent()) {
      try {
        byte [] initVector = new byte[IV_LENGTH];
        rndm.nextBytes(initVector);
        Cipher cipher = cipher(initVector, Cipher.ENCRYPT_MODE);
        return Base64.encodeBytes(Bytes.concat(initVector,
            cipher.doFinal(String.format("%s-%s-%s", operation,
                id.name(), timeout()).getBytes(StandardCharsets.UTF_8))));
      } catch (GeneralSecurityException e) {
        log.error("Token generation failed with error", e);
      }
    }

    return "not:required";
  }

  public boolean verifyAgainstToken(String token, String operation,
      AnyLongObjectId id) {
    if (!key.isPresent()) {
      return true;
    }

    if (Strings.isNullOrEmpty(token)) {
      return false;
    }

    byte[] bytes = Base64.decode(token);
    byte[] initVector = Arrays.copyOf(bytes, IV_LENGTH);
    try {
      Cipher cipher = cipher(initVector, Cipher.DECRYPT_MODE);
      String data = new String(
          cipher.doFinal(Arrays.copyOfRange(bytes, IV_LENGTH, bytes.length)),
          StandardCharsets.UTF_8);
      String oid = id.name();
      String prefix = String.format("%s-%s-", operation, oid);
      return data.startsWith(prefix)
          && onTime(data.substring(prefix.length()), operation, oid);
    } catch (GeneralSecurityException e) {
      log.error("Exception was thrown during token verification", e);
    }

    return false;
  }

  private boolean onTime(String dateTime, String operation, String id) {
    DateTime deadline =
        ISODateTimeFormat.dateTimeParser().parseDateTime(dateTime);
    if (deadline.isBeforeNow()) {
      log.info("Operation {} on id {} timed out", operation, id);
      return false;
    }

    return true;
  }

  private String timeout() {
    DateTime now = now();
    return ISODateTimeFormat.dateTime().print(now.plusSeconds(timeout));
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
    cipher.init(mode, key.get(), params);
    return cipher;
  }

  private Optional<SecretKey> generateKey() {
    try {
      KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
      generator.init(128, rndm);
      return Optional.of(generator.generateKey());
    } catch (NoSuchAlgorithmException e) {
      log.error("Generating key failed with error", e);
    }
    return Optional.absent();
  }
}
