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
        @Test
    void scanEffectTurnsYellowPaperWhiteAndKeepsTextDark() {
        Mat page = new Mat(400, 300, CvType.CV_8UC3, new Scalar(110, 170, 190));
        Imgproc.line(page, new Point(50, 200), new Point(250, 200), new Scalar(20, 20, 20), 4);

        Mat result = new PageDetector().applyScanEffect(page);

        assertThat(result.get(50, 50)[0]).isGreaterThan(240);
        assertThat(result.get(200, 150)[0]).isLessThan(80);
    }
}