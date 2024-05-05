package com.yunji.titanrxt.plugin.influxdb;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class InfluxService implements StoreService {

    private static final Pattern databaseRegex = Pattern.compile("database not found: \"([a-zA-Z0-9:.]*)\"");

    @Value("${influxDB.rpName}")
    private String rpName;

    public static final String DEFAULT_SHARD_DURATION = "1d";

    private InfluxDB influxDB;

    private String shardDuration;

    public InfluxService(String url) {
        this(url, null, null);
    }

    public InfluxService(String url, String userName, String password) {
        this(url, userName, password, DEFAULT_SHARD_DURATION);
    }

    public InfluxService(String url, String userName, String password, String shardDuration) {
        OkHttpClient.Builder client = new OkHttpClient
                .Builder()
                .readTimeout(20, TimeUnit.SECONDS);

        if (StringUtils.isBlank(userName)) {
            this.influxDB = InfluxDBFactory.connect(url, client);
        } else {
            this.influxDB = InfluxDBFactory.connect(url, userName, password, client);
        }
        this.shardDuration = shardDuration;
    }


    @Override
    public InfluxDB setLogLevel(InfluxDB.LogLevel logLevel) {
        return influxDB.setLogLevel(logLevel);
    }

    @Override
    public InfluxDB enableGzip() {
        return influxDB.enableGzip();
    }

    @Override
    public InfluxDB disableGzip() {
        return influxDB.disableGzip();
    }

    @Override
    public boolean isGzipEnabled() {
        return influxDB.isGzipEnabled();
    }

    @Override
    public InfluxDB enableBatch() {
        return influxDB.enableBatch();
    }

    @Override
    public InfluxDB enableBatch(BatchOptions batchOptions) {
        return influxDB.enableBatch(batchOptions);
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit) {
        //enableBatch(actions, flushDuration, flushDurationTimeUnit, Executors.defaultThreadFactory());

        return influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit,
                Executors.defaultThreadFactory(),
                (points, throwable) -> {
                    try {
                        if (throwable instanceof InfluxDBException.DatabaseNotFoundException) {
                            String message = throwable.getMessage();
                            if (StringUtils.isNotEmpty(message)) {
                                Matcher matcher = databaseRegex.matcher(message);
                                if (matcher.matches()) {
                                    String database = matcher.group(1);
                                    log.info("ExceptionHandler retry create database {}.", database);
                                    influxDB.createDatabase(database);
                                    createRetentionPolicy(rpName, database, shardDuration, 1, true);
                                } else {
                                    log.info("Not match  database  message {}.", message);
                                }


                            }
                        }
                    } catch (Exception e) {
                        log.error("ExceptionHandler handle exception error,cause: {}", e.getMessage());
                    }
                }
        );
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory) {
        return influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit, threadFactory);
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler, InfluxDB.ConsistencyLevel consistency) {
        return influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit, threadFactory, exceptionHandler, consistency);
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler) {
        return influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit, threadFactory, exceptionHandler);
    }

    @Override
    public void disableBatch() {
        influxDB.disableBatch();
    }

    @Override
    public boolean isBatchEnabled() {
        return influxDB.isBatchEnabled();
    }

    @Override
    public Pong ping() {
        return influxDB.ping();
    }

    @Override
    public String version() {
        return influxDB.version();
    }

    @Override
    public void write(Point point) {
        influxDB.write(point);
    }

    @Override
    public void write(String records) {
        influxDB.write(records);
    }

    @Override
    public void write(List<String> records) {
        influxDB.write(records);
    }

    @Override
    public void write(String database, String retentionPolicy, Point point) {
        try {
            influxDB.write(database, retentionPolicy, point);
        } catch (InfluxDBException.DatabaseNotFoundException e) {
            doExceptionHandler(retentionPolicy, database, shardDuration, 1, true);
        }
    }

    @Override
    public void write(int udpPort, Point point) {
        influxDB.write(udpPort, point);
    }

    @Override
    public void batchWrite(BatchPoints batchPoints) {
        try {
            influxDB.write(batchPoints);
        } catch (InfluxDBException.DatabaseNotFoundException e) {
            doExceptionHandler(batchPoints.getRetentionPolicy(), batchPoints.getDatabase(), shardDuration, 1, true);
        }
    }

    @Override
    public void writeWithRetry(BatchPoints batchPoints) {
        influxDB.writeWithRetry(batchPoints);
    }

    @Override
    public void write(String database, String retentionPolicy, InfluxDB.ConsistencyLevel consistency, String records) {
        influxDB.write(database, retentionPolicy, consistency, records);
    }

    @Override
    public void write(String database, String retentionPolicy, InfluxDB.ConsistencyLevel consistency, TimeUnit precision, String records) {
        influxDB.write(database, retentionPolicy, consistency, precision, records);
    }

    @Override
    public void write(String database, String retentionPolicy, InfluxDB.ConsistencyLevel consistency, List<String> records) {
        influxDB.write(database, retentionPolicy, consistency, records);
    }

    @Override
    public void write(String database, String retentionPolicy, InfluxDB.ConsistencyLevel consistency, TimeUnit precision, List<String> records) {
        influxDB.write(database, retentionPolicy, consistency, precision, records);
    }

    @Override
    public void write(int udpPort, String records) {
        influxDB.write(udpPort, records);
    }

    @Override
    public void write(int udpPort, List<String> records) {
        influxDB.write(udpPort, records);
    }

    @Override
    public QueryResult query(Query query) {
        return influxDB.query(query);
    }

    @Override
    public void query(Query query, Consumer<QueryResult> onSuccess, Consumer<Throwable> onFailure) {
        influxDB.query(query, onSuccess, onFailure);
    }

    @Override
    public void query(Query query, int chunkSize, Consumer<QueryResult> onNext) {
        influxDB.query(query, chunkSize, onNext);

    }

    @Override
    public void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext) {
        influxDB.query(query, chunkSize, onNext);
    }

    @Override
    public void query(Query query, int chunkSize, Consumer<QueryResult> onNext, Runnable onComplete) {
        influxDB.query(query, chunkSize, onNext, onComplete);
    }

    @Override
    public void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext, Runnable onComplete) {
        influxDB.query(query, chunkSize, onNext, onComplete);
    }

    @Override
    public void query(Query query, int chunkSize, BiConsumer<InfluxDB.Cancellable, QueryResult> onNext, Runnable onComplete, Consumer<Throwable> onFailure) {
        influxDB.query(query, chunkSize, onNext, onComplete, onFailure);
    }

    @Override
    public QueryResult query(Query query, TimeUnit timeUnit) {
        return influxDB.query(query, timeUnit);
    }

    @Override
    public void createDatabase(String name) {
        influxDB.createDatabase(name);
    }

    @Override
    public void deleteDatabase(String name) {
        influxDB.deleteDatabase(name);
    }

    @Override
    public List<String> describeDatabases() {
        return influxDB.describeDatabases();
    }

    @Override
    public boolean databaseExists(String name) {
        return influxDB.databaseExists(name);
    }

    @Override
    public void flush() {
        influxDB.flush();
    }

    @Override
    public void close() {
        influxDB.close();
    }

    @Override
    public InfluxDB setConsistency(InfluxDB.ConsistencyLevel consistency) {
        return influxDB.setConsistency(consistency);
    }

    @Override
    public InfluxDB setDatabase(String database) {
        return influxDB.setDatabase(database);
    }

    @Override
    public InfluxDB setRetentionPolicy(String retentionPolicy) {
        return influxDB.setRetentionPolicy(retentionPolicy);
    }

    @Override
    public void createRetentionPolicy(String rpName, String database, String duration, String shardDuration, int replicationFactor, boolean isDefault) {
        influxDB.createRetentionPolicy(rpName, database, duration, replicationFactor, isDefault);
    }

    @Override
    public void createRetentionPolicy(String rpName, String database, String duration, int replicationFactor, boolean isDefault) {
        influxDB.createRetentionPolicy(rpName, database, duration, replicationFactor, isDefault);
    }

    @Override
    public void createRetentionPolicy(String rpName, String database, String duration, String shardDuration, int replicationFactor) {
        influxDB.createRetentionPolicy(rpName, database, duration, shardDuration, replicationFactor);
    }

    @Override
    public void dropRetentionPolicy(String rpName, String database) {
        influxDB.dropRetentionPolicy(rpName, database);
    }

    private void doExceptionHandler(String retentionPolicy, String database, String shardDuration, int replicationFactor, boolean isDefault) {
        influxDB.createDatabase(database);
        createRetentionPolicy(retentionPolicy, database, shardDuration, replicationFactor, isDefault);
    }
}
