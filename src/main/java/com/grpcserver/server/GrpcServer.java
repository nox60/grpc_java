package com.grpcserver.server;

import com.grpcserver.server.fabric.FabricClient;
import com.grpcserver.server.fabric.FabricVO;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.security.Security;
import java.util.*;

public class GrpcServer {

    private int port = 50051;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new MessageImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // block 一直到退出程序
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    // 主函数启动RPC服务
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("GRPC SERVER START......");

        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

    // 实现 定义一个实现服务接口的类
    private class MessageImpl extends MsgServiceGrpc.MsgServiceImplBase {
        @Override
        public void sendMsg(MsgRequest req, StreamObserver<MsgResponse> responseObserver) {
            String returnResult = "";
            if (req.getMsgCode().equals("1")) {
                returnResult = FileUtils.getFiles();
                //  } else if ( req.getUsername().indexOf("|||") > 0 ){
            } else if (req.getMsgCode().equals("2")) {
                //  String[] vvs = req.getUsername().split("\\|\\|\\|");
                String fileName = req.getMsgVolue();
                String fileContent = req.getMsgBody();
                System.out.println("getMsgCode   : " + req.getMsgCode());
                System.out.println("getMsgVolue  : " + req.getMsgVolue());
                System.out.println("getMsgBody   : " + req.getMsgBody());
                // 写入文件
                FileUtils.appendToFile(fileName, fileContent);

                returnResult = "refresh";
            } else if (req.getMsgCode().equals("3")) { // 写入区块链
                String fileName = req.getMsgVolue();
                String fileContent = req.getMsgBody();
                System.out.println("getMsgCode   : " + req.getMsgCode());
                System.out.println("getMsgVolue  : " + req.getMsgVolue());
                System.out.println("getMsgBody   : " + req.getMsgBody());
                // 写入区块链
                // FileUtils.appendToFile(fileName, fileContent);
                FabricVO fabricVO = new FabricVO();
                fabricVO.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                fabricVO.setName(req.getMsgVolue());
                fabricVO.setContent(fileContent);
                try {
                    // FabricClient.addRecord(fabricVO);
                    FabricClient newThread = new FabricClient();
                    newThread.buildFabricVO(fabricVO, FabricClient.WRITE);
                    newThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (req.getMsgCode().equals("4")) { // 读取区块链数据
                String fileName = req.getMsgVolue();
//                String fileContent = req.getMsgBody();
                System.out.println("getMsgCode   : " + req.getMsgCode());
                System.out.println("getMsgVolue  : " + req.getMsgVolue());
                System.out.println("getMsgBody   : " + req.getMsgBody());
                // 写入区块链
                // FileUtils.appendToFile(fileName, fileContent);
                FabricVO fabricVO = new FabricVO();
                fabricVO.setId(req.getMsgVolue());
                //      fabricVO.setName(req.getMsgVolue());
                try {
                    // FabricClient.addRecord(fabricVO);
                    FabricClient newThread = new FabricClient();
                    newThread.buildFabricVO(fabricVO, FabricClient.READ);
                    newThread.start();
                    newThread.join();
                    System.out.println("------>>");
                    System.out.println(newThread.queryResult);
                    returnResult = newThread.queryResult;
                    returnResult = Text2Html(returnResult);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MsgResponse reply = MsgResponse.newBuilder().setMessage(returnResult).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static String Text2Html(String str) {
        if (str == null) {
            return "";
        } else if (str.length() == 0) {
            return "";
        }
        str = str.replace("\\n", "<br/>");
        return str;
    }

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

}



