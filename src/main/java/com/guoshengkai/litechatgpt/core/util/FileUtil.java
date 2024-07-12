package com.guoshengkai.litechatgpt.core.util;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.rendering.PDFRenderer;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFParagraph;
//import org.apache.poi.xwpf.usermodel.XWPFSDTCell;
//import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static String readFile(File str) {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (FileInputStream in = new FileInputStream(str)){
            for (int i = in.read(buffer); i > 0; i = in.read(buffer)){
                out.write(buffer, 0, i);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        try {
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readFile(File str, FileReaderHandler handler) {
        byte[] buffer = new byte[4096];
        try (FileInputStream in = new FileInputStream(str)){
            for (int i = in.read(buffer); i > 0; i = in.read(buffer)){
                handler.read(Arrays.copyOf(buffer, i));
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void readStream(InputStream in, FileReaderHandler handler) throws IOException{
        byte[] buffer = new byte[4096];
        for (int i = in.read(buffer); i > 0; i = in.read(buffer)){
            handler.read(Arrays.copyOf(buffer, i));
        }
    }

    public static byte[] readFile2Byte(File file){
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (FileInputStream in = new FileInputStream(file)){
            for (int i = in.read(buffer); i > 0; i = in.read(buffer)){
                out.write(buffer, 0, i);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    public static void writeFile(byte[] bytes, File file) {
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copy(File source, File target) {
        if (!target.getParentFile().exists()){
            target.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(target)){
            readFile(source, out::write);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static void writeFolder(String path, File dest) {
        // 先获得资源文件
        List<String> projectResources = getProjectResources(path);
        for (String p: projectResources){
            writeFile(readResource(p), new File(dest, p.split(path)[1]));
        }
    }

    /**
     * 获得自身项目指定路径下的所有资源文件
     * @param path
     *      项目资源路径
     * @return
     */
    @SneakyThrows
    public static List<String> getProjectResources(String path){
        List<String> result = new LinkedList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()){
            URL url = resources.nextElement();
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol)) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null) {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null) {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.startsWith(path) &&
                                    !jarEntryName.startsWith(path + "/.idea") &&
                                    !jarEntryName.endsWith("/")){
                                result.add(jarEntryName);
                            }
                        }
                    }
                }
            }else if("file".equalsIgnoreCase(protocol)){
                //从maven子项目中扫描
                File file = new File(url.getFile());
                result = getFolderResources(file, path);
            }
        }
        return result;
    }

    private static List<String> getFolderResources(File file, String split){
        LinkedList<String> result = new LinkedList<>();
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File sf: files){
                result.addAll(getFolderResources(sf, split));
            }
        }else{
            String fileName = file.getAbsolutePath();
            if (fileName.contains(split)){
                fileName = split + fileName.split(split)[1];
            }
            result.add(fileName);
        }
        return result;
    }

    public static void writeStream(InputStream inputStream, File source) {
        if (!source.getParentFile().exists()){
            source.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(source)){
            readStream(inputStream, out::write);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage mergeImage(List<BufferedImage> bufferedImages) {
        int height = 0;
        int width = 0;
        for (BufferedImage bufferedImage: bufferedImages){
            height += bufferedImage.getHeight();
            width = Math.max(width, bufferedImage.getWidth());
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int y = 0;
        for (BufferedImage bufferedImage: bufferedImages){
            image.createGraphics().drawImage(bufferedImage, 0, y, null);
            y += bufferedImage.getHeight();
        }
        return image;
    }

//    public static String readWordText(byte[] bytes) {
//        try (InputStream in = new ByteArrayInputStream(bytes)){
//            XWPFDocument document = new XWPFDocument(in);
//            StringBuilder builder = new StringBuilder();
//            document.getBodyElements().forEach(bodyElement -> {
//                if (bodyElement instanceof XWPFParagraph){
//                    builder.append(((XWPFParagraph) bodyElement).getText()).append("\n");
//                }
//                if (bodyElement instanceof XWPFTable){
//                    ((XWPFTable) bodyElement).getRows().forEach(row -> {
//                        row.getTableCells().forEach(cell -> {
//                            cell.getParagraphs().forEach(p -> builder.append(p.getText()).append("\t"));
//                        });
//                        builder.append("\n");
//                    });
//                }
//                if (bodyElement instanceof XWPFSDTCell){
//                    builder.append(((XWPFSDTCell) bodyElement).getContent().getText()).append("\n");
//                }
//            });
//            return builder.toString();
//        }catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String readWordTextOld(byte[] bytes) {
//        // doc而不是docx
//        try (InputStream in = new ByteArrayInputStream(bytes)){
//            HWPFDocument document = new HWPFDocument(in);
//            StringBuilder builder = new StringBuilder();
//            for (int i = 0; i < document.getRange().numParagraphs(); i++){
//                builder.append(document.getRange().getParagraph(i).text()).append("\n");
//            }
//            return builder.toString();
//        }catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String readExcelText(byte[] bytes) {
//        try (InputStream in = new ByteArrayInputStream(bytes)){
//            XSSFWorkbook sheets = new XSSFWorkbook(in);
//            List<Map<String, Object>> result = new LinkedList<>();
//            sheets.forEach(sheet -> {
//                Map<String, Object> sheetMap = new HashMap<>();
//                LinkedList<Object> rows = new LinkedList<>();
//                sheetMap.put("sheetName", sheet.getSheetName());
//                sheetMap.put("rows", rows);
//                result.add(sheetMap);
//                sheet.forEach(row -> {
//                    List<String> cells = new LinkedList<>();
//                    row.forEach(cell -> {
//                        CellType cellType = cell.getCellType();
//                        if (cellType == CellType.NUMERIC){
//                            cells.add(String.valueOf(cell.getNumericCellValue()));
//                        }else if (cellType == CellType.STRING){
//                            cells.add(cell.getStringCellValue());
//                        }else if (cellType == CellType.BOOLEAN) {
//                            cells.add(String.valueOf(cell.getBooleanCellValue()));
//                        }else if (cellType == CellType.FORMULA){
//                            cells.add(cell.getCellFormula());
//                        }else if (cellType == CellType.BLANK){
//                            cells.add("");
//                        }else{
//                            cells.add("");
//                        }
//                    });
//                    rows.add(cells);
//                });
//            });
//            return JSON.toJSONString(result);
//        }catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String readExcelTextOld(byte[] bytes) {
//        try (InputStream in = new ByteArrayInputStream(bytes)){
//            HSSFWorkbook sheets = new HSSFWorkbook(in);
//            List<Map<String, Object>> result = new LinkedList<>();
//            sheets.forEach(sheet -> {
//                Map<String, Object> sheetMap = new HashMap<>();
//                LinkedList<Object> rows = new LinkedList<>();
//                sheetMap.put("sheetName", sheet.getSheetName());
//                sheetMap.put("rows", rows);
//                result.add(sheetMap);
//                sheet.forEach(row -> {
//                    List<String> cells = new LinkedList<>();
//                    row.forEach(cell -> {
//                        CellType cellType = cell.getCellType();
//                        if (cellType == CellType.NUMERIC){
//                            cells.add(String.valueOf(cell.getNumericCellValue()));
//                        }else if (cellType == CellType.STRING){
//                            cells.add(cell.getStringCellValue());
//                        }else if (cellType == CellType.BOOLEAN) {
//                            cells.add(String.valueOf(cell.getBooleanCellValue()));
//                        }else if (cellType == CellType.FORMULA){
//                            cells.add(cell.getCellFormula());
//                        }else if (cellType == CellType.BLANK){
//                            cells.add("");
//                        }else{
//                            cells.add("");
//                        }
//                    });
//                    rows.add(cells);
//                });
//            });
//            return JSON.toJSONString(result);
//        }catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }


    public interface FileReaderHandler{
        void read(byte[] bytes) throws IOException;
    }

    public static String getFristXmlNodeContent(String xmlContent, String nodeName){
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        NodeList nodeList = document.getElementsByTagName(nodeName);
        if (nodeList.getLength() == 0){
            return null;
        }
        return nodeList.item(0).getTextContent();
    }

    /**
     * 复制一个目录及其子目录、文件到另外一个目录
     * @param src
     *      源目录
     * @param dest
     *      目标目录
     * @throws IOException
     */
    public static void copyFolder(File src, File dest){
        if (src.isDirectory()) {
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            if (!dest.exists()) {
                System.out.println("mkdir: " + dest.getAbsolutePath());
                dest.mkdirs();
            }
            String[] files = src.list();
            if (null == files){
                return;
            }
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            System.out.println("write: " + dest.getAbsolutePath());
            try (InputStream in = new FileInputStream(src);OutputStream out = new FileOutputStream(dest)){
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    public static void zip(File source, File target){
        if (!target.getParentFile().exists()){
            if (!target.getParentFile().mkdirs()){
                throw new RuntimeException("文件[" + source.getParentFile() + "]创建失败");
            }
        }
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target))){
            if (source.isDirectory()){
                for (File file: Objects.requireNonNull(source.listFiles())){
                    zip(file, out, file.getName());
                }
            }else{
                zip(source, out, null);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void unzip(InputStream stream, File target){
        if (!target.exists()){
            if (!target.mkdirs()){
                throw new RuntimeException("文件[" + target + "]创建失败");
            }
        }
        try (ZipInputStream in = new ZipInputStream(stream)){
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()){
                if (entry.isDirectory()){
                    File file = new File(target, entry.getName());
                    if (!file.exists()){
                        file.mkdirs();
                    }
                }else{
                    try(FileOutputStream out = new FileOutputStream(new File(target, entry.getName()))) {
                        readStream(in, out::write);
                    }
                }
                in.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void unzip(File source, File target){
        unzip(new FileInputStream(source), target);
    }

    private static void zip(File source, ZipOutputStream out, String name) throws IOException{
        if (null == name){
            name = source.getName();
        }
        if (source.isDirectory()){
            out.putNextEntry(new ZipEntry(name + "/"));
            for (File file: Objects.requireNonNull(source.listFiles())){
                zip(file, out, name + "/" + file.getName());
            }
        }else{
            out.putNextEntry(new ZipEntry(name));
            readFile(source, out::write);
        }
    }

    public static byte[] readResource(String path){
        ClassPathResource classPathResource = new ClassPathResource(path);
        byte[] bs = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(InputStream in = classPathResource.getInputStream()){
            for (int i = in.read(bs); i > 0; i = in.read(bs)){
                out.write(bs, 0, i);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    public static void deleteAll(File parentFile) {
        File[] files = parentFile.listFiles();
        if (null != files){
            for (File file: files){
                if (file.isDirectory()) {
                    deleteAll(file);
                }
                if (!file.delete()){
                    throw new RuntimeException("Clean File Error: " + file.getAbsolutePath());
                }
            }
        }
    }
    public static void rm(File file) {
        deleteAll(file);
        file.delete();
    }

//    public static String readPDF(File file){
//        File tessdata = new File("tessdata");
//        if (!tessdata.exists()){
//            writeFolder("tessdata", tessdata);
//        }
//        StringBuilder builder = new StringBuilder();
//        Tesseract tesseract = new Tesseract();
//        tesseract.setDatapath(tessdata.getAbsolutePath());
//        tesseract.setLanguage("chi_sim+eng");
//        pdf2Image(file).forEach(image -> {
//            try {
//                String s = tesseract.doOCR(image);
//                builder.append(s);
//            } catch (TesseractException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        return builder.toString();
//    }
//
//    public static List<BufferedImage> pdf2Image(File file){
//        return pdf2Image(readFile2Byte(file));
//    }
//
//    public static List<BufferedImage> pdf2Image(byte[] bytes){
//        List<BufferedImage> images = new LinkedList<>();
//        try (PDDocument document = Loader.loadPDF(bytes)){
//            PDFRenderer renderer = new PDFRenderer(document);
//            int pages = document.getNumberOfPages();
//            for (int i = 0; i < pages; i++){
//                BufferedImage bufferedImage = renderer.renderImage(i);
//                images.add(bufferedImage);
//            }
//        }catch (IOException e){
//            throw new RuntimeException(e);
//        }
//        return images;
//    }


    public static void main(String[] args) {
//        String s = readPDF(new File("C:\\Users\\71934\\Desktop\\370908.pdf"));
//        System.out.println(s);
    }
}