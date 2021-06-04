package com.grpcserver.server.fabric;

import org.apache.commons.compress.utils.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.*;
import java.security.PrivateKey;
import java.security.Security;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FabricClient extends Thread {

    private FabricVO fabricVO;
    public void buildFabricVO( FabricVO fabricVO){
        this.fabricVO = fabricVO;
    }

    @Override
    public void run() {
        try {
            addRecord(this.fabricVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void main(String args[]) {
        FabricVO fabricVO1 = new FabricVO();
        fabricVO1.setId("a");
        try {
            queryRecord(fabricVO1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FabricVO fabricVO = new FabricVO();
        fabricVO.setId("TestCodeleee");
        fabricVO.setName("heellsfffasdfasdfasdfasdfasdfasdfasdf33333333333333333");
        fabricVO.setContent("hibeyttt");
        fabricVO.setType("cakgaad");
        try {
            addRecord(fabricVO);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void queryRecord(FabricVO fabricVO) throws Exception {
        //创建User实例
        String keyFile = FabricConfig.keyFile;
        String certFile = FabricConfig.certFile;

        LocalUser user = new LocalUser("org1.admin", "org1MSP", keyFile, certFile);
        user.setEnrollment(keyFile, certFile);
        user.loadFromPemFile(keyFile, certFile);
        //创建HFClient实例
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user);

        HFCAClient aa = null;

        //创建通道实例
        Channel channel = client.newChannel("mychannel");
        Peer peer = InitPeerTLS(
                client,
                true,
                "org1.peer0.com",
                FabricConfig.peer0Ip,
                "7051",
                FabricConfig.org1AdminHomeCa);
        channel.addPeer(peer);
        // Orderer orderer = client.newOrderer("ordererMSP", "grpc://192.168.88.128:7050");
        // channel.addOrderer(orderer);
        // channel.initialize();

        Orderer orderer2 = InitOrderer(client,
                true,
                "orderer.com",
                FabricConfig.ordererIp,
                "7050",
                FabricConfig.ordererAdminHomeCa);

        //组建channel
        channel.addOrderer(orderer2);
        channel.initialize();

        //查询链码
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("chain1").build();
        req.setChaincodeID(cid);
        req.setFcn("query");
        req.setArgs(new String[]{fabricVO.getId()});

        Collection<ProposalResponse> invokePropResp = channel.queryByChaincode(req);
        // System.out.format("rsp message => %s\n", rsp[0].getProposalResponse().getResponse().getPayload().toStringUtf8());
        int code = 400;
        String msg = "";
        for (ProposalResponse pr : invokePropResp) {
            code = pr.getChaincodeActionResponseStatus();
            msg = pr.getMessage();
            if (pr.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                if (pr.getChaincodeActionResponseStatus() != 200) {
                    System.out.println(pr.getChaincodeActionResponseStatus());
                    System.out.println(pr.getMessage());
                    // return ResponseVO.errorRes(pr.getChaincodeActionResponseStatus(), pr.getMessage());
                }
                // byte[] byteArr = pr.getProposalResponse().getPayload().toByteArray();
                String resultStr = pr.getProposalResponse().getResponse().getPayload().toStringUtf8();
                System.out.println(resultStr);
                System.out.printf("successful transaction proposal response Txid : %s from peer: %s", pr.getTransactionID(), pr.getPeer().getName());
                // successful.add(pr);
            } else {
                // failurful.add(pr);
                System.out.printf("fail %s", pr.getMessage());
            }
        }

        //提交链码交易
//        TransactionProposalRequest req2 = client.newTransactionProposalRequest();
//        req2.setChaincodeID(cid);
//        req2.setFcn("inc");
//        req2.setArgs("10");
//        Collection<ProposalResponse> rsp2 = channel.sendTransactionProposal(req2);
//        BlockEvent.TransactionEvent event = channel.sendTransaction(rsp2).get();
//        System.out.format("txid: %s\n", event.getTransactionID());
//        System.out.format("valid: %b\n", event.isValid());
    }

    public static void addRecord(FabricVO fabricVO) throws Exception {
        //创建User实例
        String keyFile = FabricConfig.keyFile;
        String certFile = FabricConfig.certFile;

        //第四个参数：GOV_USER_ADMINCERTS_PATH="peerOrganizations/gov.dams.com/users/Admin@gov.dams.com/msp/admincerts/Admin@gov.dams.com-cert.pem";
        //                 "E:\\temp\\org1-admin-home\\msp\\signcerts\\cert.pem",
        LocalUser user = LocalUser.getFabricUser4Local("org1.admin",
                "org1MSP",
                "org1MSP",
                FabricConfig.certFile,
                FabricConfig.keyFile);


       // LocalUser user = new LocalUser("org1.admin", "org1MSP", keyFile, certFile);
       // user.setEnrollment(keyFile, certFile);
       // user.loadFromPemFile(keyFile, certFile);
        //创建HFClient实例
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user);

        HFCAClient aa = null;

        //创建通道实例
      //  Channel channel = client.newChannel("mychannel");
      // addChannelToFabricNetWork
        Peer peer = InitPeerTLS(
                client,
                true,
                "org1.peer0.com",
                FabricConfig.peer0Ip,
                "7051",
                FabricConfig.org1AdminHomeCa);
        // channel.addPeer(peer);
        // Orderer orderer = client.newOrderer("ordererMSP", "grpc://192.168.88.128:7050");
        // channel.addOrderer(orderer);
        // channel.initialize();
        List<Peer> allPeerList = new ArrayList<Peer>();
        allPeerList.add(peer);
        Orderer orderer2 = InitOrderer(client,
                true,
                "orderer.com",
                FabricConfig.ordererIp,
                "7050",
                FabricConfig.ordererAdminHomeCa);

        Channel channel = InitChannel(client,
                "mychannel",
                allPeerList,
                orderer2,
                null);

        //组建channel
//        channel.addOrderer(orderer2);
//        channel.initialize();

        //查询链码
        TransactionProposalRequest req = client.newTransactionProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("chain1").build();
        Collection<ProposalResponse> successful = new LinkedList<>();

        req.setChaincodeID(cid);
        req.setFcn("add");
        req.setProposalWaitTime(300000);
        req.setArgs(new String[]{
                fabricVO.getId(),
                fabricVO.getName(),
                fabricVO.getType(),
                fabricVO.getContent()
        });
        req.setUserContext(user);
        req.setChaincodeName("chain1");

        // invokePropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
        Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(req, channel.getPeers());
        // System.out.format("rsp message => %s\n", rsp[0].getProposalResponse().getResponse().getPayload().toStringUtf8());
        int code = 400;
        String msg = "";
        for (ProposalResponse pr : invokePropResp) {
            code = pr.getChaincodeActionResponseStatus();
            msg = pr.getMessage();
            if (pr.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                if (pr.getChaincodeActionResponseStatus() != 200) {
                    System.out.println(pr.getChaincodeActionResponseStatus());
                    System.out.println(pr.getMessage());
                    // return ResponseVO.errorRes(pr.getChaincodeActionResponseStatus(), pr.getMessage());
                }
                // byte[] byteArr = pr.getProposalResponse().getPayload().toByteArray();
                String resultStr = pr.getProposalResponse().getResponse().getPayload().toStringUtf8();
                System.out.println("pr: " + pr);
                System.out.println(resultStr);
                System.out.printf("successful transaction proposal response Txid : %s from peer: %s", pr.getTransactionID(), pr.getPeer().getName());
                successful.add(pr);
            } else {
                // failurful.add(pr);
                System.out.printf("fail %s", pr.getMessage());
            }

            // channel.sendTransaction(successful, user);
            BlockEvent.TransactionEvent event = channel.sendTransaction(successful, user).get(60, TimeUnit.SECONDS);
            System.out.println(event);

        }
    }

    public static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException {
        final Reader pemReader = new StringReader(new String(data));
        final PrivateKeyInfo pemPair;
        PEMParser pemParse = new PEMParser(pemReader);
        pemPair = (PrivateKeyInfo) pemParse.readObject();
        PrivateKey pk = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
        pemParse.close();
        return pk;
    }


    /**
     * 根据路劲获取文件io
     *
     * @param path
     * @return
     */
    public static InputStream getCurrentThreadInputStrem(String path) {
        // path = "test" + File.separator + path;
        File f = new File(path);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return inputStream;
    }

    /**
     * @param enableTLS             是否开启Tls
     * @param peerHostNameOverride  peer的id名称
     * @param peerIP                peer的ip名称
     * @param peerPort              peer的端口号
     * @param peerTlsServerCertPath peer节点的tls
     * @return
     * @功能描述：初始化peer
     */
    public static Peer InitPeerTLS(HFClient hfClient, boolean enableTLS, String peerHostNameOverride, String peerIP, String peerPort, String peerTlsServerCertPath) throws Exception {
        Peer peer = null;
        try {
            if (enableTLS == true) {
                String pemStr = null;
                try {
                    pemStr = new String(IOUtils.toByteArray(getCurrentThreadInputStrem(peerTlsServerCertPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Properties peer_properties = new Properties();
                peer_properties.put("pemBytes", pemStr.getBytes());
                peer_properties.setProperty("sslProvider", "openSSL");
                peer_properties.setProperty("negotiationType", "TLS");
                peer_properties.setProperty("hostnameOverride", peerHostNameOverride);
                try {
                    peer = hfClient.newPeer(peerHostNameOverride, "grpcs://" + peerIP + ":" + peerPort, peer_properties);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                peer = hfClient.newPeer(peerHostNameOverride, "grpc://" + peerIP + ":" + peerPort);
            }
            return peer;
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Channel InitChannel(HFClient hfClient, String channelName, List<Peer> peerList, Orderer orderer, EventHub eventHub ) throws Exception {
        try {
            Channel channel = hfClient.newChannel(channelName);
            if (peerList==null){
                throw new Exception("peer为空,创建通道失败");
            }
            int count=peerList.size();
            for (int i=0;i<count;i++) {
                channel.addPeer(peerList.get(i));
            }

            if(orderer==null){
                throw new Exception("orderer为空,创建通道失败");
            }
            channel.addOrderer(orderer);
            if(eventHub!=null){
                channel.addEventHub(eventHub);
            }
            channel.initialize();
            return channel;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public static Orderer InitOrderer(HFClient hfClient, boolean enableTLS, String ordererHostNameOverride, String ordererIP, String ordererPort, String ordererTlsServerCertPath) throws Exception {
        Orderer orderer = null;
        try {
            if (enableTLS == true) {
                String pemStr = new String(IOUtils.toByteArray(getCurrentThreadInputStrem(ordererTlsServerCertPath)));
                ;
                Properties orderer_properties = new Properties();
                orderer_properties.put("pemBytes", pemStr.getBytes());
                orderer_properties.setProperty("sslProvider", "openSSL");
                orderer_properties.setProperty("negotiationType", "TLS");
                orderer_properties.setProperty("hostnameOverride", ordererHostNameOverride);
                orderer = hfClient.newOrderer(ordererHostNameOverride, "grpcs://" + ordererIP + ":" + ordererPort, orderer_properties);
            } else {
                orderer = hfClient.newOrderer(ordererHostNameOverride, "grpc://" + ordererIP + ":" + ordererPort);
            }
            return orderer;
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
