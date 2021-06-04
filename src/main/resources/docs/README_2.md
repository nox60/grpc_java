# 创建网络

docker network rm bc-net

docker network create --subnet=172.18.0.0/16 bc-net

# docker 启动 root ca server
```shell
docker rm -f ca.com
docker run  \
  -it -d --restart=always \
  --name ca.com \
      --network bc-net \
      -e FABRIC_CA_HOME="/opt/ca-home" \
      -e FABRIC_CA_SERVER_CA_NAME="ca.com" \
      -e FABRIC_CA_SERVER_CSR_CN=ca.com \
      -e FABRIC_CA_SERVER_CSR_HOSTS=ca.com \
      -e FABRIC_CA_SERVER_PORT=7054 \
      -v /root/temp/test-ca-home:/opt/ca-home \
      --entrypoint="fabric-ca-server" hyperledger/fabric-ca:1.4.3  start  -b admin:adminpw -d
```

# 获得 ca 的 admin的msp拉出来
```shell
docker run --rm -it \
    --name enroll.test.ca.client \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client enroll \
    -u http://admin:adminpw@ca.com:7054

```

```shell script
mv /root/temp/test-ca-admin-home/msp/cacerts/* /root/temp/test-ca-admin-home/msp/cacerts/ca.pem
mkdir -p /root/temp/test-ca-admin-home/msp/admincerts
```

```
cat>/root/temp/test-ca-admin-home/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```

列出affiliation
```shell
#fabric-ca-client affiliation add org3.department1
docker run --rm -it \
    --name add-affiliation \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client affiliation list
```

先注册 ordererOrg的 affiliation
```shell
#fabric-ca-client affiliation add org3.department1
docker run --rm -it \
    --name add-affiliation \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client affiliation add ordererOrg
```

增加org3组织
```shell
docker run --rm -it \
    --name add-affiliation \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client  affiliation add org3

docker run --rm -it \
    --name add-affiliation \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client  affiliation add org3.department1

docker run --rm -it \
    --name add-affiliation \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client affiliation add ordererOrg.ordererMSP
```

# 0. 注册orderer
```shell script
rm -rf /root/temp/order-home
docker run --rm -it \
    --name register.orderer \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name orderer.com --id.type orderer \
    --id.affiliation ordererOrg \
    --id.secret ordererpw 

```

```shell script
docker run --rm -it \
  --name enroll.orderer \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/orderer-home \
      -v /root/temp/orderer-home/tls:/opt/orderer-home \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      --enrollment.profile tls --csr.hosts orderer.com \
      -u http://orderer.com:ordererpw@ca.com:7054

```

```shell script
mv /root/temp/orderer-home/tls/msp/keystore/* /root/temp/orderer-home/tls/msp/keystore/server.key
mv /root/temp/orderer-home/tls/msp/signcerts/* /root/temp/orderer-home/tls/msp/signcerts/server.crt
mv /root/temp/orderer-home/tls/msp/tlscacerts/* /root/temp/orderer-home/tls/msp/tlscacerts/ca.crt

```

```shell script

docker run --rm -it \
  --name enroll.cec.orderer \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/orderer-home-msp \
      -v /root/temp/orderer-home/msp:/opt/orderer-home-msp \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      -M /opt/orderer-home-msp/msp \
      -u http://orderer.com:ordererpw@ca.com:7054

```

```shell script

mv /root/temp/orderer-home/msp/msp/cacerts/* /root/temp/orderer-home/msp/msp/cacerts/ca.pem
mkdir -p /root/temp/orderer-home/msp/msp/tlscacerts
cp /root/temp/orderer-home/msp/msp/cacerts/ca.pem  /root/temp/orderer-home/msp/msp/tlscacerts/

```

```shell script

cat>/root/temp/orderer-home/msp/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF

```

```shell script

mkdir -p /root/temp/orderer-home/msp/msp/admincerts
```

注册org1.peer0
```shell
rm -rf /root/temp/org1/peer0-home
docker run --rm -it \
    --name register.org1.peer0 \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org1.peer0 --id.type peer  --id.secret peerpw 

```

```shell script

docker run --rm -it \
  --name enroll.cec.org1.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home \
      -v /root/temp/org1/peer0-home/tls:/opt/peer0-home \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      --enrollment.profile tls --csr.hosts org1.peer0.com \
      -u http://org1.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org1/peer0-home/tls/msp/keystore/* /root/temp/org1/peer0-home/tls/msp/keystore/server.key
mv /root/temp/org1/peer0-home/tls/msp/signcerts/* /root/temp/org1/peer0-home/tls/msp/signcerts/server.crt
mv /root/temp/org1/peer0-home/tls/msp/tlscacerts/* /root/temp/org1/peer0-home/tls/msp/tlscacerts/ca.crt

```

```shell script

docker run --rm -it \
  --name enroll.cec.org1.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home-msp \
      -v /root/temp/org1/peer0-home/msp:/opt/peer0-home-msp \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      -M /opt/peer0-home-msp/msp \
      -u http://org1.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org1/peer0-home/msp/msp/cacerts/* /root/temp/org1/peer0-home/msp/msp/cacerts/ca.pem
mkdir -p /root/temp/org1/peer0-home/msp/msp/tlscacerts
cp /root/temp/org1/peer0-home/msp/msp/cacerts/ca.pem  /root/temp/org1/peer0-home/msp/msp/tlscacerts/

```

```shell script

cat>/root/temp/org1/peer0-home/msp/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF

```

```shell script

rm -rf /root/temp/org2/peer0-home
docker run --rm -it \
    --name register.org2.peer0 \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org2.peer0 --id.type peer  --id.secret peerpw 

```

```shell script

docker run --rm -it \
  --name enroll.cec.org2.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home \
      -v /root/temp/org2/peer0-home/tls:/opt/peer0-home \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      --enrollment.profile tls --csr.hosts org2.peer0.com \
      -u http://org2.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org2/peer0-home/tls/msp/keystore/* /root/temp/org2/peer0-home/tls/msp/keystore/server.key
mv /root/temp/org2/peer0-home/tls/msp/signcerts/* /root/temp/org2/peer0-home/tls/msp/signcerts/server.crt
mv /root/temp/org2/peer0-home/tls/msp/tlscacerts/* /root/temp/org2/peer0-home/tls/msp/tlscacerts/ca.crt

```

```shell script

docker run --rm -it \
  --name enroll.cec.org2.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home-msp \
      -v /root/temp/org2/peer0-home/msp:/opt/peer0-home-msp \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      -M /opt/peer0-home-msp/msp \
      -u http://org2.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org2/peer0-home/msp/msp/cacerts/* /root/temp/org2/peer0-home/msp/msp/cacerts/ca.pem
mkdir -p /root/temp/org2/peer0-home/msp/msp/tlscacerts
cp /root/temp/org2/peer0-home/msp/msp/cacerts/ca.pem  /root/temp/org2/peer0-home/msp/msp/tlscacerts/

```

```shell script

cat>/root/temp/org2/peer0-home/msp/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF

```

```shell script

rm -rf /root/temp/org3/peer0-home
docker run --rm -it \
    --name register.org3.peer0 \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org3.peer0 --id.type peer  --id.secret peerpw 

```

```shell script

docker run --rm -it \
  --name enroll.cec.org3.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home \
      -v /root/temp/org3/peer0-home/tls:/opt/peer0-home \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      --enrollment.profile tls --csr.hosts org3.peer0.com \
      -u http://org3.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org3/peer0-home/tls/msp/keystore/* /root/temp/org3/peer0-home/tls/msp/keystore/server.key
mv /root/temp/org3/peer0-home/tls/msp/signcerts/* /root/temp/org3/peer0-home/tls/msp/signcerts/server.crt
mv /root/temp/org3/peer0-home/tls/msp/tlscacerts/* /root/temp/org3/peer0-home/tls/msp/tlscacerts/ca.crt

```

```shell script

docker run --rm -it \
  --name enroll.cec.org3.peer0 \
      --network bc-net \
      -e FABRIC_CA_CLIENT_HOME=/opt/peer0-home-msp \
      -v /root/temp/org3/peer0-home/msp:/opt/peer0-home-msp \
      hyperledger/fabric-ca:1.4.3 \
      fabric-ca-client enroll \
      -M /opt/peer0-home-msp/msp \
      -u http://org3.peer0:peerpw@ca.com:7054

```

```shell script

mv /root/temp/org3/peer0-home/msp/msp/cacerts/* /root/temp/org3/peer0-home/msp/msp/cacerts/ca.pem
mkdir -p /root/temp/org3/peer0-home/msp/msp/tlscacerts
cp /root/temp/org3/peer0-home/msp/msp/cacerts/ca.pem  /root/temp/org3/peer0-home/msp/msp/tlscacerts/

```

```shell script

cat>/root/temp/org3/peer0-home/msp/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```


# 生成configtx.yaml文件 ！

-1. 各种msp生成完毕之后，生成创世区块
```go
docker run --rm -it \
  --name configtxgen.generate.files \
      --network bc-net \
      -e FABRIC_CFG_PATH=/etc/hyperledger/ \
      -v /root/temp/:/opt/data \
      -v /root/temp/configtx.yaml:/etc/hyperledger/configtx.yaml \
      -v /root/temp/org1/peer0-home/msp/msp:/opt/org1/peer0-home/msp \
      -v /root/temp/org2/peer0-home/msp/msp:/opt/org2/peer0-home/msp \
      -v /root/temp/org3/peer0-home/msp/msp:/opt/org3/peer0-home/msp \
      -v /root/temp/orderer-home/msp/msp:/opt/orderer-home/msp \
      -w /etc/hyperledger \
      hyperledger/fabric-tools:1.4.3 \
      configtxgen \
      -outputBlock /opt/data/orderer.genesis.block \
      -channelID byfn-sys-channel \
      -profile TwoOrgsOrdererGenesis
```

创建通道 channel.tx文件
```go
docker run --rm -it \
  --name configtxgen.generate.files.channel.tx.file \
      -e FABRIC_LOGGING_SPEC="DEBUG" \
      --network bc-net \
      -e FABRIC_CFG_PATH=/etc/hyperledger/ \
      -v /root/temp/:/opt/data \
      -v /root/temp/configtx.yaml:/etc/hyperledger/configtx.yaml \
      -v /root/temp/org1/peer0-home/msp/msp:/opt/org1/peer0-home/msp \
      -v /root/temp/org2/peer0-home/msp/msp:/opt/org2/peer0-home/msp \
      -v /root/temp/org3/peer0-home/msp/msp:/opt/org3/peer0-home/msp \
      -v /root/temp/orderer-home/msp/msp:/opt/orderer-home/msp \
      -w /etc/hyperledger \
      hyperledger/fabric-tools:1.4.3 \
      configtxgen \
      -profile TwoOrgsChannel \
      -outputCreateChannelTx /opt/data/channel.tx \
      -channelID mychannel
```

启动 orderer服务 
```go

docker rm -f orderer.com
docker run -it -d  --restart=always \
  --name orderer.com \
      --network bc-net \
      -e FABRIC_LOGGING_SPEC="INFO" \
      -e ORDERER_GENERAL_LISTENADDRESS="0.0.0.0" \
      -e ORDERER_GENERAL_GENESISMETHOD="file" \
      -e ORDERER_GENERAL_GENESISFILE="/etc/hyperledger/orderer_data/orderer.genesis.block" \
      -e ORDERER_GENERAL_LOCALMSPID="ordererMSP" \
      -e ORDERER_GENERAL_LOCALMSPDIR="/etc/hyperledger/fabric/msp" \
      -e ORDERER_GENERAL_TLS_ENABLED="true" \
      -e ORDERER_GENERAL_TLS_PRIVATEKEY="/etc/hyperledger/orderer/tls/keystore/server.key" \
      -e ORDERER_GENERAL_TLS_CERTIFICATE="/etc/hyperledger/orderer/tls/signcerts/server.crt" \
      -e ORDERER_GENERAL_TLS_ROOTCAS="[/etc/hyperledger/orderer/tls/tlscacerts/ca.crt]" \
      -e ORDERER_KAFKA_TOPIC_REPLICATIONFACTOR="1" \
      -e ORDERER_KAFKA_VERBOSE="true" \
      -e FABRIC_CFG_PATH="/etc/hyperledger/fabric" \
      -e ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE="/etc/hyperledger/orderer/tls/signcerts/server.crt" \
      -e ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY="/etc/hyperledger/orderer/tls/keystore/server.key" \
      -e ORDERER_GENERAL_CLUSTER_ROOTCAS="[/etc/hyperledger/orderer/tls/tlscacerts/ca.crt]" \
      -v /root/temp/orderer-home/tls/msp:/etc/hyperledger/orderer/tls \
      -v /root/temp/orderer-home/msp/msp:/etc/hyperledger/fabric/msp \
      -v /root/temp/orderer.genesis.block:/etc/hyperledger/orderer_data/orderer.genesis.block \
      -v /var/run:/var/run \
      -p 7050:7050 \
      hyperledger/fabric-orderer:1.4.3
```


启动org1.peer0的couchdb
```go
docker rm -f couchdb_org1_peer0
docker run -it -d --restart=always \
    --name couchdb_org1_peer0 \
    --network bc-net \
    -e COUCHDB_USER=admin \
    -e COUCHDB_PASSWORD=admin  \
    -v /root/temp/org1/peer0-home/couchdb:/opt/couchdb/data \
    -p 5984:5984 \
    -p 9100:9100 \
    -d hyperledger/fabric-couchdb

docker rm -f couchdb_org2_peer0
docker run -it -d --restart=always \
    --name couchdb_org2_peer0 \
    --network bc-net \
    -e COUCHDB_USER=admin \
    -e COUCHDB_PASSWORD=admin  \
    -v /root/temp/org2/peer0-home/couchdb:/opt/couchdb/data \
    -p 5985:5984 \
    -d hyperledger/fabric-couchdb

docker rm -f couchdb_org3_peer0
docker run -it -d --restart=always \
    --name couchdb_org3_peer0 \
    --network bc-net \
    -e COUCHDB_USER=admin \
    -e COUCHDB_PASSWORD=admin  \
    -v /root/temp/org3/peer0-home/couchdb:/opt/couchdb/data \
    -p 5986:5984 \
    -d hyperledger/fabric-couchdb
```

//http://192.168.88.128:5984/_utils/

启动org1.peer0, org2.peer0, org3.peer0
```go

docker rm -f org1.peer0.com
docker run -it -d --restart=always \
  --name org1.peer0.com \
      --network bc-net \
      -e FABRIC_LOGGING_SPEC="INFO" \
      -e CORE_PEER_TLS_ENABLED="true" \
      -e CORE_PEER_GOSSIP_USELEADERELECTION="false" \
      -e CORE_PEER_GOSSIP_ORGLEADER="true" \
      -e CORE_PEER_PROFILE_ENABLED="true" \
      -e CORE_PEER_TLS_CERT_FILE="/etc/hyperledger/fabric/tls/signcerts/server.crt" \
      -e CORE_PEER_TLS_KEY_FILE="/etc/hyperledger/fabric/tls/keystore/server.key" \
      -e CORE_PEER_TLS_ROOTCERT_FILE="/etc/hyperledger/fabric/tls/tlscacerts/ca.crt" \
      -e CORE_PEER_ID="org1.peer0.com" \
      -e CORE_PEER_ADDRESS="org1.peer0.com:7051" \
      -e CORE_PEER_LISTENADDRESS="0.0.0.0:7051" \
      -e CORE_PEER_CHAINCODEADDRESS="org1.peer0.com:7052" \
      -e CORE_PEER_CHAINCODELISTENADDRESS="0.0.0.0:7052" \
      -e CORE_PEER_GOSSIP_BOOTSTRAP="org1.peer0.com:7051" \
      -e CORE_PEER_GOSSIP_EXTERNALENDPOINT="org1.peer0.com:7051" \
      -e CORE_PEER_LOCALMSPID="org1MSP" \
      -e CORE_LEDGER_STATE_STATEDATABASE="CouchDB" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS="couchdb_org1_peer0:5984" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME="admin" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD="admin" \
      -e CORE_NOTEOUS_ENABLE="false" \
      -e CORE_VM_ENDPOINT="unix:///var/run/docker.sock" \
      -e CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE="bc-net" \
      -e FABRIC_CFG_PATH="/etc/hyperledger/fabric" \
      -v /root/temp/org1/peer0-home/msp/msp:/etc/hyperledger/fabric/msp \
      -v /root/temp/org1/peer0-home/tls/msp:/etc/hyperledger/fabric/tls \
      -v /root/temp/org1/peer0-home/production:/var/hyperledger/production \
      -v /var/run:/var/run \
	  -p 7051:7051 \
      -p 7052:7052 \
      hyperledger/fabric-peer:1.4.3


docker rm -f org2.peer0.com
docker run -it -d --restart=always \
  --name org2.peer0.com \
      --network bc-net \
      -e FABRIC_LOGGING_SPEC="INFO" \
      -e CORE_PEER_TLS_ENABLED="true" \
      -e CORE_PEER_GOSSIP_USELEADERELECTION="false" \
      -e CORE_PEER_GOSSIP_ORGLEADER="true" \
      -e CORE_PEER_PROFILE_ENABLED="true" \
      -e CORE_PEER_TLS_CERT_FILE="/etc/hyperledger/fabric/tls/signcerts/server.crt" \
      -e CORE_PEER_TLS_KEY_FILE="/etc/hyperledger/fabric/tls/keystore/server.key" \
      -e CORE_PEER_TLS_ROOTCERT_FILE="/etc/hyperledger/fabric/tls/tlscacerts/ca.crt" \
      -e CORE_PEER_ID="org2.peer0.com" \
      -e CORE_PEER_ADDRESS="org2.peer0.com:7051" \
      -e CORE_PEER_LISTENADDRESS="0.0.0.0:7051" \
      -e CORE_PEER_CHAINCODEADDRESS="org2.peer0.com:7052" \
      -e CORE_PEER_CHAINCODELISTENADDRESS="0.0.0.0:7052" \
      -e CORE_PEER_GOSSIP_BOOTSTRAP="org2.peer0.com:7051" \
      -e CORE_PEER_GOSSIP_EXTERNALENDPOINT="org2.peer0.com:7051" \
      -e CORE_PEER_LOCALMSPID="org2MSP" \
      -e CORE_LEDGER_STATE_STATEDATABASE="CouchDB" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS="couchdb_org2_peer0:5984" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME="admin" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD="admin" \
      -e CORE_NOTEOUS_ENABLE="false" \
      -e CORE_VM_ENDPOINT="unix:///var/run/docker.sock" \
      -e CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE="bc-net" \
      -e FABRIC_CFG_PATH="/etc/hyperledger/fabric" \
      -v /root/temp/org2/peer0-home/msp/msp:/etc/hyperledger/fabric/msp \
      -v /root/temp/org2/peer0-home/tls/msp:/etc/hyperledger/fabric/tls \
      -v /root/temp/org2/peer0-home/production:/var/hyperledger/production \
      -v /var/run:/var/run \
      hyperledger/fabric-peer:1.4.3


docker rm -f org3.peer0.com
docker run -it -d \
  --name org3.peer0.com --restart=always \
      --network bc-net \
      -e FABRIC_LOGGING_SPEC="INFO" \
      -e CORE_PEER_TLS_ENABLED="true" \
      -e CORE_PEER_GOSSIP_USELEADERELECTION="false" \
      -e CORE_PEER_GOSSIP_ORGLEADER="true" \
      -e CORE_PEER_PROFILE_ENABLED="true" \
      -e CORE_PEER_TLS_CERT_FILE="/etc/hyperledger/fabric/tls/signcerts/server.crt" \
      -e CORE_PEER_TLS_KEY_FILE="/etc/hyperledger/fabric/tls/keystore/server.key" \
      -e CORE_PEER_TLS_ROOTCERT_FILE="/etc/hyperledger/fabric/tls/tlscacerts/ca.crt" \
      -e CORE_PEER_ID="org3.peer0.com" \
      -e CORE_PEER_ADDRESS="org3.peer0.com:7051" \
      -e CORE_PEER_LISTENADDRESS="0.0.0.0:7051" \
      -e CORE_PEER_CHAINCODEADDRESS="org3.peer0.com:7052" \
      -e CORE_PEER_CHAINCODELISTENADDRESS="0.0.0.0:7052" \
      -e CORE_PEER_GOSSIP_BOOTSTRAP="org3.peer0.com:7051" \
      -e CORE_PEER_GOSSIP_EXTERNALENDPOINT="org3.peer0.com:7051" \
      -e CORE_PEER_LOCALMSPID="org3MSP" \
      -e CORE_LEDGER_STATE_STATEDATABASE="CouchDB" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS="couchdb_org3_peer0:5984" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME="admin" \
      -e CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD="admin" \
      -e CORE_NOTEOUS_ENABLE="false" \
      -e CORE_VM_ENDPOINT="unix:///var/run/docker.sock" \
      -e CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE="bc-net" \
      -e FABRIC_CFG_PATH="/etc/hyperledger/fabric" \
      -v /root/temp/org3/peer0-home/msp/msp:/etc/hyperledger/fabric/msp \
      -v /root/temp/org3/peer0-home/tls/msp:/etc/hyperledger/fabric/tls \
      -v /root/temp/org3/peer0-home/production:/var/hyperledger/production \
      -v /var/run:/var/run \
      hyperledger/fabric-peer:1.4.3
```


注册orderer机构管理员
```go
docker run --rm -it \
    --name register.orderer.order.admin \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name order.admin \
    --id.type admin \
    --id.affiliation ordererOrg \
    --id.attrs 'hf.Revoker=true,admin=true' --id.secret adminpw 
```

```shell script
docker run --rm -it \
    --name register.test.ca.client \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin2-home \
    -v /root/temp/orderer-admin-home:/opt/test-admin2-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client enroll \
    -u http://order.admin:adminpw@ca.com:7054
```

```shell script
mv /root/temp/orderer-admin-home/msp/cacerts/* /root/temp/orderer-admin-home/msp/cacerts/ca.pem
mkdir -p /root/temp/orderer-admin-home/msp/tlscacerts
cp /root/temp/orderer-admin-home/msp/cacerts/ca.pem  /root/temp/orderer-admin-home/msp/tlscacerts/
```

```shell script
cat>/root/temp/orderer-admin-home/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```


注册org1机构管理员
```go
docker run --rm -it \
    --name register.org1.admin \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org1.admin \
    --id.type admin \
    --id.affiliation org1 \
    --id.attrs 'hf.Revoker=true,admin=true' --id.secret adminpw 
```

```shell script
docker run --rm -it \
    --name enroll.org1.admin.ca.client \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin2-home \
    -v /root/temp/org1-admin-home:/opt/test-admin2-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client enroll \
    -u http://org1.admin:adminpw@ca.com:7054
```

```shell script
mv /root/temp/org1-admin-home/msp/cacerts/* /root/temp/org1-admin-home/msp/cacerts/ca.pem
mkdir -p /root/temp/org1-admin-home/msp/tlscacerts
cp /root/temp/org1-admin-home/msp/cacerts/ca.pem  /root/temp/org1-admin-home/msp/tlscacerts/
```

```shell script
cat>/root/temp/org1-admin-home/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```


注册org2机构管理员
```go
docker run --rm -it \
    --name register.org1.admin \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org2.admin \
    --id.type admin \
    --id.affiliation org2 \
    --id.attrs 'hf.Revoker=true,admin=true' --id.secret adminpw 
```

```shell script
docker run --rm -it \
    --name enroll.org2.admin.ca.client \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin2-home \
    -v /root/temp/org2-admin-home:/opt/test-admin2-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client enroll \
    -u http://org2.admin:adminpw@ca.com:7054
```

```shell script
mv /root/temp/org2-admin-home/msp/cacerts/* /root/temp/org2-admin-home/msp/cacerts/ca.pem
mkdir -p /root/temp/org2-admin-home/msp/tlscacerts
cp /root/temp/org2-admin-home/msp/cacerts/ca.pem  /root/temp/org2-admin-home/msp/tlscacerts/
```

```shell script
cat>/root/temp/org2-admin-home/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```

注册org3机构管理员
```go
docker run --rm -it \
    --name register.org1.admin \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin-home \
    -v /root/temp/test-ca-admin-home:/opt/test-admin-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client register \
    --id.name org3.admin \
    --id.type admin \
    --id.affiliation org3 \
    --id.attrs 'hf.Revoker=true,admin=true' --id.secret adminpw 
```

```shell script
docker run --rm -it \
    --name enroll.org3.admin.ca.client \
    --network bc-net \
    -e FABRIC_CA_CLIENT_HOME=/opt/test-admin2-home \
    -v /root/temp/org3-admin-home:/opt/test-admin2-home \
    hyperledger/fabric-ca:1.4.3 \
    fabric-ca-client enroll \
    -u http://org3.admin:adminpw@ca.com:7054
```

```shell script
mv /root/temp/org3-admin-home/msp/cacerts/* /root/temp/org3-admin-home/msp/cacerts/ca.pem
mkdir -p /root/temp/org3-admin-home/msp/tlscacerts
cp /root/temp/org3-admin-home/msp/cacerts/ca.pem  /root/temp/org3-admin-home/msp/tlscacerts/
```

```shell script
cat>/root/temp/org3-admin-home/msp/config.yaml<<EOF
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca.pem
    OrganizationalUnitIdentifier: orderer
EOF
```


# 创建通道
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    -v /root/temp:/opt/orderer_data \
    hyperledger/fabric-tools:1.4.3 \
    peer channel create --outputBlock /opt/orderer_data/mychannel.block -o orderer.com:7050 \
    -c mychannel \
    -f /opt/orderer_data/channel.tx \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

# 加入通道 
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    -v /root/temp:/opt/orderer_data \
    hyperledger/fabric-tools:1.4.3 \
    peer channel join -b /opt/orderer_data/mychannel.block \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

# 列出通道
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer channel list
```


# org2加入通道 
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org2MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org2.peer0.com:7051 \
    -v /root/temp/org2-admin-home/msp:/etc/hyperledger/fabric/msp \
    -v /root/temp:/opt/orderer_data \
    hyperledger/fabric-tools:1.4.3 \
    peer channel join -b /opt/orderer_data/mychannel.block \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

# 列出通道
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org2MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org2.peer0.com:7051 \
    -v /root/temp/org2-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer channel list
```

# org3加入通道 
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org3MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org3.peer0.com:7051 \
    -v /root/temp/org3-admin-home/msp:/etc/hyperledger/fabric/msp \
    -v /root/temp:/opt/orderer_data \
    hyperledger/fabric-tools:1.4.3 \
    peer channel join -b /opt/orderer_data/mychannel.block \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

# 列出通道
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org3MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org3.peer0.com:7051 \
    -v /root/temp/org3-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer channel list
```

# 安装合约
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    registry.cn-chengdu.aliyuncs.com/nox60/chaincode1:0.0.1 \
    peer chaincode install \
    -n chain1 \
    -v 1.1 \
    -l golang \
    -p chain1
```

# 实例化合约
```go
docker run --rm  -it \
    -e FABRIC_LOGGING_SPEC="DEBUG" \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode instantiate  -o orderer.com:7050\
    -C mychannel \
    -n chain1 \
    -v 1.1 \
    -l golang \
    -c '{"Args":["init","a","100","b","200"]}' -P 'OR ('\''org1MSP.peer'\'')' \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```


查看已安装的合约
```go
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode list\
    -C mychannel \
    --installed \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

查看已实例化合约
```shell script
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode list\
    -C mychannel \
    --instantiated \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

```shell script
docker run --rm -it \
    --name create.channel.client \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=peer0.com:7051 \
    -v /root/temp/org1-writer-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode list\
    -C mychannel \
    --instantiated \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

执行合约
```shell script
docker run --rm -it \
    --name apply.chain.code \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode invoke \
    -o orderer.com:7050 \
    -C mychannel \
    -n chain1 \
    -c '{"Args":["add","a","10","bb","t"]}' \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```

查询
```shell
docker run --rm -it \
    --name apply.chain.code \
    --network bc-net \
    -e CORE_PEER_LOCALMSPID=org1MSP \
    -e CORE_PEER_TLS_ENABLED="true"  \
    -e CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/msp/cacerts/ca.pem \
    -e CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/msp \
    -e CORE_PEER_ADDRESS=org1.peer0.com:7051 \
    -v /root/temp/org1-admin-home/msp:/etc/hyperledger/fabric/msp \
    hyperledger/fabric-tools:1.4.3 \
    peer chaincode invoke \
    -o orderer.com:7050 \
    -C mychannel \
    -n chain1 \
    -c '{"Args":["query","a"]}' \
    --tls true \
    --cafile /etc/hyperledger/fabric/msp/cacerts/ca.pem
```


