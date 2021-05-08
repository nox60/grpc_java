package com.grpcserver.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void main(String args[]) {

    }

    public static String getFiles(){
        StringBuffer stringBuffer = new StringBuffer();
        List<String> files = getFilesByPathAndSuffix();
        if( files!=null && files.size()>0){
            for( int i =0;i<files.size();i++){
                stringBuffer.append(files.get(i)).append("|~|!|");
            }
        }
        return stringBuffer.toString();
    }

    public static List<String> getFilesByPathAndSuffix() {
        File file = new File("D:\\grpc_files");
        List<String> tempList = new ArrayList<String>();
        for (File temp : file.listFiles()) {
            if (!temp.isDirectory()) {
                // System.out.println(temp.toString());
                tempList.add(temp.toString());
            }
        }
        return tempList;
    }
}
