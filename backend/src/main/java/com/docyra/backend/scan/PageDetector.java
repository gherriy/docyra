package com.docyra.backend.scan;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import nu.pattern.OpenCV;

@Component
public class PageDetector {

    private static final double MIN_AREA_RATIO = 0.20;

    static {
        OpenCV.loadLocally();
    }

    public byte[] extractPage(Path imagePath) {
        Mat image = Imgcodecs.imread(imagePath.toString());
        if (image.empty()) {
            throw new PageDetectionException("Image illisible");
        }

        MatOfPoint2f corners = findPageCorners(image);
        Mat page = corners == null ? image : straighten(image, corners);
                Mat scanned = applyScanEffect(page);


        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", scanned, buffer);
        return buffer.toArray();
    }

    MatOfPoint2f findPageCorners(Mat image) {
        // 1. Niveaux de gris + flou gaussien
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

        // 2. Bords (Canny) + dilatation pour refermer les petits trous
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 75, 200);
        Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        // 3. Plus grand contour à 4 coins, d'au moins 20 % de l'image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double minArea = image.width() * image.height() * MIN_AREA_RATIO;
        MatOfPoint2f best = null;
        double bestArea = 0;

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < minArea || area <= bestArea) {
                continue;
            }
            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(curve, approx, 0.02 * Imgproc.arcLength(curve, true), true);

            if (approx.total() == 4 && Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
                best = approx;
                bestArea = area;
            }
        }
        return best;
    }

    // 4a. Remise en ordre : haut-gauche, haut-droite, bas-droite, bas-gauche
    static Point[] orderCorners(Point[] points) {
        Comparator<Point> bySum = Comparator.comparingDouble(p -> p.x + p.y);
        Comparator<Point> byDiff = Comparator.comparingDouble(p -> p.y - p.x);
        Point topLeft = Arrays.stream(points).min(bySum).orElseThrow();
        Point bottomRight = Arrays.stream(points).max(bySum).orElseThrow();
        Point topRight = Arrays.stream(points).min(byDiff).orElseThrow();
        Point bottomLeft = Arrays.stream(points).max(byDiff).orElseThrow();
        return new Point[] {topLeft, topRight, bottomRight, bottomLeft};
    }

    // 4b. Redressement avec la perspective
    private Mat straighten(Mat image, MatOfPoint2f corners) {
        Point[] p = orderCorners(corners.toArray());
        double width = Math.max(distance(p[2], p[3]), distance(p[1], p[0]));
        double height = Math.max(distance(p[1], p[2]), distance(p[0], p[3]));

        MatOfPoint2f source = new MatOfPoint2f(p);
        MatOfPoint2f target = new MatOfPoint2f(
                new Point(0, 0), new Point(width - 1, 0),
                new Point(width - 1, height - 1), new Point(0, height - 1));

        Mat matrix = Imgproc.getPerspectiveTransform(source, target);
        Mat result = new Mat();
        Imgproc.warpPerspective(image, result, matrix, new Size(width, height));
        return result;
    }
    Mat applyScanEffect(Mat page) {
        Mat gray = new Mat();
        Imgproc.cvtColor(page, gray, Imgproc.COLOR_BGR2GRAY);

        Mat background = new Mat();
        Imgproc.dilate(gray, background, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
        Imgproc.medianBlur(background, background, 21);

        Mat flat = new Mat();
        Core.divide(gray, background, flat, 255);

        Mat result = new Mat();
        Core.normalize(flat, result, 0, 255, Core.NORM_MINMAX);
        result.convertTo(result, -1, 1.2, -30);
        return result;
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }
}