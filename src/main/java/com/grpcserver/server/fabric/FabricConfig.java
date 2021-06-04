package com.grpcserver.server.fabric;

public class FabricConfig {
    // "E:\\temp\\org1-admin-home\\msp\\keystore\\db12c1651db6793638610ac27c2cd02a3670b2f6309537b52e6b3c32cb1131b9_sk";
    public static String keyFile = "E:\\hyperledger\\org1-admin-home\\msp\\keystore\\ab4b03b0c4997be6d55b803eae1eec3605e3513e7282cf0f6a73ec4c3190b5d4_sk";

    // "E:\\temp\\org1-admin-home\\msp\\signcerts\\cert.pem"
    public static String certFile = "E:\\hyperledger\\org1-admin-home\\msp\\signcerts\\cert.pem";

    // "192.168.88.128",
    public static String peer0Ip = "192.168.88.131";

    // "E:\\temp\\org1-admin-home\\msp\\cacerts\\ca.pem"
    public static String org1AdminHomeCa = "E:\\hyperledger\\org1-admin-home\\msp\\cacerts\\ca.pem";

    // "192.168.88.128",
    public static String ordererIp = "192.168.88.131";

    //"E:\\temp\\orderer-admin-home\\msp\\cacerts\\ca.pem"
    public static String ordererAdminHomeCa = "E:\\hyperledger\\orderer-admin-home\\msp\\cacerts\\ca.pem";

    public static  void  main(String args[]){
        String testaaa = "tent\":\"[INFO] com.grpcserver:grpc_java:jar:1.0-SNAPSHOT\\n[INFO] +- junit:junit:jar:3.8.1:test\\n[INFO] +- io.grpc:grpc";
        System.out.println(testaaa.replaceAll("\n",""));

        testaaa = testaaa.replaceAll ( "\r",  "" );
        testaaa = testaaa.replaceAll ( "\n",  "\\\\"+System.getProperty("line.separator"));
        testaaa = testaaa.replace("\\n", "");
        System.out.println(testaaa);

    }
}
