package processing.pre;

import com.sun.istack.internal.NotNull;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import utils.ErrorHandling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by NestorKrdona 13/12/2017
 */
public class ImageManipulator
{
    public enum HistType
    {
        Rows,
        Columns
    }

    /**
     *
     * @param src
     * @return
     */
    public static Mat applyGaussianBlur(Mat src)
    {
        assert src != null : "Matriz NO Valida: Valor Nulo";

        Mat dest = new Mat();
        Size ksize = new Size(5,5);
        int sigmaX = 0;
        int sigmaY = 0;
        Imgproc.GaussianBlur(src, dest, ksize, sigmaX, sigmaY);
        return dest;
    }

    /**
     *
     * @param src
     * @return
     */
    public static Mat applyOtsuBinarysation(@NotNull Mat src)
    {
        assert !src.empty() : "Imagen NO Valida: Matrices Vacias";

        Mat dest = new Mat();
        int thresh = 0;
        int maxValue = 255;
        Imgproc.threshold(src, dest, thresh, maxValue, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        return dest;
    }

    /**
     * Implementado a partir de los ejemplos dados de OpenCV, no funciona en imágenes binarizadas
     * @param src
     * @return
     */
    public static Mat calculateHistogram(Mat src)
    {
        Mat tmp = new Mat();
        List<Mat> imgs = new LinkedList<>();
        imgs.add(src);
        int histSize = 256;
        Imgproc.calcHist(imgs, new MatOfInt(0), new Mat(), tmp, new MatOfInt(histSize), new MatOfFloat(0,256));
        int hist_w = 512, hist_h = 400;
        int bin_w = hist_w/histSize;

        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(255, 255, 255));
        Core.normalize(tmp, tmp, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        for (int i = 1; i < histSize; i++)
        {
            Imgproc.line(histImage,
                    new Point(bin_w * (i-1), hist_h - tmp.get(i-1, 0)[0]),
                    new Point(bin_w * (i), hist_h - tmp.get(i, 0)[0]),
                    new Scalar(0, 0, 0), 2, 8, 0);
        }
        return histImage;
    }

    public static @NotNull Mat loadGreyImage(@NotNull File img)
    {
        Mat m = null;
        try
        {
            m = Imgcodecs.imread(img.getCanonicalPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        }
        catch (IOException ioe)
        {
            ErrorHandling.log(Level.SEVERE, ioe.getMessage() );
            m = new Mat();
        }
        return m;
    }

    /**
     * Genera un histograma de píxeles negros contando las ocurrencias de columnas
     * @param src una imagen binarizada
     * @return un histograma de las columnas como una matriz de enteros
     */
    public static int[] manualCalculationHistogramColumns(Mat src)
    {
        int[] hist = new int[src.cols()];
        for (int row = 0; row < src.rows(); row++)
        {
            for (int col = 0; col < src.cols(); col++)
            {
                if (src.get(row, col)[0] == 0d) // is black
                {
                    hist[col] += 1;
                }
            }
        }
        return hist;
    }

    /**
     * Genera un histograma de píxeles negros que cuenta las repeticiones de filas
     * @param src una imagen binarizada
     * @return un histograma de las filas como una matriz de enteros
     */
    public static int[] manualCalculationHistogramRows(Mat src)
    {
        int[] hist = new int[src.rows()];
        for (int col = 0; col < src.cols(); col++)
        {
            for (int row = 0; row < src.rows(); row++)
            {
                if (src.get(row, col)[0] == 0d) // es Negro
                {
                    hist[row] += 1;
                }
            }
        }
        return hist;
    }

    public static Mat drawHistogram(int[] hist, HistType type)
    {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : hist)
        {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        switch (type)
        {
            case Rows:
                return drawHistogram(hist, hist.length, max, new Scalar(255d, 255d, 255d), new Scalar(0, 0, 0), type);
            case Columns:
                return drawHistogram(hist, max, hist.length, new Scalar(255d, 255d, 255d), new Scalar(0, 0, 0), type);
        }
        return null;
    }

    private static Mat drawHistogram(int[] hist, int width, int height, Scalar background, Scalar fill, HistType type)
    {
//        StringBuilder sb = new StringBuilder();
        Mat histogram = new Mat(width, height, CvType.CV_8UC3, background);
        for (int i = 0; i < hist.length; i++)
        {
            Point start = null, end = null;
            switch (type)
            {
                case Rows:
                    start = new Point(0, i);
                    end = new Point(hist[i], i);
                    break;
                case Columns:
                    start = new Point(i, 0);
                    end = new Point(i, hist[i]);
                    break;
            }
            Imgproc.line(histogram,
                    start,
                    end,
                    fill,
                    2, 8, 0);
//            sb.append(hist[i]);
//            if (hist.length -1 != i) sb.append(',');
        }
//        System.out.println(sb.toString());
        return histogram;
    }

    public static Image matToImage(Mat mat)
    {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public static void showMat(Pane root, Mat mat)
    {
        Image image = matToImage(mat);
        ImageView imv = new ImageView();
        imv.setImage(image);
//        imv.setFitWidth(300);
        imv.setPreserveRatio(true);
        imv.setSmooth(false);
        root.getChildren().add(imv);
    }
}
