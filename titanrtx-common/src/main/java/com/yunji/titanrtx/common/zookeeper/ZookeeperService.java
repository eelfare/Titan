package com.yunji.titanrtx.common.zookeeper;

import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ZookeeperService {

    String createPersistentNode(String path);

    String createPersistentNode(String path, String data);

    String register(String path, String agentMeta);

    List<String> childNodes(String path);

    void update(String path, String data);

    String getData(String path);

    DistributedBarrier setBarrier(String path);

    void waitOnBarrier(String path);

    void waitOnBarrier(String path, long maxWait, TimeUnit unit);

    InterProcessMutex acquireLock(String path) throws Exception;

    InterProcessMutex acquireLock(String path, long time, TimeUnit unit) throws Exception;

    void deleteNode(String path);

    void deleteNodeIfExist(String path);

    void pathChildrenCacheLister(PathChildrenCacheListener pathChildrenCacheListener, String path);

    void nodeCacheListener(NodeCacheListener nodeCacheListener, String path);

    int nextId(String path);

}
