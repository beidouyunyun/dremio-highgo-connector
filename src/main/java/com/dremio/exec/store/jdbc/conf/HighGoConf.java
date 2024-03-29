/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.store.jdbc.conf;

import static com.google.common.base.Preconditions.checkNotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.server.SabotContext;
import com.dremio.exec.store.jdbc.CloseableDataSource;
import com.dremio.exec.store.jdbc.DataSources;
import com.dremio.exec.store.jdbc.JdbcStoragePlugin;
import com.dremio.exec.store.jdbc.JdbcStoragePlugin.Config;
import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.google.common.annotations.VisibleForTesting;

import io.protostuff.Tag;

/**
 * Configuration for DaMeng sources.
 */
@SourceType(value = "HIGHGO", label = "HighGo")
public class HighGoConf extends AbstractArpConf<HighGoConf> {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HighGoConf.class);

    private static final String ARP_FILENAME = "arp/implementation/highgo-arp.yaml";

    private static final ArpDialect ARP_DIALECT = AbstractArpConf.loadArpFile(ARP_FILENAME, (ArpDialect::new));
    private static final String DRIVER = "com.highgo.jdbc.Driver";

    @NotBlank
    @Tag(1)
    @DisplayMetadata(label = "Host")
    public String host;

    @NotBlank
    @Tag(2)
    @DisplayMetadata(label = "Port")
    public String port;

    @NotBlank
    @Tag(3)
    @DisplayMetadata(label = "Username")
    public String username;

    @NotBlank
    @Tag(4)
    @Secret
    @DisplayMetadata(label = "Password")
    public String password;

    @VisibleForTesting
    public String toJdbcConnectionString() {
        final String host = checkNotNull(this.host, "Missing host.");
        final String port = checkNotNull(this.port, "Missing port.");
        final String username = checkNotNull(this.username, "Missing username.");
        final String password = checkNotNull(this.password, "Missing password.");
//        final String database = checkNotNull(this.database, "Missing database.");

//        logger.info(String.format("JDBC URL ---- jdbc:oscar://%s:%s/%s", host, port, database));
//        return String.format("jdbc:oscar://%s:%s/%s", host, port, database);
        logger.info(String.format("JDBC URL ---- jdbc:highgo://%s:%s", host, port));
        return String.format("jdbc:highgo://%s:%s", host, port);
    }

    @Override
    @VisibleForTesting
    public Config toPluginConfig(SabotContext context) {
        return JdbcStoragePlugin.Config.newBuilder()
                .withDialect(getDialect())
                .withDatasourceFactory(this::newDataSource)
                .clearHiddenSchemas()
//                .addHiddenSchema("SYSTEM")
                .build();
    }

    private CloseableDataSource newDataSource() {
        return DataSources.newGenericConnectionPoolDataSource(DRIVER,
                toJdbcConnectionString(), username, password, null, DataSources.CommitMode.DRIVER_SPECIFIED_COMMIT_MODE);
    }

    @Override
    public ArpDialect getDialect() {
        return ARP_DIALECT;
    }

    @VisibleForTesting
    public static ArpDialect getDialectSingleton() {
        return ARP_DIALECT;
    }
}
