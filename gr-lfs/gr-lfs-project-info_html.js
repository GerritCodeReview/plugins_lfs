/**
 * @license
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export const htmlTemplate = Polymer.html`
<style include="shared-styles"></style>
<style include="gr-form-styles"></style>
<style>
  .sectionTitle {
    padding-top: 2em;
  }
</style>
<fieldset id="lfsProjectInfo" class="gr-form-styles">
  <h2 class="sectionTitle">LFS Info</h2>
  <section>
    <span class="title">Enabled</span>
    <span class="value">[[_appliedConfig.enabled]]</span>
  </section>
  <section>
    <span class="title">Max Object Size</span>
    <span class="value">[[_appliedConfig.max_object_size]]</span>
  </section>
  <section>
    <span class="title">Read Only</span>
    <span class="value">[[_appliedConfig.read_only]]</span>
  </section>
  <section hidden="[[!_appliedConfig.backend]]">
    <span class="title">Backend</span>
    <span class="value">[[_appliedConfig.backend]]</span>
  </section>
</fieldset>`;
