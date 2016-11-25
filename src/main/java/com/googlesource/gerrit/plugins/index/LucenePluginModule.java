// Copyright (C) 2016 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.index;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.gerrit.server.index.IndexDefinition;
import com.google.gerrit.server.index.SchemaDefinitions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Helper class to be instantiated once per plugin that has indexes. In order to
 * be instantiated it requires two classes:
 * - {@link LucenePluginVersionManager} unified plugin index versions handler;
 * - {@link IndexesProvider} class that is responsible for providing plugin
 * indexes definition (upon validation against schema);
 * Note that helper registers given provider to supply indexes definitions
 * expected by corresponding version manager. Binding is established over
 * {@link Named} annotation that contains provided name.
 */
public abstract class LucenePluginModule extends LifecycleModule {
  private final Class<? extends IndexesProvider> providerClass;
  private final Class<? extends LucenePluginVersionManager> mgrClass;
  private final String name;

  protected LucenePluginModule(Class<? extends LucenePluginVersionManager> mgrClass,
      Class<? extends IndexesProvider> providerClass,
      String name) {
    this.providerClass = providerClass;
    this.mgrClass = mgrClass;
    this.name = name;
  }

  @Override
  protected void configure() {
    bind(Key.get(new TypeLiteral<List<IndexDefinition<?,?,?>>>(){},
        Names.named(name)))
      .toProvider(providerClass);
    listener().to(mgrClass);
    configureIndexes();
  }

  protected void configureIndexes() {
  }

  protected static class IndexesProvider
    implements Provider<List<IndexDefinition<?, ?, ?>>> {
    private final Collection<SchemaDefinitions<?>> schemas;
    private final List<IndexDefinition<?,?,?>> indexes;

    protected IndexesProvider(Collection<SchemaDefinitions<?>> schemas,
        List<IndexDefinition<?, ?, ?>> indexes) {
      this.schemas = schemas;
      this.indexes = indexes;
    }

    @Override
    public List<IndexDefinition<?, ?, ?>> get() {
      Set<String> expected = FluentIterable.from(schemas)
          .transform(new Function<SchemaDefinitions<?>, String>() {
            @Override
            public String apply(SchemaDefinitions<?> in) {
              return in.getName();
            }
          }).toSet();
      Set<String> actual = FluentIterable.from(indexes)
          .transform(new Function<IndexDefinition<?, ?, ?>, String>() {
            @Override
            public String apply(IndexDefinition<?, ?, ?> in) {
              return in.getName();
            }
          }).toSet();
      if (!expected.equals(actual)) {
        throw new ProvisionException(
            "need index definitions for all schemas: "
            + expected + " != " + actual);
      }
      return indexes;
    }
  }
}
