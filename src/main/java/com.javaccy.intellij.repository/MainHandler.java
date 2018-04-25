package com.javaccy.intellij.repository;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainHandler extends AbstractHandler {

    private static String downloadPath = "/download/";
    private static String uploadPath = "/upload/";
    private static String regPath = "/reg/";

    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "upload";
    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

    public static File getUploadDir() {
        File dir = new File(new File("").getAbsolutePath() + File.separator + UPLOAD_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }


    public static File getUploadDir(String username,String child) {
        File dir = new File(getUploadDir() + File.separator + username + File.separator + child + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }



    public static String getBasePath(HttpServletRequest request){
        return "http://127.0.0.1:6868";
    }

    /**
     * 下载插件
     * @param request
     * @param response
     */
    public static void download(HttpServletRequest request,HttpServletResponse response) {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            String s = getUploadDir() + File.separator + request.getRequestURI().substring(downloadPath.length(), request.getRequestURI().length());
            in = new BufferedInputStream(new FileInputStream(s));
            out = new BufferedOutputStream(response.getOutputStream());
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
        PrintWriter out = null;
        try {
            response.setHeader("Content-Type","application/xml");
            out = response.getWriter();
            File dir = getUploadDir();
            out.print("<plugins>");
            String version = "";
            for (File username : dir.listFiles()) {
                if (username.isDirectory()) {
                    for (File id : username.listFiles()) {
                        if (id.isDirectory()) {
                            for (File file : id.listFiles()) {
                                version = file.getName().substring(file.getName().indexOf(".")+2,file.getName().lastIndexOf("."));
                                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                                    out.print("<plugin id=\"" + id.getName() + "\" url=\"" + getBasePath(request) + downloadPath + username.getName() + File.separator + id.getName() + File.separator + file.getName() + "\" version=\""+version+"\"/>");
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
            File dir = new File(getUploadDir()+File.separator+username);
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
                    }
                }
                for (FileItem fileItem : fileItems) {
                    if(!fileItem.isFormField()) {
                        fileItem.write(new File(getUploadDir(fields.get("userName"),fields.get("xmlId")) + File.separator +  fileItem.getName()));
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
