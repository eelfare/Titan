package com.yunji.titanrtx.common.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorService implements ZookeeperService {


    private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", 30 * 1000);
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 30 * 1000);

    private CuratorFramework zkClient;

    public CuratorService(String address) {
        zkClient = CuratorFrameworkFactory.newClient(address, DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, new RetryForever(10000));
        zkClient.start();
        log.info("connect zookeeper success address:{}.........................", address);
    }

    @Override
    public String createPersistentNode(String path) {
        return createPersistentNode(path, "");
    }

    @Override
    public String createPersistentNode(String path, String data) {
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                return createNode(path, data, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String register(String path, String agentMeta) {
        return createEphemeralSequentialNode(path, agentMeta);
    }

    @Override
    public List<String> childNodes(String path) {
        try {
            return zkClient.getChildren().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(String path, String data) {
        try {
            zkClient.setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getData(String path) {
        try {
            return new String(zkClient.getData().forPath(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DistributedBarrier setBarrier(String path) {
        DistributedBarrier barrier = new DistributedBarrier(zkClient, path);
        try {
            barrier.setBarrier();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return barrier;
    }

    @Override
    public void waitOnBarrier(String path) {
        DistributedBarrier barrier = new DistributedBarrier(zkClient, path);
        try {
            barrier.waitOnBarrier();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void waitOnBarrier(String path, long maxWait, TimeUnit unit) {
        DistributedBarrier barrier = new DistributedBarrier(zkClient, path);
        try {
            barrier.waitOnBarrier(maxWait, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InterProcessMutex acquireLock(String path) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(zkClient, path);
        lock.acquire();
        return lock;
    }

    @Override
    public InterProcessMutex acquireLock(String path, long time, TimeUnit unit) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(zkClient, path);
        lock.acquire(time, unit);
        return lock;
    }

    @Override
    public void deleteNode(String path) {
        try {
            zkClient.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNodeIfExist(String path) {
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat != null) {
                deleteNode(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pathChildrenCacheLister(PathChildrenCacheListener pathChildrenCacheListener, String path) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, path, true);
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
            log.info("pathChildrenCacheLister success :{}", pathChildrenCacheListener);
        } catch (Exception e) {
            log.warn("pathChildrenCacheLister failer:{}", e);
        }

    }

    @Override
    public void nodeCacheListener(NodeCacheListener nodeCacheListener, String path) {
        NodeCache nodeCache = new NodeCache(zkClient, path);
        nodeCache.getListenable().addListener(nodeCacheListener);
        try {
            nodeCache.start();
            log.info("监听当前agent信息节点成功  path:{}", path);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("监听当前agent信息节点失败  path:{}", path);
        }
    }


    private String createEphemeralSequentialNode(String path, String data) {
        return createNode(path, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    private String createNode(String path, String data, CreateMode mode) {
        try {
            return zkClient.create().creatingParentsIfNeeded().withMode(mode).forPath(path, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int nextId(String path) {
        try {
            return zkClient.setData().forPath(path).getVersion();
        } catch (KeeperException.NodeExistsException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

}
