package com.yunji.titanrxt.plugin.influxdb;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.*;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StoreService{

    InfluxDB setLogLevel(final InfluxDB.LogLevel logLevel);

    InfluxDB enableGzip();

    InfluxDB disableGzip();

    boolean isGzipEnabled();

    InfluxDB enableBatch();

    InfluxDB enableBatch(final BatchOptions batchOptions);

    InfluxDB enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit);

    InfluxDB enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit,
                         final ThreadFactory threadFactory);

    InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit,
                         ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler,
                         InfluxDB.ConsistencyLevel consistency);

    InfluxDB enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit,
                         final ThreadFactory threadFactory,
                         final BiConsumer<Iterable<Point>, Throwable> exceptionHandler);

    void disableBatch();

    boolean isBatchEnabled();

    Pong ping();

    String version();

    void write(final Point point);

    void write(final String records);

    void write(final List<String> records);

    void write(final String database, final String retentionPolicy, final Point point);

    void write(final int udpPort, final Point point);

    void batchWrite(final BatchPoints batchPoints);

    void writeWithRetry(final BatchPoints batchPoints);

    void write(final String database, final String retentionPolicy,
               final InfluxDB.ConsistencyLevel consistency, final String records);

    void write(final String database, final String retentionPolicy,
               final InfluxDB.ConsistencyLevel consistency, final TimeUnit precision, final String records);

    void write(final String database, final String retentionPolicy,
               final InfluxDB.ConsistencyLevel consistency, final List<String> records);

    void write(final String database, final String retentionPolicy,
               final InfluxDB.ConsistencyLevel consistency, final TimeUnit precision, final List<String> records);

    void write(final int udpPort, final String records);

    void write(final int udpPort, final List<String> records);

    QueryResult query(final Query query);

    void query(final Query query, final Consumer<QueryResult> onSuccess, final Consumer<Throwable> onFailure);

    void query(Query query, int chunkSize, Consumer<QueryResult> onNext);

    void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext);

    void query(Query query, int chunkSize, Consumer<QueryResult> onNext, Runnable onComplete);

    void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext, Runnable onComplete);

    void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext, Runnable onComplete,
               Consumer<Throwable> onFailure);

    QueryResult query(final Query query, TimeUnit timeUnit);

    void createDatabase(final String name);

    void deleteDatabase(final String name);

    List<String> describeDatabases();

    boolean databaseExists(final String name);

    void flush();

    void close();

    InfluxDB setConsistency(final InfluxDB.ConsistencyLevel consistency);

    InfluxDB setDatabase(final String database);

    InfluxDB setRetentionPolicy(final String retentionPolicy);

    void createRetentionPolicy(final String rpName, final String database, final String duration,
                               final String shardDuration, final int replicationFactor, final boolean isDefault);

    void createRetentionPolicy(final String rpName, final String database, final String duration,
                               final int replicationFactor, final boolean isDefault);

    void createRetentionPolicy(final String rpName, final String database, final String duration,
                               final String shardDuration, final int replicationFactor);

    void dropRetentionPolicy(final String rpName, final String database);


}
