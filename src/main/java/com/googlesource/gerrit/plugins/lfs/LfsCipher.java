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
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.inject.Singleton;

import org.eclipse.jgit.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@Singleton
public class LfsCipher {
  private static final Logger log =
      LoggerFactory.getLogger(LfsCipher.class);
  private static final int IV_LENGTH = 16;
  private static final String ALGORITHM = "AES";
  private static final String CIPHER_TYPE = ALGORITHM + "/CBC/PKCS5PADDING";
  private static final int KEY_SIZE = 128;

  private final SecureRandom random;
  private final SecretKey key;

  public LfsCipher() {
    this.random = new SecureRandom();
    this.key = generateKey();
  }

  public String encrypt(String input) {
    try {
      byte[] initVector = new byte[IV_LENGTH];
      random.nextBytes(initVector);
      Cipher cipher = cipher(initVector, Cipher.ENCRYPT_MODE);
      return Base64.encodeBytes(
          Bytes.concat(initVector, cipher.doFinal(input.getBytes(UTF_8))));
    } catch (GeneralSecurityException e) {
      log.error("Token generation failed with error", e);
      throw new RuntimeException(e);
    }
  }

  public Optional<String> decrypt(String input) {
    if (Strings.isNullOrEmpty(input)) {
      return Optional.absent();
    }

    byte[] bytes = Base64.decode(input);
    byte[] initVector = Arrays.copyOf(bytes, IV_LENGTH);
    try {
      Cipher cipher = cipher(initVector, Cipher.DECRYPT_MODE);
      return Optional.of(new String(
          cipher.doFinal(Arrays.copyOfRange(bytes, IV_LENGTH, bytes.length)),
          UTF_8));
    } catch (GeneralSecurityException e) {
      log.error("Exception was thrown during token verification", e);
    }

    return Optional.absent();
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
