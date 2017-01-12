package cop5618;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;

public class FJBufferedImage extends BufferedImage {
	
   /**Constructors*/
    static ForkJoinPool pool=ForkJoinPool.commonPool(); //Creating the forkjoin pool
    
    private class GETRGB extends RecursiveAction            //Recursive Action class for getting RGB pixels parallely
    {
            
              int mstartx;
              int mstarty;
              int mwidth;
              int mheight;
              int[] rgbarray;
              int moffset;
              int mscansize;
                        
        public GETRGB(int startx,int starty,int width, int height,int[] rgbarray,int offset, int scansize)
        {            
           mstartx=startx;
           mstarty=starty;
           mwidth=width; 
           mheight=height; 
           this.rgbarray=rgbarray;
           moffset=offset;
           mscansize=scansize;           
            
        }
        
        protected  int sThreshold = 10000;              //setting threshold to 10000 pixels since parallelizing is most optimum when NQ>10000
       
        @Override
        protected void compute()
        {            
            if((mwidth*mheight)>sThreshold)
            
            { 
               GETRGB ob1=(new GETRGB(mstartx,mstarty,mwidth,mheight/2,rgbarray,moffset,mscansize));
                ob1.fork();                
               GETRGB ob2=(new GETRGB(mstartx,mstarty+(mheight/2),mwidth,(mheight-(mheight/2)),rgbarray,(moffset+((mheight/2)*mscansize)),mscansize));
                ob2.compute();
                ob1.join();
                               
            }
            else
            {   FJBufferedImage.super.getRGB(mstartx, mstarty, mwidth, mheight, rgbarray, moffset, mscansize);
            
            }
          }
             
    }
         private class SETRGB extends RecursiveAction         //Recursive Action class for setting RGB pixels parallely
    {
              int mstartx;
              int mstarty;
              int mwidth;
              int mheight;
              int[] rgbarray;
              int moffset;
              int mscansize;
          
             
        public SETRGB(int startx,int starty,int width, int height,int[] rgbarray,int offset, int scansize)
        {            
           mstartx=startx;
           mstarty=starty;
           mwidth=width; 
           mheight=height; 
           this.rgbarray=rgbarray;
           moffset=offset;
           mscansize=scansize;
                                            
        }
        
        protected  int sThreshold = 10000;
      
        @Override
        protected void compute()
        {
            
            if((mwidth*mheight)>sThreshold)
            
            { 
               SETRGB ob1=(new SETRGB(mstartx,mstarty,mwidth,mheight/2,rgbarray,moffset,mscansize));
                ob1.fork();
                
                SETRGB ob2=(new SETRGB(mstartx,mstarty+(mheight/2),mwidth,(mheight-(mheight/2)),rgbarray,(moffset+((mheight/2)*mscansize)),mscansize));
                ob2.compute();
                ob1.join();

            }
            else
            {  
                FJBufferedImage.super.setRGB(mstartx, mstarty, mwidth, mheight, rgbarray, moffset, mscansize);
            }
          }
             
    }
        
        
	
	public FJBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
			Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
	}
	

	/**
	 * Creates a new FJBufferedImage with the same fields as source.
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
	       Hashtable<String,Object> properties=null; 
	       String[] propertyNames = source.getPropertyNames();
	       if (propertyNames != null) {
	    	   properties = new Hashtable<String,Object>(); 
	    	   for (String name: propertyNames){properties.put(name, source.getProperty(name));}
	    	   }
	 	   return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);	
                   
                   
	}
                
      
	@Override
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
        /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
                pool.invoke(new SETRGB(xStart, yStart, w, h, rgbArray, offset, scansize));
	}
	

	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
	       /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
               //System.out.println("Hello");
                pool.invoke(new GETRGB(xStart, yStart, w, h, rgbArray, offset, scansize));
        
	return null;
	}
	
}


// System.out.println(mlength);
//                subtasks = new ArrayList<GETRGB>();
//                subtasks.addAll(createSubtasks());
//                for(RecursiveAction subtask : subtasks)
//                {
//                    subtask.fork();// System.out.println("boo");
//                }