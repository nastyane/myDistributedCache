package ru.nastya.memcache.discovery;

import java.io.IOException;

public interface StarTopologyNode {
    void start() throws IOException;
    void stop();

    ClusterNode getCurrentNode();


}
