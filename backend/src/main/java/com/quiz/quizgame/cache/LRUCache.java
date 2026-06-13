package com.quiz.quizgame.cache;

import com.quiz.quizgame.model.Quiz;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component // Spring will manage this as a bean
public class LRUCache {
    private final int capacity;

    // TTL or Time To Live
    //tell how long a cache entry stays valid before expiring
    // 5 * 60 * 1000 = 300000 milliseconds = 5 minutes
    //after 5mins cache entry expires automatically & GET fetches new data from DDB
    private final long TTL_MS = 5 * 60 * 1000;
    //same as cache_dict in python with {key:Node(key,val)} mapping
    private final Map<String,Node> cacheMap;
    //dummy nodes: left:LRU Node, right:MRU Node
    private final Node left;
    private final Node right;

    public LRUCache() {
        this.capacity = 10;
        this.cacheMap = new HashMap<>();

        //Left and right dummy node
        this.left = new Node(null,null,0);
        this.right = new Node(null,null,0);

        // connect left and right dummy nodes
        this.left.next = this.right;
        this.right.prev = this.left;

    }
    private void remove(Node node){
        Node prev = node.prev;
        Node nxt = node.next;
        prev.next = nxt;
        nxt.prev = prev;
    }

    private void insert(Node node){
        Node prev = this.right.prev;
        Node nxt = this.right;
        prev.next = node;
        nxt.prev = node;
        node.prev = prev;
        node.next = nxt;
    }
    // check if a cache entry has expired based on TTL
    // returns true if current time is past expiry time
    private boolean isExpired(Node node) {
        // System.currentTimeMillis() = current time in milliseconds
        // if current time > expiry time → entry has expired
        return System.currentTimeMillis() > node.expiryTime;
    }

    public Quiz get(String key){
        //if dict has key then return its value & move it to MRU place
        //else return null
        if (cacheMap.containsKey(key)) {
            Node node = cacheMap.get(key);

            // check if this cache entry has expired (TTL check)
            if (isExpired(node)) {
                // entry expired, remove from linked list
                remove(node);
                // remove from hashmap as well
                cacheMap.remove(key);

                // return null → caller will fetch fresh data from DB
                return null;
            }
            // entry not expired → move to MRU position (right side)
            // because we just accessed it = recently used
            remove(node);  // remove from current position
            insert(node);  // insert at MRU position (right side)

            System.out.println("CACHE HIT → returning from cache: " + key);
            // return the quiz stored in this node
            return node.value;
        }
        return null;
    }
    //update the value if key exists in dict else create new (key,val) pair and put it in dict
    //and check eviction
    public void put(String key, Quiz value) {
        long expiryTime = System.currentTimeMillis() + TTL_MS;
        if (cacheMap.containsKey(key)) {
            Node node = cacheMap.get(key);
            // VERSION CHECK:
            // only update cache if new version >= existing cached version
            // this prevents stale data from overwriting fresh data
            if (value.getVersion() >= node.value.getVersion()) {
                // new data is same version or newer → update cache
                node.value = value;

                // reset TTL → entry gets fresh 5 minutes from now
                // because we just updated it with fresh data
                node.expiryTime = expiryTime;

                // move to MRU position → just updated = recently used
                remove(node);
                insert(node);

            } else {
                // new data has older version than what's in cache
                // skip → don't overwrite fresh cache with stale data
                System.out.println("CACHE SKIPPED stale put → key: " + key +
                        " cache has v" + node.value.getVersion() +
                        " tried to put v" + value.getVersion());
            }

        } else {
            Node newNode = new Node(key, value, expiryTime);
            cacheMap.put(key, newNode);
            insert(newNode);
            //check if capacity exceeded
            if (cacheMap.size() > capacity) {
                Node lru = this.left.next;
                remove(lru);
                cacheMap.remove(lru.key);
            }

        }
    }
    //removes specific key from cache
    //used when quiz is updated or deleted

    public void evict(String key){
        if (cacheMap.containsKey(key)){
            remove(cacheMap.get(key));//remove from LL
            cacheMap.remove(key); //remove from hashmap

            }
        }



}
