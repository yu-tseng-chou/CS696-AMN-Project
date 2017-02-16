import java.awt.image.*;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Vector;
import javax.swing.*;
import com.pearsoneduc.ip.io.*;
import com.pearsoneduc.ip.gui.*;
import com.pearsoneduc.ip.op.OperationException;



public class EdgeDetector extends ImageSelector {

    public static int L = 256;

    public EdgeDetector(String imageFile)
            throws IOException, ImageDecoderException, OperationException {
        super(imageFile);
    }


    // Checks that the image is suitable for simulation

    public boolean imageOK() {

        // Must be RGB...

        //if (getSourceImage().getType() != BufferedImage.TYPE_BYTE_GRAY)
        //    return false;
        return true;
    }

    public BufferedImage getMaskedImage(BufferedImage image, float [] coeff) {
        if (image == null)
            return null;


        int coeff_size = coeff.length;
        int mask_size = (int)Math.sqrt(coeff_size);
        int width = image.getWidth();
        int height = image.getHeight();
        int band = image.getSampleModel().getNumBands();
        int dest_width = width - mask_size + 1;
        int dest_height = height - mask_size + 1;

        Kernel kernel = new Kernel(mask_size, mask_size, coeff);
        ConvolveOp op = new ConvolveOp(kernel);

        return op.filter(image, null).getSubimage((mask_size/2)-1, (mask_size/2)-1, width - mask_size + 1, height - mask_size + 1);
/*
        Raster src = image.getRaster();
        BufferedImage destImage = new BufferedImage(dest_width, dest_height, image.getType()); //, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster dest = destImage.getRaster();

        for(int x = 0; x < dest_width; x++)
            for(int y = 0; y < dest_height; y++)
                for(int b = 0; b < band; b++) {
                    int f1 = src.getSample(x, y, b);
                    int f2 = src.getSample(x + 1, y, b);
                    int f3 = src.getSample(x + 2, y, b);
                    int f4 = src.getSample(x, y + 1, b);
                    int f5 = src.getSample(x + 1, y + 1, b);
                    int f6 = src.getSample(x + 2, y + 1, b);
                    int f7 = src.getSample(x, y + 2, b);
                    int f8 = src.getSample(x + 1, y + 2, b);
                    int f9 = src.getSample(x + 2, y + 2, b);
                    dest.setSample(x, y, b, f1 * coeff[0] + f2 * coeff[1] + f3 * coeff[2] + f4 * coeff[3] +
                            f5 * coeff[4] + f6 * coeff[5] + f7 * coeff[6] + f8 * coeff[7] + f9 * coeff[8]);
                }

        return destImage;
*/
    }

    public BufferedImage getAverageMaskedImage(BufferedImage image, float [] coeff) {
        if (image == null)
            return null;

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage image_R = new BufferedImage(width, height, image.getType());
        BufferedImage image_G = new BufferedImage(width, height, image.getType());
        BufferedImage image_B = new BufferedImage(width, height, image.getType());

        Raster src = image.getRaster();

        WritableRaster dest_R = image_R.getRaster();
        WritableRaster dest_G = image_G.getRaster();
        WritableRaster dest_B = image_B.getRaster();

        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++) {
                dest_R.setSample(x, y, 0, src.getSample(x, y, 0));
                dest_G.setSample(x, y, 1, src.getSample(x, y, 1));
                dest_B.setSample(x, y, 2, src.getSample(x, y, 2));
            }

        BufferedImage mask_R = getMaskedImage(image_R, coeff);
        BufferedImage mask_G = getMaskedImage(image_G, coeff);
        BufferedImage mask_B = getMaskedImage(image_B, coeff);

        int mask_width = mask_R.getWidth();
        int mask_height = mask_R.getHeight();

        Raster src_R = mask_R.getRaster();
        Raster src_G = mask_G.getRaster();
        Raster src_B = mask_B.getRaster();

        BufferedImage destImage = new BufferedImage(mask_width, mask_height, image.getType()); //BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster dest = destImage.getRaster();

        for(int x = 0; x < mask_width; x++)
            for(int y = 0; y < mask_height; y++) {
                // dest.setSample(x, y, 0, (src_R.getSample(x, y, 0) + src_G.getSample(x, y, 0) + src_B.getSample(x, y, 0))/3);
                dest.setSample(x, y, 0, src_R.getSample(x, y, 0));
                dest.setSample(x, y, 1, src_G.getSample(x, y, 1));
                dest.setSample(x, y, 2, src_B.getSample(x, y, 2));
            }

        return destImage;
    }

    public BufferedImage getGradientMagnitudeImage(BufferedImage image_x, BufferedImage image_y)
    {
        if (image_x == null)
            return null;

        int width = image_x.getWidth();
        int height = image_x.getHeight();
        int band = image_x.getSampleModel().getNumBands();

        Raster src_x = image_x.getRaster();
        Raster src_y = image_y.getRaster();
        BufferedImage destImage = new BufferedImage(width, height, image_x.getType()); //BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster dest = destImage.getRaster();

        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                for(int b = 0; b < band; b++)
                    dest.setSample(x, y, b, Math.sqrt(Math.pow(src_x.getSample(x, y, b), 2) + Math.pow(src_y.getSample(x, y, b), 2)));

        return destImage;
    }

    public BufferedImage getPhaseImage(BufferedImage image_x, BufferedImage image_y)
    {
        if (image_x == null)
            return null;

        int width = image_x.getWidth();
        int height = image_x.getHeight();
        int band = image_x.getSampleModel().getNumBands();

        Raster src_x = image_x.getRaster();
        Raster src_y = image_y.getRaster();
        BufferedImage destImage = new BufferedImage(width, height, image_x.getType()); //BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster dest = destImage.getRaster();

        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                for(int b = 0; b < band; b++)
                    if (src_x.getSample(x, y, b) == 0)
                        dest.setSample(x, y, b, L-1);
                    else
                        dest.setSample(x, y, b, (Math.atan(src_y.getSample(x, y, b)/src_x.getSample(x, y, b)) + L)%L);

        return destImage;
    }

    // Creates simulated views of an image

    public Vector generateImages() {
        Vector resolutions = new Vector();

        String key = "Original Image";
        resolutions.addElement(key);
        addImage(key, new ImageIcon(this.getSourceImage()));




        float[] coeff_x = new float[] {-1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f};
        float[] coeff_y = new float[] {-1.0f, -2.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f};

        BufferedImage gradXImage = getAverageMaskedImage(this.getSourceImage(), coeff_x);
        key = "X Dir Gradient Image";
        resolutions.addElement(key);
        addImage(key, new ImageIcon(gradXImage));

        BufferedImage gradYImage = getAverageMaskedImage(this.getSourceImage(), coeff_y);
        key = "Y Dir Gradient Image";
        resolutions.addElement(key);
        addImage(key, new ImageIcon(gradYImage));

        key = "Gradient Magnitude Image";
        resolutions.addElement(key);
        addImage(key, new ImageIcon(getGradientMagnitudeImage(gradXImage, gradYImage)));

        key = "Phase Image";
        resolutions.addElement(key);
        addImage(key, new ImageIcon(getPhaseImage(gradXImage, gradYImage)));
        return resolutions;

    }


    public static void main(String[] argv) {
        if (argv.length > 0) {
            try {
                JFrame frame = new EdgeDetector(argv[0]);
                frame.pack();
                frame.setVisible(true);
            }
            catch (Exception e) {
                System.err.println(e);
                System.exit(1);
            }
        }
        else {
            System.err.println("usage: java EdgeDetector <imagefile>");
            System.exit(1);
        }
    }


}
