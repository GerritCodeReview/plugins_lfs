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

package com.googlesource.gerrit.plugins.lfs.auth;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public abstract class LfsAuthToken {
  public abstract static class Processor<T extends LfsAuthToken> {
    private static final char DELIMETER = '~';
    protected final LfsCipher cipher;

    protected Processor(LfsCipher cipher) {
      this.cipher = cipher;
    }

    public String serialize(T token) {
      return cipher.encrypt(Joiner.on(DELIMETER).join(getValues(token)));
    }

    public Optional<T> deserialize(String input) {
      Optional<String> decrypted = cipher.decrypt(input);
      if (!decrypted.isPresent()) {
        return Optional.empty();
      }
      return createToken(Splitter.on(DELIMETER).splitToList(decrypted.get()));
    }

    protected abstract List<String> getValues(T token);

    protected abstract Optional<T> createToken(List<String> values);
  }

  public abstract static class Verifier<T extends LfsAuthToken> {
    protected final T token;

    protected Verifier(T token) {
      this.token = token;
    }

    public boolean verify() {
      return onTime(Instant.now()) && verifyTokenValues();
    }

    protected abstract boolean verifyTokenValues();

    public boolean onTime(Instant when) {
      return when.isBefore(token.issued.plusSeconds(token.expiresIn));
    }
  }

  public final Instant issued;
  public final Long expiresIn;

  protected LfsAuthToken(Instant issued, Long expiresIn) {
    this.issued = issued;
    this.expiresIn = expiresIn;
  }

  protected LfsAuthToken(String issued, Long expiresIn) {
    this.issued = Instant.parse(issued);
    this.expiresIn = expiresIn;
  }
}
