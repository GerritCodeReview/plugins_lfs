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

Gerrit.install(function(self) {
  function toUpperCamel(str) {
    var words = str.split('_');
    for (i=0; i<words.length; i++) {
      words[i] = words[i].charAt(0).toUpperCase() + words[i].slice(1);
    }
    return words.join(' ');
  }

  function createRow(doc, name, value) {
    var tr = doc.createElement('tr');
    var td = doc.createElement('td');
    td.appendChild(doc.createTextNode(toUpperCamel(name) + ':'));
    tr.appendChild(td);
    td = doc.createElement('td');
    td.appendChild(doc.createTextNode(value));
    tr.appendChild(td);
    return tr;
  }

  function onProjectInfo(panel) {
    var url = 'projects/'
      + encodeURI(panel.p.PROJECT_NAME).replace(/\//g, '%2F') //escape slashes
      + '/' + this.pluginName + '~'
      + 'lfs:config-project';

    Gerrit.get(url, function(lfs) {
      // Don't show LFS header if nothing is configured
      if (Object.getOwnPropertyNames(lfs).length < 1) {
        return;
      }

      var doc = document;
      // create header
      var td = doc.createElement('td');
      td.appendChild(Gerrit.html(
        '<div class=\"smallHeading\">LFS Options</div>'));
      var tr = doc.createElement('tr');
      tr.appendChild(td);
      var table = doc.createElement('table');
      table.appendChild(tr);

      // add all properties
      for (var name in lfs) {
        table.appendChild(createRow(doc, name, lfs[name]));
      }

      var frg = doc.createDocumentFragment();
      frg.appendChild(table);
      panel.body.appendChild(frg);
    });
  }

  self.panel('PROJECT_INFO_SCREEN_BOTTOM', onProjectInfo);
  });