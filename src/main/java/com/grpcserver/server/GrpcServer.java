package com.grpcserver.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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

        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

    // 实现 定义一个实现服务接口的类
    private class MessageImpl extends MsgServiceGrpc.MsgServiceImplBase {
        @Override
        public void sendMsg(MsgRequest req, StreamObserver<MsgResponse> responseObserver) {
            String returnResult = "";
            if( req.getMsgCode().equals("1")){
                returnResult = FileUtils.getFiles();
          //  } else if ( req.getUsername().indexOf("|||") > 0 ){
            } else if ( req.getMsgCode().equals("2") ){
              //  String[] vvs = req.getUsername().split("\\|\\|\\|");
                String fileName = req.getMsgVolue();
                String fileContent = req.getMsgBody();

                // 写入文件
                FileUtils.appendToFile(fileName, fileContent);

                returnResult = "refresh";
            }

            MsgResponse reply = MsgResponse.newBuilder().setMessage( returnResult ).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }


}



