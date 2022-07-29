package cn.sliew.hadoop.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public enum FileSystemFacotry {

    INSTANCE;

    private static final HashMap<FSKey, FileSystem> CACHE = new HashMap<>();

    private static URI defaultScheme;

    public static String schema() {
        return defaultScheme.getScheme();
    }

    public static void initialize(Configuration conf) {
        FileSystemFacotry.defaultScheme = FileSystem.getDefaultUri(conf);
    }

    public static FileSystem getLocalFileSystem(Configuration conf) throws IOException {
        return FileSystem.getLocal(conf);
    }

    public static FileSystem get(URI uri, Configuration conf) throws IOException {
        return FileSystem.get(uri, conf);
    }

    private static final class FSKey {

        /**
         * The scheme of the file system.
         */
        private final String scheme;

        /**
         * The authority of the file system.
         */
        @Nullable
        private final String authority;

        /**
         * Creates a file system key from a given scheme and an authority.
         *
         * @param scheme    The scheme of the file system
         * @param authority The authority of the file system
         */
        public FSKey(String scheme, @Nullable String authority) {
            this.scheme = checkNotNull(scheme, "scheme");
            this.authority = authority;
        }
    }


}
