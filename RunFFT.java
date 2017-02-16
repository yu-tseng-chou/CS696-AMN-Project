import java.awt.image.*;
import java.awt.Image;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Vector;
import java.util.Scanner;
import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

// Some ideas were drawn from reference http://introcs.cs.princeton.edu/java/97data/Complex.java.html
class Complex {
    double real;
    double imag;

    public Complex(double r, double i)
    {
        real = r;
        imag = i;
    }

    public double abs()
    {
        return Math.hypot(real, imag);
    }

    public Complex plus(Complex c)
    {
        return new Complex(this.real + c.real, this.imag + c.imag);
    }

    public Complex mimus(Complex c)
    {
        return new Complex(this.real - c.real, this.imag - c.imag);
    }

    public Complex times(Complex c) {
        return new Complex(this.real * c.real - this.imag * c.imag, this.real * c.imag + this.imag * c.real);
    }

    //public Complex conjugate()
    //{
    //    return new Complex(real, -imag);
    //}
}

public class RunFFT {

    public static int L = 256;

    // Ideas drawn from reference: http://introcs.cs.princeton.edu/java/97data/FFT.java.html
    public static Complex[] runFFT1D(Complex[] c, int direction)
    {
        int size = c.length;

        if (size == 1)
            return c;

        Complex[] even = new Complex[size/2];
        Complex[] odd = new Complex[size/2];

        for (int x = 0; x < size / 2; x++)
        {
            even[x] = c[2*x];
            odd[x] = c[2*x+1];
        }

        c = null;

        // Recursively pass in the even and odd groups to the same function
        // so that it does the bit reversal automatically
        even = runFFT1D(even, direction);
        odd = runFFT1D(odd, direction);

        Complex[] output = new Complex[size];

        // nF(u) = Fe(u) + Q^u*Fo(u) where Q = exp(-j*2*pi/n) -> Q^u = exp(-j*2*pi*u/n)
        for (int u = 0; u < size/2; u++)
        {
            Complex q = new Complex(Math.cos(-2*direction*Math.PI*u/size), -Math.sin(-2*direction*Math.PI*u/size));

            output[u] = even[u].plus(q.times(odd[u]));
            output[u + size/2] = even[u].mimus(q.times(odd[u]));
        }

        return output;
    }

    public static Complex[][][] runFFT2D(Complex[][][] c, int direction)
    {
        int band = c.length;
        int size = c[0][0].length;
        int power = 0;

        Complex[][][] output = new Complex[3][size][size];
        Complex[] input;

        for (int b = 0; b < band; b++) {
            // Transform each row
            input = new Complex[size];

            for (int j = 0; j < size; j++) {
                for (int i = 0; i < size; i++) {
                    input[i] = c[b][i][j].times(new Complex(Math.pow(-1 * direction, i + j), 0));
                    c[b][i][j] = null;
                }
                input = runFFT1D(input, direction);

                for (int i = 0; i < size; i++)
                    output[b][i][j] = input[i];
            }
            
            // Transform each column
            input = new Complex[size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++)
                    input[j] = output[b][i][j];

                input = runFFT1D(input, direction);

                for (int j = 0; j < size; j++) {
                    output[b][i][j] = input[j];
                    if (direction == 1)
                        output[b][i][j] = output[b][i][j].times(new Complex((double)1/size, 0));
                }

            }
        }
        c = null;
        return output;
    }

    public static BufferedImage getImageByFilename(String filename)
    {
        // Reference: http://stackoverflow.com/questions/10391778/create-a-bufferedimage-from-file-and-make-it-type-int-argb
        try {
            BufferedImage in = ImageIO.read(new File(filename));

            BufferedImage newImage = new BufferedImage(
                    in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();
            return newImage;

        } catch (IOException ioe) {
            //System.err.println(ioe);
            //System.exit(1);
        }
        return null;
    }

    //public static BufferedImage runFFT(BufferedImage image, int direction) {
    public static Complex[][][] getComplexMatrixFromImage(BufferedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int band = image.getSampleModel().getNumBands();

        int size = Math.max(width, height);
        int power = 0;

        while (Math.pow(2, power+1) <= size)
            power ++;

        size = (int)Math.pow(2, power);

        Raster src = image.getRaster();
        Complex[][][] output = new Complex[band][size][size];


        for (int b = 0; b < band; b++)
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    if (i < width && j < height)
                        output[b][i][j] = new Complex(src.getSample(i, j, b), 0);
                    else
                        output[b][i][j] = new Complex(0, 0);

        return output;
    }

    public static Complex[][][] applyButterworthMask(Complex[][][] c, int passtype, double r0, double p)
    {
        int band = c.length;
        int size = c[0][0].length;
        r0 = r0*size/2;

        Complex[][][] output = new Complex[band][size][size];
        double m, r, ratio;

        for(int b = 0; b < band; b++)
            for(int u = 0; u < size; u++)
                for(int v = 0; v < size; v++) {
                    r = Math.hypot(u-(size/2), v-(size/2));
                    if (passtype == 0) // "low"
                        ratio = r/r0;
                    else if (passtype == 1) // "high"
                        ratio = r0/r;
                    else
                        break;
                    m = 1/(1+Math.pow(ratio, 2*p));
                    output[b][u][v] = c[b][u][v].times(new Complex(m, 0));
                }

        return output;
    }

    public static Complex[][][] applyCircularMask(Complex[][][] c, int passtype, double r0)
    {
        int band = c.length;
        int size = c[0][0].length;
        r0 = r0*size/2;

        Complex[][][] output = new Complex[band][size][size];
        double r;

        for(int b = 0; b < band; b++)
            for(int u = 0; u < size; u++)
                for(int v = 0; v < size; v++) {
                    r = Math.hypot(u-(size/2), v-(size/2));

                    if ((passtype == 0 && r <= r0) || (passtype == 1 && r > r0))
                        output[b][u][v] = c[b][u][v];
                    else // ((passtype == 0 && r > r0) || (passtype == 1 && r <= r0))
                        output[b][u][v] = new Complex(0, 0);
                }

        return output;
    }
    
    public static Complex[][][] applyRingMask(Complex[][][] c, int passtype, double r0, double r1)
    {
        int band = c.length;
        int size = c[0][0].length;
        r0 = r0*size/2;
        r1 = r1*size/2;
        
        Complex[][][] output = new Complex[band][size][size];
        double r;
        
        for(int b = 0; b < band; b++)
            for(int u = 0; u < size; u++)
                for(int v = 0; v < size; v++) {
                    r = Math.hypot(u-(size/2), v-(size/2));
                    
                    if ((passtype == 0 && (r <= r0 || r>= r1)) || (passtype == 1 && (r > r0 || r < r1)))
                        output[b][u][v] = c[b][u][v];
                    else // ((passtype == 0 && r > r0) || (passtype == 1 && r <= r0))
                        output[b][u][v] = new Complex(0, 0);
                }
        
        return output;
    }

    public static BufferedImage generateImage(Complex[][][] c, int imageType)
    {
        int band = c.length;
        int size = c[0][0].length;

        BufferedImage destImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        WritableRaster dest = destImage.getRaster();

        for (int b = 0; b < band; b++) {
            double[][] m = new double[size][size];
            double max = 0;
            double min = Double.MAX_VALUE;

            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++) {
                    if (imageType == 1)
                        m[i][j] = c[b][i][j].abs();
                    else
                        m[i][j] = Math.log(c[b][i][j].abs() + 1);

                    //c[b][i][j] = null;

                    if (m[i][j] > max)
                        max = m[i][j];
                    if (m[i][j] < min)
                        min = m[i][j];
                }

            double C = (double)L/(max-min);
            //System.out.println("max = " + max + " min = " + min + " C = " + C);
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++) {
                    double value = C * (m[i][j] - min);
                    dest.setSample(i, j, b, value);
                }
        }
        try {
            if (imageType == 0) {
                ImageIO.write(destImage, "jpg", new File("fftImg.jpg"));
                System.out.println("Image file fftImg created.");
            }
            else if (imageType == -1) {
                ImageIO.write(destImage, "jpg", new File("fftFiltImg.jpg"));
                System.out.println("Image file fftFiltImg created.");
            }
        }
        catch (IOException ioe){}

        return destImage;
    }

    public static void main(String[] argv)
    {
        Scanner reader = new Scanner(System.in);
        System.out.println("\n***********************************************************");
        System.out.println("******************** Welcome to RunFFT ********************");
        System.out.println("***********************************************************");
        System.out.println("\nTo exit the program, please enter \"exit\" or press Ctrl+C.");// or enter the word \"exit\".");

        String s        = "";
        String filename = "";
        int state       = 0;
        double r0       = 0.3;
        double r1       = 0.7;
        int m           = 2;
        // 0 = start, 1 = filename, 2 = operation option, 3 = fft, 4 filter options
        // 5 = hc, 6 = lc, 7 = hb, 8 = lb, 9 = iftt, 10 = show spectrum, 11 = show image
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);


        BufferedImage image = null;
        BufferedImage spec = null;
        Complex[][][] c = null;
        Complex[][][] f = null;
        Complex[][][] i;

        do
        {
            switch(state)
            {
                case 0:
                {
                    frame.getContentPane().removeAll();
                    frame.repaint();
                    frame.setVisible(false);
                    System.out.println("\nPlease enter a filename:");
                    System.out.print("> ");
                    s = reader.nextLine();
                    state = 1;
                    break;
                }
                case 1:
                {
                    filename = s;
                    image = getImageByFilename(filename);
                    if (image == null)
                    {
                        System.out.println("\nFilename was invalid. Please enter another filename:");
                        System.out.print("> ");
                        s = reader.nextLine();
                        break;
                    }
                    frame.getContentPane().removeAll();
                    frame.getContentPane().setLayout(new GridBagLayout());
                    frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                    frame.repaint();
                    frame.setVisible(true);
                    frame.pack();
                    state = 2;
                    break;
                }
                case 2:
                {
                    System.out.println("\nPlease enter one of the following options:");
                    System.out.println("    \"fft\"        = run Fast Fourier Transform on the image.");
                    System.out.println("    \"startover\"  = start over.");
                    System.out.println("To exit the program, please enter \"exit\" or press Ctrl+C."); // or enter the word \"exit\".");
                    System.out.print("> ");
                    s = reader.nextLine();
                    if (s.equals("fft")) state = 3;
                    else if (s.equals("startover")) state = 0;
                    else if (!s.equals("exit")) System.out.println("**** Invalid Input ****");
                    break;
                }
                case 3: // FFT
                {
                    System.out.println("Generating FFT ...");
                    f = null;
                    c = null;
                    c = getComplexMatrixFromImage(image);
                    c = runFFT2D(c, 1);
                    image = generateImage(c, 0);
                    state = 12;
                    break;
                }
                case 4:
                {
                    System.out.println("\nPlease enter one of the following options:");
                    System.out.println("[r] = (OPTIONAL) cut-off radius rate between 0 and 1; default 0.3.");
                    System.out.println("[m] = (OPTIONAL) order m value between 1 to 10 for butterworth mask; default 2.\n");
                    System.out.println("    \"hc [r]\"    = apply high pass circular mask");
                    System.out.println("    \"lc [r]\"    = apply  low pass circular mask");
                    System.out.println("    \"hb [r] [m]\"= apply high pass butterworth mask");
                    System.out.println("    \"lb [r] [m]\"= applt  low pass butterworth mask");
                    System.out.println("    \"hr [r] [r]\"= apply high pass ring mask");
                    System.out.println("    \"lr [r] [r]\"= apply  low pass ring mask");
                    System.out.println("    \"fft \"      = see original FFT");
                    System.out.println("    \"ifft\"      = run IFFT on current image");
                    System.out.println("    \"startover\" = start over with a different image");
                    System.out.println("\n    (For example, \"lb 0.5 2\".)");
                    System.out.println("\nTo exit the program, please enter \"exit\" or press Ctrl+C."); // or enter the word \"exit\".");
                    System.out.print("> ");
                    s = reader.nextLine();
                    String[] ss = s.split(" ");
                    if (ss.length == 0)
                    {
                        System.out.println("**** Invalid Input ****");
                        break;
                    }
                    s = ss[0];
                    if (ss.length > 1)
                    {
                        try { r0 = Math.min(Math.max(Double.parseDouble(ss[1]), 0), 1); }
                        catch(Exception e){ r0 = 0.3; }
                    }
                    else r0 = 0.3;
                    
                    if (ss.length > 2)
                    {
                        if (s.equals("hb") || s.equals("lb"))
                        {
                            try { m = Math.min(Math.max(Integer.parseInt(ss[2]), 1), 10); }
                            catch(Exception e){ m = 2; }
                        }
                        else if (s.equals("hr") || s.equals("lr"))
                        {
                            try { r1 = Math.min(Math.max(Double.parseDouble(ss[2]), 0), 1); }
                            catch(Exception e){ r1 = 0.7; }
                        }
                    }
                    else {m = 2; r1 = 0.7;}

                    // 0 = start, 1 = filename, 2 = operation option, 3 = fft, 4 filter options
                    // 5 = hc, 6 = lc, 7 = hb, 8 = lb, 9 = iftt, 10 = show image
                    if (s.equals("startover")) state = 0;
                    else if (s.equals("fft"))
                    {
                        f = null;
                        image = generateImage(c, -1);
                        state = 12;
                    }
                    else if (s.equals("hc")) state = 5;
                    else if (s.equals("lc")) state = 6;
                    else if (s.equals("hb")) state = 7;
                    else if (s.equals("lb")) state = 8;
                    else if (s.equals("hr")) state = 9;
                    else if (s.equals("lr")) state = 10;
                    else if (s.equals("ifft")) state = 11;
                    else if (!s.equals("exit")) System.out.println("**** Invalid Input ****");
                    break;
                }
                case 5: // High pass circular mask
                {
                    System.out.println("Applying high pass circular mask with cut-off radius ratio = " + r0 + ".");
                    f = applyCircularMask(c, 1, r0);
                    //c = applyCircularMask(c, 1, r0);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 6: // Low pass circular mask
                {
                    System.out.println("Applying low pass circular mask with cut-off radius ratio = " + r0 + ".");
                    f = applyCircularMask(c, 0, r0);
                    //c = applyCircularMask(c, 0, r0);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 7: // High pass butterworth mask
                {
                    System.out.println("Applying high pass butterworth mask with cut-off radius ratio = " + r0 + " and m = " + m + ".");
                    f = applyButterworthMask(c, 1, r0, m);
                    //c = applyButterworthMask(c, 1, r0, m);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 8: // Low pass butterworth mask
                {
                    System.out.println("Applying low pass butterworth mask with cut-off radius ratio = " + r0 + " and m = " + m + ".");
                    f = applyButterworthMask(c, 0, r0, m);
                    //c = applyButterworthMask(c, 0, r0, m);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 9: // High pass ring mask
                {
                    System.out.println("Applying high pass ring mask with cut-off radius ratio r0 = " + r0 + " and r1 = " + m + ".");
                    f = applyRingMask(c, 1, r0, r1);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 10: // Low pass ring mask
                {
                    System.out.println("Applying low pass ring mask with cut-off radius ratio r0 = " + r0 + " and r1 = " + m + ".");
                    f = applyRingMask(c, 0, r0, r1);
                    image = generateImage(f, -1);
                    state = 12;
                    break;
                }
                case 11: // IFFT
                {
                    System.out.println("Generating IFFT ...");
                    if (f == null)
                        i = runFFT2D(c, -1);
                    else
                        i = runFFT2D(f, -1);
                    image = generateImage(i, 1);
                    state = 12;
                    break;
                }
                case 12: {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    frame.getContentPane().removeAll();
                    frame.getContentPane().setLayout(new GridBagLayout());
                    if (width < 800)
                        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                    else
                        frame.getContentPane().add(new JLabel(new ImageIcon(image.getScaledInstance(800, 800, Image.SCALE_SMOOTH))));
                    frame.repaint();
                    frame.pack();
                    frame.setVisible(true);
                    state = 4;
                    System.out.println("Completed.");
                    break;
                }
                default:
                    break;
            }
        } while (!s.equals("exit"));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.dispose();
        return;
    }


}
