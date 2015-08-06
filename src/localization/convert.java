/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package localization;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.mozilla.universalchardet.UniversalDetector;
/**
 *
 * @author wei7771
 */
public class convert {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final Charset UTF_16_LE = Charset.forName("UTF-16LE");
    public static final Charset UTF_16_BE = Charset.forName("UTF-16BE");
    
    public static void main(String[] args){
        String path = "C:\\Users\\wei7771\\Desktop\\test\\Geoprocessing Tools=GUID-CEBCBB99-9543-4CB7-887F-518263CA9BC9=8=en=.xml";
        String zipPath = "C:\\Users\\wei7771\\Desktop\\test\\Geoprocessing Tools=GUID-CEBCBB99-9543-4CB7-887F-518263CA9BC9=8=en=.xml";
        convert(zipPath);
    }
    
    public static void convert(String filepath){
        if(filepath.endsWith(".zip")){
            convert_encoding(filepath,"");
            String OUTPUT_ZIP_FILE_NAME = filepath.substring(0,filepath.lastIndexOf(".")) + ".zip";
            String SOURCE_FOLDER = filepath.substring(0,filepath.lastIndexOf("\\"));
            try{
                File originalZipFile = new File(filepath);
                originalZipFile.delete();
                zipFile(SOURCE_FOLDER,OUTPUT_ZIP_FILE_NAME,true);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else if(filepath.endsWith(".xml")){
            String FileEncoding = detectEncoding(filepath);
            if(!FileEncoding.equals("UTF-16LE")){
                convert_encoding(filepath,FileEncoding);
            }
            String OUTPUT_ZIP_FILE_NAME = filepath.substring(0,filepath.lastIndexOf(".")) + ".zip";
            String SOURCE_FOLDER = filepath.substring(0,filepath.lastIndexOf("\\"));
            try{
                zipFile(filepath,OUTPUT_ZIP_FILE_NAME,true);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            return;
        }

    }
    
    public static Vector<String> readzipfile(String filepath){
        Vector<String> v = new Vector<String>();
        byte[] buffer = new byte[1024];
        String outputFolder = filepath.substring(0,filepath.lastIndexOf("."));
        System.out.println(outputFolder);
        try{
            File folder = new File(outputFolder);
            if(!folder.exists()){
		folder.mkdir();
	  }
			
                ZipInputStream zis = new ZipInputStream(new FileInputStream(filepath));
		ZipEntry ze = zis.getNextEntry();
                while(ze != null){
                    String fileName = ze.getName();
		    File newFile = new File(outputFolder + "\\" + fileName);
		    v.addElement(newFile.getAbsolutePath());
		    FileOutputStream fos = new FileOutputStream(newFile);
		    int len;
		    while((len = zis.read(buffer)) > 0){
			fos.write(buffer, 0, len);
		     }
                fos.close();
                ze = zis.getNextEntry();
              }	 
	    zis.closeEntry();
	    zis.close();  
        }catch(Exception e){
            
        }
        return v;
    } 
    
    public static String detectEncoding(String filepath){
        byte[] buf = new byte[4096];
        String encoding = "";
        try{
            FileInputStream fis = new FileInputStream(filepath);
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                    detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            if (encoding != null) {
                //System.out.println(filepath + " Detected encoding = " + encoding);
            } else {
                System.out.println(filepath + " No encoding detected.");
            }
            detector.reset();
        }catch(Exception e){
            e.printStackTrace();
        }
          return encoding;
    }
    
    public static void convert_encoding(String filepath, String encoding){
        if(filepath.endsWith(".zip")){
            Vector<String> zFile = readzipfile(filepath);
            for(String s : zFile){
                if(s.endsWith(".xml")){
                    String FileEncoding = detectEncoding(s);
                    if(!FileEncoding.equals("UTF-16LE")){
                        convert_encoding(s,FileEncoding);
                    }
                }
            }
        }else if(filepath.endsWith(".xml") && (!encoding.isEmpty())){
            try{  
            FileInputStream inputStream = new FileInputStream(filepath);
            Charset inputCharset = Charset.forName(encoding);
            InputStreamReader reader = new InputStreamReader(inputStream, inputCharset);
            StringBuffer buffer = new StringBuffer();
            int character;
            while ((character = reader.read()) != -1) {
                buffer.append((char)character);
            }
            reader.close();
            File originFile = new File(filepath);
            originFile.delete();
            FileOutputStream fos = new FileOutputStream(filepath);
            Writer out = new OutputStreamWriter(fos,UTF_16_LE);
            out.write(buffer.toString());
            out.close();
            
            }catch(Exception e){
                 e.printStackTrace();
            }
        }
    }
 
   public static void zipFile(String fileToZip, String zipFile, boolean excludeContainingFolder) throws IOException {        
       ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));    
       File srcFile = new File(fileToZip);
       if(excludeContainingFolder && srcFile.isDirectory()) {
            for(String fileName : srcFile.list()) {
                File isFolder = new File(fileToZip + "/" + fileName);
                if (isFolder.isDirectory()){
                    addToZip("", fileToZip + "/" + fileName, zipOut);
                }
            }
        } 
        else {
            addToZip("", fileToZip, zipOut);
        }
        zipOut.flush();
        zipOut.close();
        System.out.println("Successfully created " + zipFile);
  }
    
     private static void addToZip(String path, String srcFile, ZipOutputStream zipOut) throws IOException 
  {        
    File file = new File(srcFile);
    String filePath = "".equals(path) ? file.getName() : path + "/" + file.getName();
    if (file.isDirectory()) 
    {
      for (String fileName : file.list()) 
      {             
        addToZip(filePath, srcFile + "/" + fileName, zipOut);
      }
    } 
    
    else 
    {
      zipOut.putNextEntry(new ZipEntry(filePath));
      FileInputStream in = new FileInputStream(srcFile);

      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int len;
      while ((len = in.read(buffer)) != -1) 
      {
        zipOut.write(buffer, 0, len);
      }

      in.close();
    }
  }
    
    
}
