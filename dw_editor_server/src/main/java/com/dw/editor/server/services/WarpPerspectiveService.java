package com.dw.editor.server.services;

import com.dw.editor.server.objects.MarkedImageDTO;
import com.dw.editor.server.utils.MatTools;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

@Service
public class WarpPerspectiveService {
    public String warpPerspective(MarkedImageDTO markedImageDTO){
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        Mat img = MatTools.base64ToMat(markedImageDTO.chosenImg);
        HashMap<String, Point> rawPoints = locatePoints(markedImageDTO.points);
        HashMap<String, Point> straightPoints = calcStraightPoints(rawPoints);

        List<Point> listSrcs=java.util.Arrays.asList(
                rawPoints.get("left"), rawPoints.get("cornerInner"), rawPoints.get("right"), rawPoints.get("cornerOuter"));
        Mat srcPoints= Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);
        List<Point> listDsts=java.util.Arrays.asList(
                straightPoints.get("left"), straightPoints.get("cornerInner"), straightPoints.get("right"), straightPoints.get("cornerOuter"));
        Mat dstPoints= Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);
        Mat perspectiveMat=Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(img, img, perspectiveMat, img.size());
        img = clearUselessPart(straightPoints, img);
        imwrite("C:\\Users\\LMN\\Downloads\\out.png", img);
        return "data:image/jpeg;base64,"+MatToBase64(img);
    }

    public Mat clearUselessPart(HashMap<String, Point> points, Mat img){
        Imgproc.cvtColor(img, img,Imgproc.COLOR_RGB2RGBA);
        Imgproc.rectangle (img, new Point(0, 0), points.get("cornerInner"), new Scalar(0, 0, 255, 0), -1);
        Imgproc.rectangle (img, new Point(points.get("cornerOuter").x, 0), new Point(img.width(), img.height()), new Scalar(0, 0, 255, 0), -1);
        Imgproc.rectangle (img, new Point(0, points.get("cornerOuter").y), new Point(img.width(), img.height()), new Scalar(0, 0, 255, 0), -1);
        return img;
    }

    public String MatToBase64(Mat capImg){

        String jpg_base64 = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(mat2BI(capImg), "png", baos);
        }catch (IOException ex){
            System.out.println(ex);
        }
        byte[] bytes = baos.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        jpg_base64 = encoder.encodeBuffer(Objects.requireNonNull(bytes));
        return jpg_base64;
    }

    public BufferedImage mat2BI(Mat mat) {
        int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
        byte[] data = new byte[dataSize];
        mat.get(0, 0, data);
        int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY :
                BufferedImage.TYPE_4BYTE_ABGR;

        if (type == BufferedImage.TYPE_4BYTE_ABGR) {
            for (int i = 0; i < dataSize; i += 4) {
                byte blue = data[i + 0];
                data[i + 0] = data[i + 2];
                data[i + 2] = blue;
            }
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }

    public HashMap<String, Point> calcStraightPoints(HashMap<String, Point> rawPoints){
        HashMap<String, Point> straightPoints = new HashMap<>();
        straightPoints.put("cornerOuter",   rawPoints.get("cornerOuter"));
        straightPoints.put("left",          new Point(rawPoints.get("left").x,  rawPoints.get("cornerOuter").y));
        straightPoints.put("right",         new Point(rawPoints.get("cornerOuter").x, rawPoints.get("right").y));
        double offset = calcOffset(rawPoints.get("cornerOuter"), rawPoints.get("cornerInner"));
        straightPoints.put("cornerInner",   new Point(rawPoints.get("cornerOuter").x-offset, rawPoints.get("cornerOuter").y-offset));
        return straightPoints;
    }

    public double calcOffset(Point A, Point B){
        double distance = Math.sqrt(Math.pow(A.x-B.x,2)+Math.pow(A.y-B.y,2));
        return distance/Math.sqrt(2);
    }

    public HashMap<String, Point> locatePoints(ArrayList<ArrayList<Float>> points){
        HashMap<String, Point> rawPoints = new HashMap<>();

//        通过判断Y坐标找出右上角和靠内的角点
        points.sort((o1, o2) -> (int)(o1.get(1)-o2.get(1)));
        rawPoints.put("right",          new Point(points.get(0).get(0), points.get(0).get(1)));
        rawPoints.put("cornerInner",    new Point(points.get(1).get(0), points.get(1).get(1)));

//        通过判断X坐标找出左下角和靠外的角点
        points.remove(0);
        points.remove(0);

        points.sort((o1, o2) -> (int)(o1.get(0)-o2.get(0)));
        rawPoints.put("left",          new Point(points.get(0).get(0), points.get(0).get(1)));
        rawPoints.put("cornerOuter",   new Point(points.get(1).get(0), points.get(1).get(1)));

        return rawPoints;
    }
}
