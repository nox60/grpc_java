package com.grpcserver.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void main(String args[]) {

    }

    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }

    public static String getFiles(){
        StringBuffer stringBuffer = new StringBuffer();
        List<String> files = getFilesByPathAndSuffix();
        if( files!=null && files.size()>0){

            for( int i =0;i<files.size();i++){
                stringBuffer.append("<tr>");
                stringBuffer.append("<td>");
                stringBuffer.append(files.get(i));
                stringBuffer.append("</td>");
                stringBuffer.append("<td>");

                // 读取文件内容
                stringBuffer.append(readFileContent(files.get(i)));

                stringBuffer.append("</td>");
                stringBuffer.append("<td>");
                stringBuffer.append("<input type=\"radio\" name=\"selectedfile\" value=\""+files.get(i)+"\">");
                stringBuffer.append("</td>");
                stringBuffer.append("</tr>");
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
