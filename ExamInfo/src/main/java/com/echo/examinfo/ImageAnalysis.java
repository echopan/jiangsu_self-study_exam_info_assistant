package com.echo.examinfo;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/11/28 10:11
 * @description 图像分析
 */
class ImageAnalysis {
    // static String datapath="tessdata";
    static String language="eng";
    static String oriPath="temp/codeMsg.png";
    static String denoPath="temp/denoise.png";

    public static String recognizeImage(String inPath,String outPath){
        String code=null;
        //配置识别引擎
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(Config.iniMap.get("tessdata")); // 具体路径根据你的安装配置
        tesseract.setLanguage(ImageAnalysis.language); // 设置要识别的语言，英文字母为 "eng"
        inPath=null==inPath?ImageAnalysis.oriPath:inPath;
        outPath=null==outPath?ImageAnalysis.denoPath:outPath;
        try{
            //读取图片
            File inFile = new File(inPath);
            BufferedImage inImg = ImageIO.read(inFile);
            BufferedImage deImg = ImageAnalysis.denoise(inImg);
            File outFile = new File(outPath);
            ImageIO.write(deImg, "png", outFile);
            // 进行 OCR 识别
            code= tesseract.doOCR(outFile).trim();
            System.out.println("识别结果: " + code);
        }catch(Exception e){
            ExamInfo.createLog(e);
        }
        return code;
    }

    //降噪
    public static BufferedImage denoise(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        // 检测背景色
        int backgroundColor = detectBackgroundColor(image);

        // 检测最外层的颜色
        Map<Integer, Integer> edgeColorCounts = new HashMap<>();
        detectEdgeColors(image, edgeColorCounts, backgroundColor);

        // 将干扰色转换为背景色
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color = image.getRGB(x, y);
                if (edgeColorCounts.containsKey(color)) {
                    image.setRGB(x, y, backgroundColor);
                }
            }
        }
        return image;
    }

    private static int detectBackgroundColor(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Map<Integer, Integer> colorCounts = new HashMap<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color = image.getRGB(x, y);
                colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
            }
        }

        int backgroundColor = 0;
        int maxCount = 0;

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                backgroundColor = entry.getKey();
            }
        }
        return backgroundColor;
    }

    private static void detectEdgeColors(BufferedImage image, Map<Integer, Integer> edgeColorCounts, int backgroundColor) {
        int w = image.getWidth();
        int h = image.getHeight();

        // 边缘扫描
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (isOnEdge(x, y, w, h)) {
                    int color = image.getRGB(x, y);
                    if (color != backgroundColor) {
                        edgeColorCounts.put(color, edgeColorCounts.getOrDefault(color, 0) + 1);
                    }
                }
            }
        }
    }

    private static boolean isOnEdge(int x, int y, int w, int h) {
        return x == 0 || x == w - 1 || y == 0 || y == h - 1;
    }
}
