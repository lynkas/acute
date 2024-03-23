// ICacheServer.aidl
package cat.moki.acute.services.aidl;
// Declare any non-default types here with import statements
import java.util.Map;
import cat.moki.acute.models.ServerCacheStatus;

interface ICacheServer {
    void cacheOperation(String operation, String serverId);
    Map<String, ServerCacheStatus> cacheStatus();
}