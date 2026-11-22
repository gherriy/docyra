package com.docyra.backend.scan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

class PageDetectorTest {

    private final PageDetector detector = new PageDetector();

    @Test
    void findsTiltedPageOnDarkBackground() {
        Mat photo = new Mat(800, 600, CvType.CV_8UC3, new Scalar(40, 40, 40));
        MatOfPoint sheet = new MatOfPoint(
                new Point(120, 90), new Point(500, 130), new Point(470, 700), new Point(90, 660));
        Imgproc.fillConvexPoly(photo, sheet, new Scalar(255, 255, 255));

        MatOfPoint2f corners = detector.findPageCorners(photo);

        assertThat(corners).isNotNull();
        assertThat(corners.total()).isEqualTo(4);
    }

    @Test
    void returnsNullWhenThereIsNoPage() {
        Mat photo = new Mat(800, 600, CvType.CV_8UC3, new Scalar(40, 40, 40));

        assertThat(detector.findPageCorners(photo)).isNull();
    }

    @Test
    void ordersCornersClockwiseFromTopLeft() {
        Point[] shuffled = {new Point(500, 130), new Point(90, 660), new Point(120, 90), new Point(470, 700)};

        Point[] ordered = PageDetector.orderCorners(shuffled);

        assertThat(ordered).containsExactly(
                new Point(120, 90), new Point(500, 130), new Point(470, 700), new Point(90, 660));
    }
}