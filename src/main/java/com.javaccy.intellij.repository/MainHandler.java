package com.javaccy.intellij.repository;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainHandler extends AbstractHandler {

    private static String downloadPath = "/download/";
    private static String uploadPath = "/upload/";
    private static String regPath = "/reg/";

    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB




    public static File getUploadDir(String username,String child) {
        File dir = new File(Main.getUploadDir() + File.separator + username + File.separator + child + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }





    public static Double getVersion(String str) {
        StringBuffer sb = new StringBuffer();
        boolean flag = true;
        for (char c : str.toCharArray()) {
            if ("0123456789".contains(c+"")) {
                sb.append(c);
            } else if (c == '.' && flag) {
                sb.append(c);
                flag = false;
            }
        }

        return Double.valueOf(sb.toString());
    }

    /**
     * 下载插件
     * @param request
     * @param response
     */
    public static void download(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("下载url："+request.getRequestURI());
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            String s = Main.getUploadDir() + File.separator + request.getRequestURI().substring(downloadPath.length(), request.getRequestURI().length());
            File file = new File(s);
            FileInputStream f = new FileInputStream(file);
            in = new BufferedInputStream(f);
            out = new BufferedOutputStream(response.getOutputStream());
            String filename=URLEncoder.encode(file.getName(),"utf-8");
            response.setHeader("Content-Disposition","attachment; filename="+filename+"");
            response.setCharacterEncoding("utf-8");
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = in.read(bytes))!=-1) {
                out.write(bytes,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void updatePlugins(HttpServletRequest request, HttpServletResponse response){
        //IC-181.4445.78
        String build = request.getParameter("build");
        Main.log("build:"+build);
        Double clientVer = 0D;
        int clientBigVer = 0;
        if (build != null) {
            clientVer = getVersion(build);
            clientVer.intValue();
        }
        Double version;
        int bigVersion;
        String fileName;
        PrintWriter out = null;
        try {
            response.setHeader("Content-Type","application/xml");
            out = response.getWriter();
            File dir = Main.getUploadDir();
            out.print("<plugins>");
            for (File username : dir.listFiles()) {
                if (username.isDirectory()) {
                    for (File id : username.listFiles()) {
                        if (id.isDirectory()) {
                            for (File file : id.listFiles()) {

                                fileName = file.getName();
                                if (file.isDirectory()||fileName.startsWith(".")) {
                                    continue;
                                }
                                version = getVersion(fileName);
                                bigVersion = version.intValue();
                                //判断大版本是否相同，例如：181.1124.23 = 181.32445.999
                                if (bigVersion == clientBigVer || clientBigVer == 0) {//clientBigVer = 0时全部返回方便测试
                                    if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                                        out.print("<plugin id=\"" + id.getName() + "\" url=\"" + Main.getBasePath() + downloadPath + username.getName() + File.separator + id.getName() + File.separator + file.getName() + "\" version=\""+version+"\"/>");
                                    }
                                }

                            }
                        }
                    }

                }
            }
            out.print("</plugins>");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (out != null) {
                out.flush();
                out.close();
            }

        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (target.startsWith(downloadPath)) {
            download(request,response);
        }else if(target.startsWith(regPath)) {
            reg(request,response);
        } else if (target.startsWith(uploadPath)) {
            upload(request,response);
        }else {
           updatePlugins(request,response);
        }
    }

    /**
     * 用户注册
     * @param request
     * @param response
     */
    public static void reg(HttpServletRequest request,HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null && username.trim().length() >= 2 && password != null && password.length() > 5) {
            File dir = new File(Main.getUploadDir() + File.separator + username);
            if (!dir.exists()) {
                dir.exists();
            }
        }
        System.out.println(request);
    }

    /**
     * 上传插件
     * @param request
     * @param response
     */
    public static void upload(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                // 配置上传参数
                DiskFileItemFactory factory = new DiskFileItemFactory();
                // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
                factory.setSizeThreshold(MEMORY_THRESHOLD);
                // 设置临时存储目录
                factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
                ServletFileUpload upload = new ServletFileUpload(factory);
                // 设置最大文件上传值
                upload.setFileSizeMax(MAX_FILE_SIZE);
                // 设置最大请求值 (包含文件和表单数据)
                upload.setSizeMax(MAX_REQUEST_SIZE);
                // 中文处理
                upload.setHeaderEncoding("UTF-8");
                // 构造临时路径来存储上传的文件
                // 这个路径相对当前应用的目录
                List<FileItem> fileItems = upload.parseRequest(request);
                Map<String, String> fields = new LinkedHashMap();
                for (FileItem fileItem : fileItems) {
                    if (fileItem.isFormField()) {
                        fields.put(fileItem.getFieldName(), fileItem.getString());
                        Main.log(fileItem.getFieldName() + ":" + fileItem.getString());
                    }
                }
                File file;
                for (FileItem fileItem : fileItems) {
                    if(!fileItem.isFormField()) {
                        file = new File(getUploadDir(fields.get("userName"),fields.get("xmlId")) + File.separator +  fileItem.getName());
                        if (file.getAbsolutePath().contains("..")) {
                            System.out.println("错误的路径:"+file.getAbsolutePath());
                        }
                        FileUtils.copyInputStreamToFile(fileItem.getInputStream(), file);
                        Main.log("上传成功；"+file.getAbsolutePath()+file.getName());
                    }
                }
                out = response.getWriter();
                out.write(" upload successful");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FileUploadException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            }
        }
    }
}
