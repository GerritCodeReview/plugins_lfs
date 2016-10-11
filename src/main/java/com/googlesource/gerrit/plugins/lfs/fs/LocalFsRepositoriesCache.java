// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.lfs.fs;

import static com.googlesource.gerrit.plugins.lfs.LfsBackend.FS;
import static  com.googlesource.gerrit.plugins.lfs.LfsBackendConfig.DEFAULT;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsBackendConfig;

@Singleton
public class LocalFsRepositoriesCache {
  private final LoadingCache<String, LocalLargeFileRepository> repositories;

  @Inject
  LocalFsRepositoriesCache(LocalFsRepositoriesCache.Loader loader) {
    this.repositories = CacheBuilder.newBuilder().build(loader);
  }

  public LocalLargeFileRepository getRepository(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return repositories.getUnchecked(DEFAULT);
    }

    return repositories.getUnchecked(name);
  }

  static class Loader extends CacheLoader<String, LocalLargeFileRepository> {
    private final LocalLargeFileRepository.Factory fsRepoFactory;

    @Inject
    Loader(LocalLargeFileRepository.Factory fsRepoFactory) {
      this.fsRepoFactory = fsRepoFactory;
    }

    @Override
    public LocalLargeFileRepository load(String name) throws Exception {
      LfsBackendConfig config =
          new LfsBackendConfig(DEFAULT.equals(name) ? null : name, FS);
      return fsRepoFactory.create(config);
    }
  }
}