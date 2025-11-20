package cn.sliew.flink.dw.job.inspection.util;

import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.flink.FlinkCatalogFactory;
import org.apache.paimon.options.Options;
import org.apache.paimon.schema.Schema;

public enum PaimonUtil {
    ;

    public static Options getOptions() {
        Options catalogOptions = new Options();
        catalogOptions.set("type", "paimon");
        catalogOptions.set("warehouse", "/Users/mac/Downloads/test");
        catalogOptions.set("metastore", "filesystem");
        return catalogOptions;
    }

    public static CatalogLoader getCatalogLoader() {
        return () -> FlinkCatalogFactory.createPaimonCatalog(getOptions());
    }

    public static Identifier getTableIdentifier() {
        return Identifier.create("my_db", "T");
    }

    public static Schema getTableSchema() {
        return Schema.newBuilder()
                .column("name", org.apache.paimon.types.DataTypes.STRING())
                .column("age", org.apache.paimon.types.DataTypes.INT())
                .primaryKey("name")
                .option("bucket", "4")
                .build();
    }
}
