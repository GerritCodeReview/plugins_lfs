// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

(function () {
  'use strict';

  Polymer({
    is: 'gr-lfs-project-info',

    properties: {
      repoName: String,
      _appliedConfig: Object,
    },

    attached() {
      this._getPreferences()
    },

    _getPreferences() {
      return this.plugin.restApi('/projects/')
        .get(`${this.repoName}/${this.plugin.getPluginName()}~lfs:config-project`)
        .then(config => {
          if (!config || Object.entries(config).length === 0) {
            this.$.lfsProjectInfo.hidden = true;
            return;
          }

          this._appliedConfig = config
        })
    },
  });
})();
