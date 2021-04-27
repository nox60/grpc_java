package com.grpcserver.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void main(String args[]) {
        getFilesByPathAndSuffix();
    }

    public static List<String> getFilesByPathAndSuffix() {
        // TODO Auto-generated method stub
        File file = new File("D:\\grpc_files");
        List<String> tempList = new ArrayList<String>();
        for (File temp : file.listFiles()) {
            if (!temp.isDirectory()) {
                // System.out.println(temp.toString());
                tempList.add(temp.toString());
            }
        }
        return null;
    }
}
