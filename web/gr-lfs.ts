/**
 * @license
 * Copyright (C) 2025 The Android Open Source Project
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

import {PluginApi} from '@gerritcodereview/typescript-api/plugin';
import {css, CSSResult, html, LitElement, nothing} from 'lit';
import {customElement, property, state} from 'lit/decorators.js';
import '@gerritcodereview/typescript-api/gerrit';

declare global {
  interface HTMLElementTagNameMap {
    'gr-lfs': GrLfs;
  }
}

@customElement('gr-lfs')
export class GrLfs extends LitElement {
  @property({type: Object})
  plugin!: PluginApi;

  @property({type: String}) repoName = '';

  @state() private _appliedConfig?: {
    enabled?: boolean;
    max_object_size?: string;
    read_only?: boolean;
    backend?: string;
  };

  override connectedCallback() {
    super.connectedCallback();
    this._getPreferences();
  }

  static override get styles() {
    return [
      window.Gerrit?.styles.form as CSSResult,
      css`
        :host {
          display: block;
        }

        .sectionTitle {
          padding-top: 2em;
        }

        /* Placeholder: Replace these with actual shared styles or imports */
        .gr-form-styles {
          border: none;
          padding: 0;
        }

        section {
          display: flex;
          margin-bottom: 8px;
        }

        .title {
          font-weight: bold;
          width: 150px;
        }

        .value {
          flex: 1;
        }
      `,
    ];
  }

  override render() {
    const cfg = this._appliedConfig;
    if (!cfg) return nothing;

    return html`
      <fieldset class="gr-form-styles">
        <h2 class="sectionTitle">LFS Info</h2>
        <section>
          <span class="title">Enabled</span>
          <span class="value">${cfg.enabled ?? ''}</span>
        </section>
        <section>
          <span class="title">Max Object Size</span>
          <span class="value">${cfg.max_object_size ?? ''}</span>
        </section>
        <section>
          <span class="title">Read Only</span>
          <span class="value">${cfg.read_only ?? ''}</span>
        </section>
        ${cfg.backend
          ? html`
              <section>
                <span class="title">Backend</span>
                <span class="value">${cfg.backend}</span>
              </section>
            `
          : nothing}
      </fieldset>
    `;
  }

  private _getPreferences() {
    let encodedRepoName = encodeURIComponent(this.repoName);
    return this.plugin.restApi()
      .get(`/projects/${encodedRepoName}/${this.plugin.getPluginName()}~lfs:config-project`)
      .then(config => {
        if (!config || Object.entries(config).length === 0) {
          return;
        }

        this._appliedConfig = config
      })
  }
}
