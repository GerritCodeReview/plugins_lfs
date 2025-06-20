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

import {css, CSSResult, html, LitElement, nothing} from 'lit';
import {customElement, property, state} from 'lit/decorators.js';

declare global {
  interface HTMLElementTagNameMap {
    'gr-lfs': GrLfs;
  }
}

@customElement('gr-lfs')
export class GrLfs extends LitElement {

  @property({type: String}) repoName = '';

  @state() private _appliedConfig?: {
    enabled?: boolean;
    max_object_size?: string;
    read_only?: boolean;
    backend?: string;
  };

  connectedCallback() {
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
    if (!this._appliedConfig) return nothing;

    const cfg = this._appliedConfig;

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

  private async _getPreferences() {
    const encodedRepoName = encodeURIComponent(this.repoName);
    const config = await this.plugin.restApi('/projects/')
      .get(`${encodedRepoName}/${this.plugin.getPluginName()}~lfs:config-project`);

    if (!config || Object.entries(config).length === 0) {
      this._appliedConfig = undefined;
      return;
    }

    this._appliedConfig = config;
  }
}
