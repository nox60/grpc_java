package com.grpcserver.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {


    // public static final String DIR_PATH = "D:"+File.separator+"grpc_files"+File.separator;
    public static final String DIR_PATH = "D:"+File.separator+"grpc_files"+File.separator;

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


        stringBuffer.append("<tr>");
        stringBuffer.append("<td>");
        stringBuffer.append("新建文件: <input type=\"text\" id = \"newfilename\"  name=\"newfilename\" maxlength=\"10\" value=\"\">");
        stringBuffer.append("</td>");
        stringBuffer.append("<td>");


        stringBuffer.append("</td>");
        stringBuffer.append("<td>");
        stringBuffer.append("<input type=\"radio\" name=\"selectedfile\" checked  value=\"NEWFILE\">");
        stringBuffer.append("</td>");
        stringBuffer.append("</tr>");


        if( files!=null && files.size()>0){

            for( int i =0;i<files.size();i++){
                stringBuffer.append("<tr>");
                stringBuffer.append("<td>");
                stringBuffer.append(files.get(i).replace(DIR_PATH.toString(),""));
                stringBuffer.append("</td>");
                stringBuffer.append("<td>");

                // 读取文件内容
                stringBuffer.append(readFileContent(files.get(i)));

                stringBuffer.append("</td>");
                stringBuffer.append("<td>");
                stringBuffer.append("<input  type=\"radio\" name=\"selectedfile\" value=\""+files.get(i).replace(DIR_PATH.toString(),"")+"\">");
                stringBuffer.append("</td>");
                stringBuffer.append("</tr>");
            }

        }
        return stringBuffer.toString();
    }

    public static List<String> getFilesByPathAndSuffix() {
        File file = new File(DIR_PATH);
        List<String> tempList = new ArrayList<String>();
        for (File temp : file.listFiles()) {
            if (!temp.isDirectory()) {
//                System.out.println("----------------------");
//                System.out.println(temp.toString());
//                System.out.println(DIR_PATH);
//                System.out.println(temp.toString().replace(DIR_PATH.toString(),""));
//                System.out.println("----------------------");
                tempList.add(temp.toString());
            }
        }
        return tempList;
    }

    public static void main(String args[]) {
        getFilesByPathAndSuffix();
    }
}
