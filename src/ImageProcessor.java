/*
* Author: Andy Fang
* Date Created: 01/26/09
* Description: This class processes an image. It is able to locate an object within the image and perform
*              a variety of functions on it. This includes centering the object in the image frame, detecting
*              the edges of the object (up, down, left, right), and scaling images.
*/

public class ImageProcessor
{
    public static final int BLANK = 0;
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 0;
    public static final int EAST = 1;
    public static final int NUM_EDGES = 4;
    public static final int DEFAULT_WIDTH = 80;
    public static final int DEFAULT_HEIGHT = 80;
    
    /*
    * Special condition: Picture must contain an image
    * Parameters: A 2D int array
    * Function: Returns the center of mass of the circle found
    */
    public Location centerOfMass(int[][] pic)
    {
        int xCOM = 0;
        int yCOM = 0;
        int numPts = 0;
        
        for(int i = 0; i < pic.length; i++)
        {
            for(int j = 0; j < pic[0].length; j++)
            {
                xCOM += i * pic[i][j];
                yCOM += j * pic[i][j];
                
                if(pic[i][j] != BLANK)
                {
                    numPts++;
                }
            }
        }
        
        return new Location(xCOM/numPts, yCOM/numPts);
    } //private Location centerOfMass()
    
    /*
    * Special condition: Picture must not be empty
    * Parameters: A 2D int array and a Location vector
    * Function: Returns an image with the circle shifted by the given vector
    */
    public int[][] shiftImage(int[][] pic, Location shift)
    {
        int[][] output = new int[pic.length][pic[0].length];
        
        for(int i = 0; i < pic.length; i++)
        {
            for(int j = 0; j < pic[0].length; j++)
            {
                if(pic[i][j] != BLANK)
                {
                    if(isInbounds(pic, new Location(i + shift.getX(), j + shift.getY())))
                    {
                        output[i + shift.getX()][j + shift.getY()] = pic[i][j];
                    }
                }
            }
        }
        
        return output;
    } //public int[][] shiftImage(int[][] pic, Location shift)
    
    /*
     * Special condition: Picture must contain an image
     * Parameters: A 2D int array
     * Function: Returns an image with the circle shifted to the center
     */
     public int[][] shiftImage(int[][] pic)
     {
         Location centerOfPic = new Location((pic.length - 1)/2, (pic[0].length - 1)/2);
         Location centerShiftVector = new Location(centerOfPic.getX() - centerOfMass(pic).getX(),
                 centerOfPic.getY() - centerOfMass(pic).getY());
         
         return shiftImage(pic, centerShiftVector);
     }
    
    /*
    * Parameters: A 2D int array and a Location object
    * Function: Checks to see if the location is within the boundaries of the picture
    */
    private boolean isInbounds(int[][] pic, Location loc)
    {
        if((loc.getX() < pic.length) &&
                (loc.getX() >= 0) &&
                (loc.getY() < pic[0].length) &&
                (loc.getY() >= 0))
        {
            return true;
        }
        else
        {
            return false;
        }
    } //private boolean isInbounds(int[][] pic, Location loc)
    
    /*
    * Special condition: Assumes image is not empty, numPix and threshold must be > 0
    * Parameters: n/a
    * Function: Returns a 2x2 2D Location array of the corner edges of the image in the given picture,
    *               counting an edge as the first row/column with numPix pixels with
    *               a value >= threshold
    */
    public Location[][] cornerArray(int[][] pic, int numPix, int threshold)
    {
        Location[][] output = new Location[NUM_EDGES/2][NUM_EDGES/2];
        
        int[] hor = new int[NUM_EDGES/2];                   //saves horizontal bounds of image
        int[] ver = new int[NUM_EDGES/2];                   //saves vertical bounds of image
        boolean inImage = false;                           //true if scanned edge is within the image
        boolean isDone = false;                             //true if both horizontal/vertical edges have been found
        int pixCount;                                       //number of pixels in scanned edge >= threshold
        int numRows = pic.length;
        int numCols = pic[0].length;
        
        for(int i = 0; i < numRows; i++)                 //scans image for horizontal edges
        {
            pixCount = 0;                                   //resets the counter for each edge
            for(int j = 0; j < numCols; j++)
            {
                if(pic[i][j] >= threshold)
                {
                    pixCount++;
                }
            }
            
            if((pixCount >= numPix) && (inImage == false))
            {
                inImage = true;
                hor[NORTH] = i;
            }
            
            if((pixCount < numPix) && (inImage == true))
            {
                inImage = false;
                hor[SOUTH] = i-1;
                isDone = true;                              //no longer needs to search for horizontal edges 
            }
            
            if(isDone)
            {
                break;
            }
        } //for(int i = 0; i < pic.length; i++)
        
        if(inImage)                                        //if the 2nd horizontal edge hasn't been found at the the end of the array
        {
            inImage = false;
            hor[SOUTH] = numRows - 1;
        }
        
        
        
        isDone = false;
        for(int i = 0; i < numCols; i++)              //scans image for vertical edges
        {
            pixCount = 0;                                   //resets the counter for each edge
            for(int j = 0; j < numRows; j++)
            {
                if(pic[j][i] >= threshold)
                {
                    pixCount++;
                }
            }
            
            if((pixCount >= numPix) && (inImage == false))
            {
                inImage = true;
                ver[WEST] = i;
            }
            
            if((pixCount < numPix) && (inImage == true))
            {
                inImage = false;
                ver[EAST] = i-1;
                isDone = true;                              //no longer needs to search for vertical edges
            }
            
            if(isDone)
            {
                break;
            }
        } //for(int i = 0; i < pic[0].length; i++)
        
        if(inImage)                                        //if the 2nd vertical edge hasn't been found at the the end of the array
        {
            ver[SOUTH] = numCols - 1;
        }
        
        
        
        for(int i = 0; i < NUM_EDGES/2; i++)
        {
            for(int j = 0; j < NUM_EDGES/2; j++)
            {
                output[i][j] = new Location(hor[i], ver[j]);
            }
        }
        
        return output;
    } //private Location[][] cornerArray(int[][] pic, int numPix, int threshold)
    
    /*
    * Parameters: A 2D int array and two ints
    * Function: Scales given image to given dimensions
    * Details: Divides original picture into a width x height grid. Each picture element of the output array
    *               is the average of all the array elements contained in each corresponding grid element of the
    *               original array. For instance, if a 12x12 image is being scaled to a 4x4 image, the 16x16 image
    *               is partitioned into a 4x4 grid: each grid element is actually a 3x3 array of picture elements.
    *               The average of the values in each 3x3 array corresponds to the value set in the scaled picture.
    *          The algorithm used to partition the original picture into a width x height grid is by using horizontal
    *               and vertical dividers that correspond to rows and columns in the original picture array. The array
    *               of dividers requires to be 1 more than the dimensions of the picture like how a 4x4 grid needs 5 horizontal
    *               lines and 5 vertical lines to be drawn.
    */
    public int[][] scaleImage(int[][] pic, Location[][] corners, int width, int height)
    {
        int[][] output = new int[height][width];
        int[] horDividers = new int[width+1];
        int[] verDividers = new int[height+1];
        int horOffset = corners[0][0].getY();
        int verOffset = corners[0][0].getX();
        int originalWidth = Math.abs(corners[0][1].getY() - corners[0][0].getY());
        int originalHeight = Math.abs(corners[1][0].getX() - corners[0][0].getX());
        double horScalingFactor = ((originalWidth + 0.0)/(double)(width));
        double verScalingFactor = ((originalHeight + 0.0)/(double)(height));
        
        for(int i = 0; i < width; i++)                     //calculates the horizontal dividers of original picture
        {
            double div = Math.round((double)i * horScalingFactor);             //prevents truncation from occurring too early
            horDividers[i] = horOffset + (int)div;
            //System.out.println("horDividers[" + i + "] = " + horDividers[i]);
        }
        horDividers[width] = corners[0][1].getY();         //prevents array out of bounds exception in array element calculating for loop          
        //System.out.println("horDividers[" + width + "] = " + horDividers[width]);
        
        for(int i = 0; i < height; i++)                    //calculates the vertical dividers of original picture
        {
            double div = Math.round((double)i * verScalingFactor);             //prevents truncation from occurring too early
            verDividers[i] = verOffset + (int)div;
            //System.out.println("verDividers[" + i + "] = " + verDividers[i]);
        }
        verDividers[height] = corners[1][0].getX();          //prevents array out of bounds exception in array element calculating for loop
        //System.out.println("verDividers[" + height + "] = " + verDividers[height]);
        
        for(int i = 0; i < width; i++)                     //calculates the array element in new picture from grid element from old picture
        {
            int horBound1 = horDividers[i];
            int horBound2 = horDividers[i+1];
            
            for(int j = 0; j < height; j++)
            {
                int verBound1 = verDividers[j];
                int verBound2 = verDividers[j+1];
                
                output[j][i] = avgVal(pic, verBound1, verBound2, horBound1, horBound2);   
            }
        }
        
        return output;
    }//public int[][] scaleImage(int[][] pic, int width, int height)
    
    /*
     * Parameters: a 2d int array
     * Preconditions: input must have a fixed length and width
     * Function: Compresses the 2D array into a 1D int array
     */
     public int[] flattenImage(int[][] img)
     {
         int[] output = new int[img.length*img[0].length];
         
         for(int i = 0; i < img.length; i++)
         {
             for(int j = 0; j < img[i].length; j++)
             {
                 output[i*img.length + j] = img[i][j];
             }
         }
        
         return output;
     }
    
    /*
    * Special condition: x0 and y0 have to be >= 0; x1 and y1 have to be less than the bounds of the array
    * Parameters: A 2D int array and 4 ints
    * Function: Calculates the average value (rounded to the nearest integer) of the elements
    *           in the array that are within the bounds.
    */
    private int avgVal(int[][] pic, int x0, int x1, int y0, int y1)
    {
        double output = 0;
        int truncatedOutput = 0;
        int numVals = 0;
        
        for(int i = x0; i <= x1; i++)
        {
            for(int j = y0; j <= y1; j++)
            {
                output += pic[i][j];
                numVals++;
            }
        }
        output /= (numVals + 0.0);
        
        truncatedOutput = (int)output;
        
        
        if(output - truncatedOutput >= .5)
        {
            truncatedOutput++;
        }
        
        return truncatedOutput;
    } //private int avgVal(int[][] pic, int x0, int x1, int y0, int y1)
    
    /*
    * Tests the circle processor
    */
    public static void main(String[] args)
    {
        int[][] picture = new int[9][10];
        
        for(int i = 1; i <= 8; i++)
        {
            picture[0][i] = 1;
            picture[1][i] = 1;
            picture[2][i] = 1;
            picture[8][i] = 1;
        }
        
        picture[1][4] = 0;
        picture[2][4] = 0;
        picture[2][8] = 0;
        picture[3][4] = 1;
        picture[3][5] = 1;
        picture[3][6] = 1;
        picture[4][4] = 1;
        picture[4][5] = 1;
        picture[4][3] = 1;
        picture[5][4] = 1;
        picture[5][5] = 1;
        picture[5][3] = 1;
        picture[6][4] = 1;
        picture[6][2] = 1;
        picture[6][3] = 1;
        picture[6][7] = 1;
        picture[6][8] = 1;
        picture[6][9] = 1;
        picture[6][1] = 1;
        picture[6][2] = 1;
        picture[6][3] = 1;
        picture[6][7] = 1;
        picture[6][8] = 1;
        picture[6][9] = 1;
        picture[7][4] = 1;
        picture[7][2] = 1;
        picture[7][3] = 1;
        picture[7][7] = 1;
        picture[7][8] = 1;
        picture[7][9] = 1;
        picture[7][1] = 1;
        picture[7][2] = 1;
        picture[7][3] = 1;
        picture[7][7] = 1;
        picture[7][8] = 1;
        picture[7][9] = 1;
        picture[8][0] = 1;
        picture[8][9] = 1;
        
        /*
        picture[2][6] = 1;
        picture[3][3] = 1;
        picture[3][4] = 1;
        picture[3][7] = 1;
        picture[3][8] = 1;
        picture[4][3] = 1;
        picture[4][8] = 1;
        picture[5][3] = 1;
        picture[5][8] = 1;
        picture[6][3] = 1;
        picture[6][8] = 1;
        picture[6][4] = 1;
        picture[6][7] = 1;
        picture[7][5] = 1;
        picture[7][6] = 1;*/
        
        ImageProcessor processor = new ImageProcessor();
        
        //int[][] centeredPic = processor.shiftImage(picture);
        Location[][] corners = processor.cornerArray(picture, 1, 1);
        
        
        
        for(int i = 0; i < picture.length; i++)
        {
            for(int j = 0; j < picture[0].length; j++)
            {
                System.out.print(picture[i][j] + " ");
            }
            System.out.println();
        }
        
        System.out.println();
        
        for(int i = 0; i < corners.length; i++)
        {
            for(int j = 0; j < corners[0].length; j++)
            {
                System.out.print(corners[i][j] + " ");
            }
            System.out.println();
        }
        
        int[][] scaledImage = processor.scaleImage(picture, corners, 24, 21);
        
        for(int i = 0; i < scaledImage.length; i++)
        {
            for(int j = 0; j < scaledImage[0].length; j++)
            {
                System.out.print(scaledImage[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    /*
    * A Location object will save a 2D location vector.
    * For example, the coordinates of a 2D array would be in located in array[x][y].
    * The coordinates are saved as integers.
    */
    private class Location
    {
        private int x;
        private int y;
        
        /*
        * Parameters: 2 ints
        * Function: Creates a Location object
        */
        public Location(int a, int b)
        {
            x = a;
            y = b;
        }
        
        /*
        * Parameters: n/a
        * Function: Returns the x-component.
        */
        public int getX()
        {
            return x;
        }
        
        /*
        * Parameters: n/a
        * Function: Returns the y-component.
        */
        public int getY()
        {
            return y;
        }
        
        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         * Parameters: n/a
         * Function: Returns the Location as a String
         */
        public String toString()
        {
            return "(" + x + ", " + y + ")";
        }
    } //private class Location
} //public class CircleProcessor