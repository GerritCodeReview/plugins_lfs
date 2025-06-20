/**
 * @license
 * Copyright 2025 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

const {defineConfig} = require('eslint/config');

// eslint-disable-next-line no-undef
__plugindir = 'lfs/web';

const gerritEslint = require('../../eslint.config.js');

module.exports = defineConfig([
  {
    extends: [gerritEslint],
  },
]);
