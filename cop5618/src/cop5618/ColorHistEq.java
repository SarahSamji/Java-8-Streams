package cop5618;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.Color;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
//import java.util.Set;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
//import java.util.stream.DoubleStream;
//mport java.lang.Number;
//import java.util.Iterator;
//import java.util.stream.Stream;

public class ColorHistEq {

	static String[] labels = { "getRGB", "convert to HSB and create brightness map", "parallel prefix",
			"probability array", "equalize pixels", "setRGB" };

	static ColorHistEq histobj = new ColorHistEq();            

	class HSBobj                                //Class to encapsulate float HSB arrays returned by RGBtoHSB.
        {
		float h;
		float s;
		float b;

		public HSBobj(float[] hsbArray) {
			h = hsbArray[0];
			s = hsbArray[1];
			b = hsbArray[2];                      
		}
		
	};

        
	static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
		Timer time = new Timer(labels);
		ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();
		time.now();
                float[] hsbArray = new float[] { 0, 0, 0 };
		int[] sourcePixelArray = new int[w * h];
                sourcePixelArray=  image.getRGB(0, 0, w, h,sourcePixelArray , 0, w);
		time.now(); // getRGB
                

		Map<Integer,Long> bincountMap = Arrays.stream(sourcePixelArray) 	// Convert RGBpixels into HSB values 						// IntStream
						.mapToObj(pixel -> {					
                                                Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel),colorModel.getBlue(pixel), hsbArray);
                                                return histobj.new HSBobj(hsbArray);        //Constructore of HSBobj invoked
                                                })                                          
                                                .collect( Collectors.groupingBy(hsb ->((int)(hsb.b*256)),Collectors.counting())); // Grouping the brightness values by bins and creating a mapping: bin number->count
                 
                 int[] countArray=new int[256];                     //Countarray to store count values for each bin
                 countArray=IntStream.range(0,256)
                          .map(key->bincountMap.containsKey(key)?bincountMap.get(key).intValue():0)         //if there is no mapping for a particular bin then put zero
                          .toArray();
                time.now(); // convert to HSB and create brightness map

                
		// calculate prefix sum
		Arrays.parallelPrefix(countArray, (x, y) -> x + y);
		time.now(); // parallel prefix	
                
                // Store the cumalative probability into CParray
		double noOfPixels = w * h;                       
		final double[] CParray = Arrays.stream(countArray).mapToDouble(x-> ((double)x / noOfPixels)).toArray();                
//               for(int i=0;i<256;i++)
//                    System.out.println(CParray[i]);                   
		time.now(); // cumulative probability array

		int[] outputPixelArray = Arrays.stream(sourcePixelArray)                //Creating new RGB array with modified brightness values
                                            .mapToObj(pixel -> {
                                                   Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel), colorModel.getBlue(pixel),hsbArray);
                                                    return histobj.new HSBobj(hsbArray);
                                                    })
                                            .map(hsb -> {                   
                                                     int x=(int)(hsb.b*256);
                                                     hsb.b= (float) CParray[Math.min(x,255)];    //Min value for the edge case where last bin is encountered.
                                                     return hsb;
                                                    })
                                            .mapToInt(hsb -> Color.HSBtoRGB(hsb.h, hsb.s, hsb.b))
                                            .toArray();               
		time.now(); // equalize pixels
                
                
		newImage.setRGB(0, 0, w, h, outputPixelArray, 0, w);
		time.now(); // setRGB
                
		return time;
	}

	static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {
		Timer time = new Timer(labels);
		/**
		 * IMPLEMENT PARALLEL METHOD
		 */
		ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();
		time.now();         // getRGB
                
                float[] hsbarray = new float[] { 0, 0, 0 };
		int[] sourcePixelArray = new int[w * h];
                image.getRGB(0, 0, w, h,sourcePixelArray , 0, w);                
		time.now(); // getRGB

		Map<Object, Long> bincountMap = Arrays.stream(sourcePixelArray).parallel()      // Convert RGBpixels into HSB values 	
                                                      .mapToObj(pixel -> {
                                                      Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel),colorModel.getBlue(pixel), hsbarray);
                                                      return histobj.new HSBobj(hsbarray);  //Constructore of HSBobj invoked
                                                       })
                                        	      .collect(Collectors.groupingBy(hsb -> (int)(hsb.b * 256), Collectors.counting()));        //Min value for the edge case where last bin is encountered.
		            

		
               
                int[] countArray=new int[256];                     //Countarray to store count values for each bin
                 countArray=IntStream.range(0,256).parallel()
                          .map(key->bincountMap.containsKey(key)?bincountMap.get(key).intValue():0)         //if there is no mapping for a particular bin then put zero
                          .toArray();
                 time.now();            // convert to HSB and create brightness map
                
		
		Arrays.parallelPrefix(countArray, (x, y) -> x + y);     // calculate prefix sum of countarray
		time.now(); // parallel prefix

                double noOfPixels = w * h;
		final double[] CParray = Arrays.stream(countArray).parallel().mapToDouble(x -> ((double)x / noOfPixels)).toArray();
		time.now(); // cumulative probability array
                
		int[] outputPixelArray = Arrays.stream(sourcePixelArray).parallel().mapToObj(pixel -> {
                                                   Color.RGBtoHSB(colorModel.getRed(pixel),colorModel.getGreen(pixel), colorModel.getBlue(pixel),hsbarray);
                                                   return histobj.new HSBobj(hsbarray);
                                                    })
                                                  .mapToInt(hsb -> Color.HSBtoRGB(hsb.h, hsb.s,(float) CParray[Math.min((int) (hsb.b * 256), 255)])).toArray();

		time.now(); // equalize pixels

		newImage.setRGB(0, 0, w, h, outputPixelArray, 0, w);
                

		time.now(); // setRGB
		return time;
	}

}

                //((int)Math.ceil(hsb.b/(1.0/256.0))-1),Collectors.counting()));
//for (Map.Entry<Integer,Long> entry : binsMap.entrySet()) {
//    Object key = entry.getKey();
//    Long value = entry.getValue();
//    System.out.println(key+ " "+ value);
//}

//                 for(int i=0;i<256;i++)
//                    System.out.println(binsArray[i]);   
                 
//                        //  .collect(Collectors.groupingBy(hsb->(((int)Math.ceil(hsb.b/(1.0/256.0)))-1),Collectors.counting()));
//                double[] binsArray=IntStream.range(0,256)
//                                    .map(key->binsMap.containsKey(key)?(binsMap.get(key).intValue()):0)
//                                 .toArray();   
		 //count of pixels in each histogram bin
               //double[] binsArray =
//                        binsMap.entrySet().stream().map(Map.Entry::getValue).mapToDouble(hsbList -> hsbList.size())
//                        .forEach(hsb->
//                         binsMap.containsKey(((int)Math.ceil(hsb.b/(1.0/256.0)))-1)?(binsMap.get(((int)Math.ceil(hsb.b/binwidth))-1).intValue()):0
//                        //binsArray[((int)Math.ceil(hsb.b/binwidth))-1]=hsb;
//                        });
//				//.toArray();
//                System.out.println(binsArray.length);
