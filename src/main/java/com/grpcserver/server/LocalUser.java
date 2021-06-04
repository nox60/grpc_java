package com.grpcserver.server;

import org.apache.commons.compress.utils.IOUtils;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Set;

import static com.grpcserver.server.fabric.FabricClient.getCurrentThreadInputStrem;
import static com.grpcserver.server.fabric.FabricClient.getPrivateKeyFromBytes;

public class LocalUser implements User {             //实现User接口
    private String name;
    private String mspId;
    private Enrollment enrollment;

    public void setName(String name) {
        this.name = name;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    LocalUser(String name, String mspId) {
        this.name = name;
        this.mspId = mspId;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public static LocalUser getFabricUser4Local(String username, String org, String orgId, String userAdmincertsPermFilePath, String userPrimarykeyFilePath) {
        LocalUser user = new LocalUser(username, org);
        user.setMspId(orgId);
        try {
            //将生成的fabricTest/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem保存到本地的某个目录下
            String certificate = new String(IOUtils.toByteArray(getCurrentThreadInputStrem(userAdmincertsPermFilePath)));
            //将生成的fabricTest/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/keystore/****_sk文件保存到本地的某个目录下
            PrivateKey pk = getPrivateKeyFromBytes(IOUtils.toByteArray(getCurrentThreadInputStrem(userPrimarykeyFilePath)));
            EnrollmentImpl enrollement = new EnrollmentImpl(pk, certificate);
            user.setEnrollment(enrollement);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    static class EnrollmentImpl implements Enrollment, Serializable {

        private static final long serialVersionUID = 1L;
        private final PrivateKey privateKey;
        private final String certificate;

        public EnrollmentImpl(PrivateKey pk, String c) {
            this.privateKey = pk;
            this.certificate = c;
        }

        @Override
        public PrivateKey getKey() {
            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }

    }

    LocalUser(String name, String mspId, String keyFile, String certFile) {
        this.name = name;
        this.mspId = mspId;
        loadFromPemFile(keyFile, certFile);
    }

//    private  User getFabricUser4Local(String username, String mspId, String orgId,String userAdmincertsPermFilePath,String userPrimarykeyFilePath ) {
//        LocalUser user = new LocalUser(username, mspId);
//        try {
//            //将生成的fabricTest/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/admincerts/Admin@org1.example.com-cert.pem保存到本地的某个目录下
//            String certificate = new String(IOUtils.toByteArray(getCurrentThreadInputStrem(userAdmincertsPermFilePath)));
//            //将生成的fabricTest/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/keystore/****_sk文件保存到本地的某个目录下
//            PrivateKey pk = getPrivateKeyFromBytes(IOUtils.toByteArray(getCurrentThreadInputStrem(userPrimarykeyFilePath)));
//            EnrollmentImpl enrollement = new EnrollmentImpl(pk, certificate);
//            user.setEnrollment(enrollement);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return user;
//    }

    public void setEnrollment(String keyFile, String certFile) {
        byte[] keyPem = new byte[0];     //载入私钥PEM文本
        try {
            keyPem = Files.readAllBytes(Paths.get(keyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] certPem = new byte[0];   //载入证书PEM文本
        try {
            certPem = Files.readAllBytes(Paths.get(certFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CryptoPrimitives suite = null;            //载入密码学套件
        try {
            suite = new CryptoPrimitives();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        PrivateKey privateKey = null;    //将PEM文本转换为私钥对象
        try {
            privateKey = suite.bytesToPrivateKey(keyPem);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        this.enrollment = new X509Enrollment(privateKey, new String(certPem));  //创建并返回X509Enrollment对象
    }

    public Enrollment loadFromPemFile(String keyFile, String certFile) {
        byte[] keyPem = new byte[0];     //载入私钥PEM文本
        try {
            keyPem = Files.readAllBytes(Paths.get(keyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] certPem = new byte[0];   //载入证书PEM文本
        try {
            certPem = Files.readAllBytes(Paths.get(certFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CryptoPrimitives suite = null;            //载入密码学套件
        try {
            suite = new CryptoPrimitives();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        PrivateKey privateKey = null;    //将PEM文本转换为私钥对象
        try {
            privateKey = suite.bytesToPrivateKey(keyPem);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return new X509Enrollment(privateKey, new String(certPem));  //创建并返回X509Enrollment对象
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }
}