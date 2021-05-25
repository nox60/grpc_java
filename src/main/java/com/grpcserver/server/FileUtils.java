package com.grpcserver.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {


    // public static final String DIR_PATH = "D:"+File.separator+"grpc_files"+File.separator;
    public static final String DIR_PATH = "D:"+File.separator+"grpc_files"+File.separator;

    // 读取文件详细信息
    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append("<p>");
                sbf.append(tempStr);
                sbf.append("</p>");
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

    // 获取文件列表
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
                stringBuffer.append("<div>"+readFileContent(files.get(i))+"</div>");

                stringBuffer.append("</td>");
                stringBuffer.append("<td>");
                stringBuffer.append("<input  type=\"radio\" name=\"selectedfile\" value=\""+files.get(i).replace(DIR_PATH.toString(),"")+"\">");
                stringBuffer.append("</td>");
                stringBuffer.append("</tr>");
            }

        }
        return stringBuffer.toString();
    }

    // 获取文件内容
    public static List<String> getFilesByPathAndSuffix() {
        File file = new File(DIR_PATH);
        List<String> tempList = new ArrayList<String>();
        for (File temp : file.listFiles()) {
            if (!temp.isDirectory()) {
                tempList.add(temp.toString());
            }
        }
        return tempList;
    }

    public static void main(String args[]) {
        // getFilesByPathAndSuffix();
        String aaa = "asdfasdfasdfasdf|||asdfasdfasdf ";
        String ar[] = aaa.split("\\|\\|\\|");
        System.out.println(ar[0]);
        System.out.println(ar[1]);
    }

    // 追加新增的内容到文件末尾
    public static void appendToFile(String fileName, String content){
        try{
            File file = new File(DIR_PATH+fileName);
            FileOutputStream fos = null;
            if(!file.exists()){
                file.createNewFile();//如果文件不存在，就创建该文件
                fos = new FileOutputStream(file);//首次写入获取
            }else{
                //如果文件已存在，那么就在文件末尾追加写入
                fos = new FileOutputStream(file,true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
            }

            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");//指定以UTF-8格式写入文件

//            //遍历list
//            for(Map<String,Object> map: list){
//                //遍历Map
//                for(Map.Entry<String, Object> entry : map.entrySet()){
//                    //以英文","逗号隔开多个写入的str，每个Map写一行
//                    String str = entry.getKey()+"="+entry.getValue();
//                    osw.write(str+",");
//                }
//
//                //每写入一个Map就换一行
//                osw.write("\r\n");
//            }
            osw.write(content);
            //写入完成关闭流
            osw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
