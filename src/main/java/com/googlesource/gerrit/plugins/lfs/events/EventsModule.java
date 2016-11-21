/*
 * Copyright 2016 CollabNet, Inc. All rights reserved.
 * http://www.collab.net
 */
package com.googlesource.gerrit.plugins.lfs.events;

import static com.google.inject.Scopes.SINGLETON;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.registration.DynamicSet;

public class EventsModule extends FactoryModule {
  @Override
  protected void configure() {
    factory(RefUpdateToOperationConverter.Factory.class);
    factory(LfsDataProvider.Factory.class);

    DynamicSet.bind(binder(), EventListener.class)
      .to(LfsEventListener.class).in(SINGLETON);
  }
}
