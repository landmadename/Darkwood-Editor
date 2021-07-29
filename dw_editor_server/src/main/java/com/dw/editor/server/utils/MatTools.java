package com.dw.editor.server.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.xml.bind.DatatypeConverter;

public class MatTools {
    public static Mat base64ToMat(String data){
        data = data.replace("=", "");
        data = data.substring(data.indexOf(",")+1);
        byte[] imgbytes = DatatypeConverter.parseBase64Binary(data);
        Mat mat = Imgcodecs.imdecode(new MatOfByte(imgbytes), Imgcodecs.IMREAD_COLOR);
        return mat;
    }
}

