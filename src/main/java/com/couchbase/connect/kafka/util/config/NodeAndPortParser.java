package com.couchbase.connect.kafka.util.config;

import com.couchbase.client.dcp.config.ClientEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NodeAndPortParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeAndPortParser.class);

  public static List<NodeAndPort> provideSource(List<String> hostNames, Boolean sslEnabled){

    final List<NodeAndPort> nodesAndPorts = new ArrayList<>();

    hostNames.forEach( node -> {
      NodeAndPort nodeAndPort = getNodeAndPort(node, sslEnabled);
      LOGGER.info(String.format("Connecting to Node: %s, Port: %s", nodeAndPort.hostName, nodeAndPort.port));
      nodesAndPorts.add(nodeAndPort);
    });

    return nodesAndPorts;
  }


  public static SinkPortsConfig provideSink(List<String> hostNames, Boolean sslEnabled) {

    SinkPortsConfig sinkPortsConfig;
    boolean portModeEnabled = false;

    for(String node: hostNames) {
      if (node.contains(":")){
        portModeEnabled = true;
        break;
      }
    }

    String firstHost = hostNames.get(0);
    NodeAndPort nodeAndPort = getNodeAndPort(firstHost, sslEnabled);

    if (portModeEnabled) {
      sinkPortsConfig = new SinkPortsConfig(true, nodeAndPort.hostName, nodeAndPort.port);
    } else {
      sinkPortsConfig = new SinkPortsConfig(false, nodeAndPort.hostName, sslEnabled ? ClientEnvironment.BOOTSTRAP_HTTP_SSL_PORT : ClientEnvironment.BOOTSTRAP_HTTP_DIRECT_PORT);
    }

    return sinkPortsConfig;
  }

  private static NodeAndPort getNodeAndPort(String hostName, Boolean sslEnabled) {

    NodeAndPort nodeAndPort = null;

    if(hostName.indexOf(":") > 0) {
      nodeAndPort = new NodeAndPort(hostName.substring(0, hostName.indexOf(":")), Integer.parseInt(hostName.substring(hostName.indexOf(":") + 1)));
    } else {
      nodeAndPort = new NodeAndPort(hostName, sslEnabled ? ClientEnvironment.BOOTSTRAP_HTTP_SSL_PORT : ClientEnvironment.BOOTSTRAP_HTTP_DIRECT_PORT);
    }

    return nodeAndPort;
  }

  public static class SinkPortsConfig {
    public final Boolean httpBootstrapEnabled;
    public final Integer httpBootstrapPort;
    public final String hostName;

    public SinkPortsConfig(Boolean httpBootstrapEnabled, String hostName, Integer httpBootstrapPort) {
      this.httpBootstrapEnabled = httpBootstrapEnabled;
      this.httpBootstrapPort = httpBootstrapPort;
      this.hostName = hostName;
    }
  }

  public static class NodeAndPort {
    public final String hostName;
    public final Integer port;

    public NodeAndPort(String hostName, Integer port) {
      this.hostName = hostName;
      this.port = port;
    }
  }
}
