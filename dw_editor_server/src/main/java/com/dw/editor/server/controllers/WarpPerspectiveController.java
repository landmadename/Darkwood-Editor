package com.dw.editor.server.controllers;

import com.dw.editor.server.objects.MarkedImageDTO;
import com.dw.editor.server.services.WarpPerspectiveService;
import com.dw.editor.server.utils.MatTools;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;



@RestController
public class WarpPerspectiveController {
    @Autowired
    public WarpPerspectiveService warpPerspectiveService;

    @PostMapping(value = "/warp_perspective")
    public String warpPerspectiveController(@RequestBody MarkedImageDTO markedImageDTO){
        return warpPerspectiveService.warpPerspective(markedImageDTO);
    }
}
