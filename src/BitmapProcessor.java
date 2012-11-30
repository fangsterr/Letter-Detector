/*
 * Author: Andy Fang
 * Date Created: 02/23/09
 * Description: Used code from Eric Nelson's DibDump.java created on 02/02/09
 * Classes in the file:
 *  RgbQuad
 *  DibDump
 *  
 * Methods in this file:
 *  int     swapInt(int v)
 *  int     swapShort(int v)
 *  RgbQuad pelToRGBQ(int pel)
 *  int     rgbqToPel(int red, int green, int blue, int reserved)
 *  RgbQuad pelToRGB(int pel)
 *  int     rgbToPel(int red, int green, int blue)
 *  int     colorToGrayscale(int pel)
 *  void    main(String[] args)
 *  
 * There is a lot of cutting and pasting from various
 * documents dealing with bitmaps and I have not taken the
 * time to clean up the formatting in the comments. The C syntax is
 * included for reference. The types are declared in windows.h. The C
 * structures and data arrays are predefined static so that they don't
 * ever fall out of scope.
 * 
 * Notes on reading bitmaps:
 *
 * The BMP format assumes an Intel integer type (little endian), however, the Java virtual machine
 * uses the Motorola integer type (big endian), so we have to do a bunch of byte swaps to get things
 * to read and write correctly. Also note that many of the values in a bitmap header are unsigned
 * integers of some kind and Java does not know about unsigned values, except for reading in
 * unsigned byte and unsigned short, but the unsigned int still poses a problem.
 * We don't do any math with the unsigned int values, so we won't see a problem.
 *
 * Bitmaps on disk have the following basic structure
 *  BITMAPFILEHEADER (may be missing is file is not saved properly)
 *  BITMAPINFO -
 *        BITMAPINFOHEADER
 *        RGBQUAD - Color Table Array (not present for true color images)
 *  Bitmap Bits in one of many coded formats
 *
 *  The BMP image is stored from bottom to top, meaning that the first scan line in the file is the last scan line in the image.
 *
 *  For ALL images types, each scan line is padded to an even 4-byte boundary.
 *  
 *  For images where there are multiple pels per bye, the left side is the high order elements and the right is the
 *  low order element.
 *
 *  in Windows...
 *  DWORD is an unsigned 4 byte integer
 *  WORD is an unsigned 2 byte integer
 *  LONG is a 4 byte signed integer
 *
 *  in Java we have the following sizes:
 *
 * byte
 *   1 signed byte (two's complement). Covers values from -128 to 127.
 *
 * short
 *   2 bytes, signed (two's complement), -32,768 to 32,767
 *
 * int
 *   4 bytes, signed (two's complement). -2,147,483,648 to 2,147,483,647.
 *   Like all numeric types ints may be cast into other numeric types (byte, short, long, float, double).
 *   When lossy casts are done (e.g. int to byte) the conversion is done modulo the length of the smaller type.
 */
import java.io.*;

/*
 * A member-variable-only class for holding the RGBQUAD C structure elements.
 */
final class RgbQuad
{
    int red;
    int green;
    int blue;
    int reserved;
}

public class BitmapProcessor
{
// BITMAPFILEHEADER
    static int bmpFileHeader_bfType;          // WORD
    static int bmpFileHeader_bfSize;          // DWORD
    static int bmpFileHeader_bfReserved1;     // WORD
    static int bmpFileHeader_bfReserved2;     // WORD
    static int bmpFileHeader_bfOffBits;       // DWORD
// BITMAPINFOHEADER
    static int bmpInfoHeader_biSize;          // DWORD
    static int bmpInfoHeader_biWidth;         // LONG
    static int bmpInfoHeader_biHeight;        // LONG
    static int bmpInfoHeader_biPlanes;        // WORD
    static int bmpInfoHeader_biBitCount;      // WORD
    static int bmpInfoHeader_biCompression;   // DWORD
    static int bmpInfoHeader_biSizeImage;     // DWORD
    static int bmpInfoHeader_biXPelsPerMeter; // LONG
    static int bmpInfoHeader_biYPelsPerMeter; // LONG
    static int bmpInfoHeader_biClrUsed;       // DWORD
    static int bmpInfoHeader_biClrImportant;  // DWORD
// The true color pels
    static int[][] imageArray;
//output file for processed bitmap
    //static final String outFile = "out.bmp";

/*
* Methods to go between little and big endian integer formats.
*/
    public int swapInt(int v)
    {
        return  (v >>> 24) | (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
    }

    public int swapShort(int v)
    {
        return  ((v << 8) & 0xFF00) | ((v >> 8) & 0x00FF);
    }
/*
 * Method pelToRGBQ accepts an integer (32 bit) picture element and returns the red, green and blue colors.
 * Unlike pelToRGB, this method also extracts the most significant byte and populates the reserved element of RgbQuad.
 * It returns an RgbQuad object. See rgbqToPel(int red, int green, int blue, int reserved) to go the the other way. 
 */
    public RgbQuad pelToRGBQ(int pel)
    {
        RgbQuad rgbq = new RgbQuad();

        rgbq.blue     =  pel        & 0x00FF;
        rgbq.green    = (pel >> 8)  & 0x00FF;
        rgbq.red      = (pel >> 16) & 0x00FF;
        rgbq.reserved = (pel >> 24) & 0x00FF;
            
        return rgbq;        
    }

/*
 * The rgbqToPel method takes red, green and blue color values plus an additional byte and returns a single 32-bit integer color.
 * See pelToRGBQ(int pel) to go the other way.
 */
        public int rgbqToPel(int red, int green, int blue, int reserved)
        {
            return (reserved << 24) | (red << 16) | (green << 8) | blue;
        }

/*
 * Method pelToRGB accepts an integer (32 bit) picture element and returns the red, green and blue colors
 * as an RgbQuad object. See rgbToPel(int red, int green, int blue) to go the the other way. 
 */
    public RgbQuad pelToRGB(int pel)
    {
        RgbQuad rgb = new RgbQuad();

        rgb.reserved = 0;

        rgb.blue  =  pel        & 0x00FF;
        rgb.green = (pel >> 8)  & 0x00FF;
        rgb.red   = (pel >> 16) & 0x00FF;
        
        return rgb;        
    }

/*
 * The rgbToPel method takes red, green and blue color values and returns a single 32-bit integer color.
 * See pelToRGB(int pel) to go the other way.
 */
    public int rgbToPel(int red, int green, int blue)
    {
        return (red << 16) | (green << 8) | blue;
    }

 /*
 * Y = 0.3RED+0.59GREEN+0.11Blue
 * The colorToGrayscale method takes a color picture element (pel) and returns the gray scale pel.
 */
    public int colorToGrayscale(int pel)
    {
        RgbQuad rgb = pelToRGB(pel);
    
        int lum = (int)Math.round(0.3 * (double)rgb.red + 0.589 * (double)rgb.green + 0.11 * (double)rgb.blue);

        return rgbToPel(lum, lum, lum);
    }

    public BitmapProcessor(String inFileName)
    {
        //String outFileName;
        int i, j, k;
        int numberOfColors;
        int pel;
        int iByteVal, iColumn, iBytesPerRow, iPelsPerRow, iTrailingBits, iDeadBytes;
        int[] colorPallet = new int[256];                       // color table with space reserved for the largest possible color table (256-bits)
// RBGQUAD
        int rgbQuad_rgbBlue;
        int rgbQuad_rgbGreen;
        int rgbQuad_rgbRed;
        int rgbQuad_rgbReserved;    
        
        
        //outFileName = outFile;                                  // saves processed bitmap into pre-configured output file
        
        try
        {
            FileInputStream fstream = new FileInputStream(inFileName);
            DataInputStream in = new DataInputStream(fstream);  //allows for access into bytes of the bitmap file
            
/*
*  Read in BITMAPFILEHEADER
*
*  bfType
*      Specifies the file type. It must be set to the signature word BM (0x4D42) to indicate bitmap.
*  bfSize
*      Specifies the size, in bytes, of the bitmap file.
*  bfReserved1
*      Reserved; set to zero
*  bfReserved2
*      Reserved; set to zero
*  bfOffBits
*      Specifies the offset, in bytes, from the BITMAPFILEHEADER structure to the bitmap bits
*/
            // Read and Convert to big endian
            bmpFileHeader_bfType      = swapShort(in.readUnsignedShort());    // WORD
            bmpFileHeader_bfSize      = swapInt(in.readInt());                // DWORD
            bmpFileHeader_bfReserved1 = swapShort(in.readUnsignedShort());    // WORD
            bmpFileHeader_bfReserved2 = swapShort(in.readUnsignedShort());    // WORD
            bmpFileHeader_bfOffBits   = swapInt(in.readInt());                // DWORD
            
            if(bmpFileHeader_bfType != 0x4D42)                                //checks to see if file is a true bitmap file
            {
                throw new RuntimeException("File is not a bitmap. Please check the file and run the program again.");
            }

/*
*    Read in BITMAPINFOHEADER
*
*                 typedef struct tagBITMAPINFOHEADER{
*                          DWORD      biSize;
*                          LONG       biWidth;
*                          LONG       biHeight;
*                          WORD       biPlanes;
*                          WORD       biBitCount;
*                          DWORD      biCompression;
*                          DWORD      biSizeImage;
*                          LONG       biXPelsPerMeter;
*                          LONG       biYPelsPerMeter;
*                          DWORD      biClrUsed;
*                          DWORD      biClrImportant;
*                  } BITMAPINFOHEADER, FAR *LPBITMAPINFOHEADER, *PBITMAPINFOHEADER;
*
*
*   biSize
*       Specifies the size of the structure, in bytes.
*       This size does not include the color table or the masks mentioned in the biClrUsed member.
*       See the Remarks section for more information.
*   biWidth
*       Specifies the width of the bitmap, in pixels.
*   biHeight
*       Specifies the height of the bitmap, in pixels.
*       If biHeight is positive, the bitmap is a bottom-up DIB and its origin is the lower left corner.
*       If biHeight is negative, the bitmap is a top-down DIB and its origin is the upper left corner.
*       If biHeight is negative, indicating a top-down DIB, biCompression must be either BI_RGB or BI_BITFIELDS. Top-down DIBs cannot be compressed.
*   biPlanes
*       Specifies the number of planes for the target device.
*       This value must be set to 1.
*   biBitCount
*       Specifies the number of bits per pixel.
*       The biBitCount member of the BITMAPINFOHEADER structure determines the number of bits that define each pixel and the maximum number of colors in the bitmap.
*       This member must be one of the following values.
*       Value     Description
*       1       The bitmap is monochrome, and the bmiColors member contains two entries.
*               Each bit in the bitmap array represents a pixel. The most significant bit is to the left in the image. 
*               If the bit is clear, the pixel is displayed with the color of the first entry in the bmiColors table.
*               If the bit is set, the pixel has the color of the second entry in the table.
*       2       The bitmap has four possible color values.  The most significant half-nibble is to the left in the image.
*       4       The bitmap has a maximum of 16 colors, and the bmiColors member contains up to 16 entries.
*               Each pixel in the bitmap is represented by a 4-bit index into the color table. The most significant nibble is to the left in the image.
*               For example, if the first byte in the bitmap is 0x1F, the byte represents two pixels. The first pixel contains the color in the second table entry, and the second pixel contains the color in the sixteenth table entry.
*       8       The bitmap has a maximum of 256 colors, and the bmiColors member contains up to 256 entries. In this case, each byte in the array represents a single pixel.
*       16      The bitmap has a maximum of 2^16 colors.
*               If the biCompression member of the BITMAPINFOHEADER is BI_RGB, the bmiColors member is NULL.
*               Each WORD in the bitmap array represents a single pixel. The relative intensities of red, green, and blue are represented with 5 bits for each color component.
*               The value for blue is in the least significant 5 bits, followed by 5 bits each for green and red.
*               The most significant bit is not used. The bmiColors color table is used for optimizing colors used on palette-based devices, and must contain the number of entries specified by the biClrUsed member of the BITMAPINFOHEADER.
*       24      The bitmap has a maximum of 2^24 colors, and the bmiColors member is NULL.
*               Each 3-byte triplet in the bitmap array represents the relative intensities of blue, green, and red, respectively, for a pixel.
*               The bmiColors color table is used for optimizing colors used on palette-based devices, and must contain the number of entries specified by the biClrUsed member of the BITMAPINFOHEADER.
*       32      The bitmap has a maximum of 2^32 colors. If the biCompression member of the BITMAPINFOHEADER is BI_RGB, the bmiColors member is NULL. Each DWORD in the bitmap array represents the relative intensities
*                       of blue, green, and red, respectively, for a pixel. The high byte in each DWORD is not used. The bmiColors color table is
*               used for optimizing colors used on palette-based devices, and must contain the number of entries specified by the biClrUsed member of the BITMAPINFOHEADER.
*               If the biCompression member of the BITMAPINFOHEADER is BI_BITFIELDS, the bmiColors member contains three DWORD color masks that specify the red, green, and blue components, respectively, of each pixel.
*               Each DWORD in the bitmap array represents a single pixel.
*   biCompression
*       Specifies the type of compression for a compressed bottom-up bitmap (top-down DIBs cannot be compressed). This member can be one of the following values.
*       Value               Description
*       BI_RGB              An uncompressed format.
*       BI_BITFIELDS        Specifies that the bitmap is not compressed and that the color table consists of three DWORD color masks that specify the red, green, and blue components of each pixel.
*                           This is valid when used with 16- and 32-bpp bitmaps.
*                           This value is valid in Windows Embedded CE versions 2.0 and later.
*       BI_ALPHABITFIELDS   Specifies that the bitmap is not compressed and that the color table consists of four DWORD color masks that specify the red, green, blue, and alpha components of each pixel.
*                           This is valid when used with 16- and 32-bpp bitmaps.
*                           This value is valid in Windows CE .NET 4.0 and later.
*                           You can OR any of the values in the above table with BI_SRCPREROTATE to specify that the source DIB section has the same rotation angle as the destination.
*   biSizeImage
*       Specifies the size, in bytes, of the image. This value will be the number of bytes in each scan line which must be padded to
*       insure the line is a multiple of 4 bytes (it must align on a DWORD boundary) times the number of rows.
*       This value may be set to zero for BI_RGB bitmaps (so you cannot be sure it will be set).
*   biXPelsPerMeter
*       Specifies the horizontal resolution, in pixels per meter, of the target device for the bitmap.
*       An application can use this value to select a bitmap from a resource group that best matches the characteristics of the current device.
*   biYPelsPerMeter
*       Specifies the vertical resolution, in pixels per meter, of the target device for the bitmap
*   biClrUsed
*       Specifies the number of color indexes in the color table that are actually used by the bitmap.
*       If this value is zero, the bitmap uses the maximum number of colors corresponding to the value of the biBitCount member for the compression mode specified by biCompression.
*       If biClrUsed is nonzero and the biBitCount member is less than 16, the biClrUsed member specifies the actual number of colors the graphics engine or device driver accesses.
*       If biBitCount is 16 or greater, the biClrUsed member specifies the size of the color table used to optimize performance of the system color palettes.
*       If biBitCount equals 16 or 32, the optimal color palette starts immediately following the three DWORD masks.
*       If the bitmap is a packed bitmap (a bitmap in which the bitmap array immediately follows the BITMAPINFO header and is referenced by a single pointer), the biClrUsed member must be either zero or the actual size of the color table.
*   biClrImportant
*       Specifies the number of color indexes required for displaying the bitmap.
*       If this value is zero, all colors are required.
*   Remarks
*
*   The BITMAPINFO structure combines the BITMAPINFOHEADER structure and a color table to provide a complete definition of the dimensions and colors of a DIB.
*   An application should use the information stored in the biSize member to locate the color table in a BITMAPINFO structure, as follows.
*
*   pColor = ((LPSTR)pBitmapInfo + (WORD)(pBitmapInfo->bmiHeader.biSize));
*/
            
            // Read and convert to big endian
            bmpInfoHeader_biSize          = swapInt(in.readInt());              // DWORD
            bmpInfoHeader_biWidth         = swapInt(in.readInt());              // LONG
            bmpInfoHeader_biHeight        = swapInt(in.readInt());              // LONG
            bmpInfoHeader_biPlanes        = swapShort(in.readUnsignedShort());  // WORD
            bmpInfoHeader_biBitCount      = swapShort(in.readUnsignedShort());  // WORD
            bmpInfoHeader_biCompression   = swapInt(in.readInt());              // DWORD
            bmpInfoHeader_biSizeImage     = swapInt(in.readInt());              // DWORD
            bmpInfoHeader_biXPelsPerMeter = swapInt(in.readInt());              // LONG
            bmpInfoHeader_biYPelsPerMeter = swapInt(in.readInt());              // LONG
            bmpInfoHeader_biClrUsed       = swapInt(in.readInt());              // DWORD
            bmpInfoHeader_biClrImportant  = swapInt(in.readInt());              // DWORD
            
/*
*Now for the color table. For true color images, there isn't one.
*
*typedef struct tagRGBQUAD {
*        BYTE    rgbBlue;
*        BYTE    rgbGreen;
*        BYTE    rgbRed;
*        BYTE    rgbReserved;
*        } RGBQUAD;
*
*typedef RGBQUAD FAR* LPRGBQUAD;
*/
    
            switch (bmpInfoHeader_biBitCount) // Determine the number of colors in the default color table
            {
                case 1:
                    numberOfColors = 2;
                    break;
                case 2:
                    numberOfColors = 4;
                    break;
                case 4:
                    numberOfColors = 16;
                    break;
                case 8:
                    numberOfColors = 256;
                    break;
                default:
                    numberOfColors = 0; // no color table
            }
            
/*
* biClrUsed -  Specifies the number of color indexes in the color table that are actually used by the bitmap.
*     If this value is zero, the bitmap uses the maximum number of colors corresponding to the value of the biBitCount member for the compression mode specified by biCompression.
*     If biClrUsed is nonzero and the biBitCount member is less than 16, the biClrUsed member specifies the actual number of colors the graphics engine or device driver accesses.
*     If biBitCount is 16 or greater, the biClrUsed member specifies the size of the color table used to optimize performance of the system color palettes.
*     If biBitCount equals 16 or 32, the optimal color palette starts immediately following the three DWORD masks.
*     If the bitmap is a packed bitmap (a bitmap in which the bitmap array immediately follows the BITMAPINFO header and is referenced by a single pointer), the biClrUsed member must be either zero or the actual size of the color table.
*/
           if (bmpInfoHeader_biClrUsed > 0) numberOfColors = bmpInfoHeader_biClrUsed;
           
           for (i = 0; i < numberOfColors; ++i) // Read in the color table (or not if numberOfColors is zero)
           {
               rgbQuad_rgbBlue      = in.readUnsignedByte(); // lowest byte in the color
               rgbQuad_rgbGreen     = in.readUnsignedByte();
               rgbQuad_rgbRed       = in.readUnsignedByte(); // highest byte in the color
               rgbQuad_rgbReserved  = in.readUnsignedByte();

               // Build the color from the RGB values. Since we declared the rgbQuad values to be int, we can shift and then OR the values
               // to build up the color. Since we are reading one byte at a time, there are no "endian" issues.

               colorPallet[i] = (rgbQuad_rgbRed << 16) | (rgbQuad_rgbGreen << 8) | rgbQuad_rgbBlue;
           }
/*
* Now, we need to read in the rest of the bit map, but how we interpret the values depends on the color depth.
*
* numberOfColors = 2:   Each bit is a pel, so there are 8 pels per byte. The Color Table has only two values for "black" and "white"
* numberOfColors = 4:   Each pair of bits is a pel, so there are 4 pels per byte. The Color Table has only four values
* numberOfColors = 16;  Each nibble (4 bits) is a pel, so there are 2 pels per byte. The Color Table has 16 entries.
* numberOfColors = 256; Each byte is a pel and the value maps into the 256 byte Color Table.
*
* Any other value is read in as "true" color.
*
* The BMP image is stored from bottom to top, meaning that the first scan line is the last scan line in the image.
*
* The rest is the bitmap. Use the height and width information to read it in.
* In the 24-bit format, each pixel in the image is represented by a series of three bytes of RGB stored as BRG.
* For ALL image types each scan line is padded to an even 4-byte boundary.
*
*/
           imageArray = new int[bmpInfoHeader_biHeight][bmpInfoHeader_biWidth]; // Create the array for the pels
/*
* I use the same loop structure for each case for clarity so you can see the similarities and differences.
* The outer loop is over the rows (in reverse), the inner loop over the columns. 
*/
           switch (bmpInfoHeader_biBitCount)
           {
              
               case 24: // Works
/*
* Each three bytes read in is 1 column. Each scan line is padded to by a multiple of 4 bytes.
*/
                   iPelsPerRow = bmpInfoHeader_biWidth;
                   iDeadBytes = (4 - (iPelsPerRow * 3) % 4) % 4;
    
                   for (i = bmpInfoHeader_biHeight - 1; i >= 0; --i) // read over the rows
                   {
                       for (j = 0; j < iPelsPerRow; ++j)         // j is now just the column counter
                       {
                           rgbQuad_rgbBlue      = in.readUnsignedByte();
                           rgbQuad_rgbGreen     = in.readUnsignedByte();
                           rgbQuad_rgbRed       = in.readUnsignedByte();
                           pel = (rgbQuad_rgbRed << 16) | (rgbQuad_rgbGreen << 8) | rgbQuad_rgbBlue;
                           imageArray[i][j] = pel;
                       }
                       for (j = 0; j < iDeadBytes; ++j) in.readUnsignedByte(); // Now read in the "dead bytes" to pad to a 4 byte boundary
                   }
                   break;
               default: // Oops
                   throw new RuntimeException("This can only read in 24-bit BMPs. Please reformat the image and try again.\n");

           } // switch (bmpInfoHeader_biBitCount)

           in.close();
           fstream.close();
           
        } //try
        catch (Exception e)
        {
            System.err.println("File input error" + e);
        }
        

/*
 * Console dump of image bytes in HEX if the image is smaller than 33 x 33
 */

        if ((bmpInfoHeader_biWidth < 33) && (bmpInfoHeader_biHeight < 33))
        {
            iBytesPerRow = bmpInfoHeader_biWidth;
            for (i = 0; i < bmpInfoHeader_biHeight; ++i) // read over the rows
            {
                for (j = 0; j < iBytesPerRow; ++j)         // j is now just the column counter
                {
                    //System.out.printf("%d\t", imageArray[i][j]);
                    //System.out.printf("%06X\t", imageArray[i][j]);
                }
                //System.out.printf("\n");
            }
        }

        /**
/*
 * Now write out the true color bitmap to a disk file. This is here mostly to be sure we did it all correctly.
 *
 *
        try
        {
            iDeadBytes = (4 - (bmpInfoHeader_biWidth * 3) % 4) % 4;

            bmpInfoHeader_biSizeImage =  (bmpInfoHeader_biWidth * 3 + iDeadBytes) * bmpInfoHeader_biHeight;
            bmpFileHeader_bfOffBits = 54;        // 54 byte offset for 24 bit images (just open one with this app to get this value)
            bmpFileHeader_bfSize = bmpInfoHeader_biSizeImage + bmpFileHeader_bfOffBits;
            bmpInfoHeader_biBitCount = 24;       // 24 bit color image
            bmpInfoHeader_biCompression = 0;     // BI_RGB (which is a value of zero)
            bmpInfoHeader_biClrUsed = 0;         // Zero for true color
            bmpInfoHeader_biClrImportant = 0;    // Zero for true color

            FileOutputStream fstream = new FileOutputStream(outFileName);
            DataOutputStream out = new DataOutputStream(fstream);

// BITMAPFILEHEADER
            out.writeShort(swapShort(bmpFileHeader_bfType));      // WORD
            out.writeInt(swapInt(bmpFileHeader_bfSize));          // DWORD
            out.writeShort(swapShort(bmpFileHeader_bfReserved1)); // WORD
            out.writeShort(swapShort(bmpFileHeader_bfReserved2)); // WORD
            out.writeInt(swapInt(bmpFileHeader_bfOffBits));       // DWORD

// BITMAPINFOHEADER
            out.writeInt(swapInt(bmpInfoHeader_biSize));          // DWORD
            out.writeInt(swapInt(bmpInfoHeader_biWidth));         // LONG
            out.writeInt(swapInt(bmpInfoHeader_biHeight));        // LONG
            out.writeShort(swapShort(bmpInfoHeader_biPlanes));    // WORD
            out.writeShort(swapShort(bmpInfoHeader_biBitCount));  // WORD
            out.writeInt(swapInt(bmpInfoHeader_biCompression));   // DWORD
            out.writeInt(swapInt(bmpInfoHeader_biSizeImage));     // DWORD
            out.writeInt(swapInt(bmpInfoHeader_biXPelsPerMeter)); // LONG
            out.writeInt(swapInt(bmpInfoHeader_biYPelsPerMeter)); // LONG
            out.writeInt(swapInt(bmpInfoHeader_biClrUsed));       // DWORD
            out.writeInt(swapInt(bmpInfoHeader_biClrImportant));  // DWORD

// there is no color table for this true color image, so write out the pels

            for (i = bmpInfoHeader_biHeight - 1; i >= 0; --i)    // write over the rows
            {
                for (j = 0; j < bmpInfoHeader_biWidth; ++j) // and the columns
                {
                    pel = imageArray[i][j];
                    rgbQuad_rgbBlue  = pel & 0x00FF;
                    rgbQuad_rgbGreen = (pel >> 8)  & 0x00FF;
                    rgbQuad_rgbRed   = (pel >> 16) & 0x00FF;
                    out.writeByte(rgbQuad_rgbBlue); // lowest byte in the color
                    out.writeByte(rgbQuad_rgbGreen);
                    out.writeByte(rgbQuad_rgbRed);  // highest byte in the color
                }
                for (j = 0; j < iDeadBytes; ++j)
                {
                    out.writeByte(0); // Now write out the "dead bytes" to pad to a 4 byte boundary
                }
            }

            out.close();
            fstream.close();
        } //try
        catch (Exception e)
        {
            System.err.println("File output error" + e);
        }
        */
    }
    
    /*
     * Function: Gets the image array in a usable form for the ImageProcessor
     */
    public int[][] getImage()
    {
        int[][] output = new int[bmpInfoHeader_biHeight][bmpInfoHeader_biWidth];
        
        for (int i = 0; i < bmpInfoHeader_biHeight; i++) // read over the rows
        {
            for (int j = 0; j < bmpInfoHeader_biWidth; j++)
            {
                output[i][j] = 16777215 - imageArray[i][j]; //subtracts from the value of white so that white = 0
                /*if(imageArray[i][j] > 0xFFFFFF/2)          // if pel is more white than black, convert it to 0
                {
                    output[i][j] = 0;
                }
                else
                {
                    output[i][j] = 1;
                }*/
            }
        }
        
        return output;
    }
    
    /*
     * Function: Gets the byte array for the image
     */
    public int[][] getImageRaw()
    {
        return imageArray;
    }
    
    /*
     * Function: Returns the image type of the saved bitmap
     */
    public int getImageType()
    {
        return bmpInfoHeader_biBitCount;
    }

    /*
     * Function: Saves input image from outFile source as the true color image of the same type of bitmap as the inputed image
     */
    public void saveAsBMP(int[][] image, String outFile)
    {
        int iDeadBytes;
        int pel;
        int rgbQuad_rgbBlue, rgbQuad_rgbGreen, rgbQuad_rgbRed;
        int width, height, imageSize, colorsUsed, colorsImportant, compression, fileSize;
        
        width = image[0].length;
        height = image.length;
        
        try
        {
            iDeadBytes = (4 - (width * 3) % 4) % 4;

            imageSize =  (width * 3 + iDeadBytes) * height;
            fileSize = imageSize + bmpFileHeader_bfOffBits;      // off bits determined in constructor method
            compression = 0;                                     // BI_RGB (which is a value of zero)
            colorsUsed = 0;                                      // Zero for true color
            colorsImportant = 0;                                 // Zero for true color

            FileOutputStream fstream = new FileOutputStream(outFile);
            DataOutputStream out = new DataOutputStream(fstream);

// BITMAPFILEHEADER
            out.writeShort(swapShort(bmpFileHeader_bfType));      // WORD; predetermined in constructor
            out.writeInt(swapInt(fileSize));                      // DWORD
            out.writeShort(swapShort(bmpFileHeader_bfReserved1)); // WORD; predetermined in constructor
            out.writeShort(swapShort(bmpFileHeader_bfReserved2)); // WORD; predetermined in constructor
            out.writeInt(swapInt(bmpFileHeader_bfOffBits));       // DWORD; predetermined in constructor

// BITMAPINFOHEADER
            out.writeInt(swapInt(bmpInfoHeader_biSize));          // DWORD; predetermined in constructor
            out.writeInt(swapInt(width));                         // LONG
            out.writeInt(swapInt(height));                        // LONG
            out.writeShort(swapShort(bmpInfoHeader_biPlanes));    // WORD; predetermined in constructor
            out.writeShort(swapShort(bmpInfoHeader_biBitCount));  // WORD; predetermined in constructor
            out.writeInt(swapInt(compression));                   // DWORD
            out.writeInt(swapInt(imageSize));                     // DWORD
            out.writeInt(swapInt(bmpInfoHeader_biXPelsPerMeter)); // LONG; predetermined in constructor
            out.writeInt(swapInt(bmpInfoHeader_biYPelsPerMeter)); // LONG; predetermined in constructor
            out.writeInt(swapInt(colorsUsed));       // DWORD
            out.writeInt(swapInt(colorsImportant));  // DWORD

// there is no color table for this true color image, so write out the pels

            for (int i = height - 1; i >= 0; i--)    // write over the rows
            {
                for (int j = 0; j < width; j++)      // and the columns
                {
                    pel = image[i][j];
                    pel = 16777215 - pel;
                    pel = colorToGrayscale(pel);
                    /*if(pel > 0)
                    {
                        pel = 0;                // sets the pel to black 
                    }
                    else
                    {
                        pel = 0xFFFFFF;                // sets the pel to white
                    }*/
                    rgbQuad_rgbBlue  = pel & 0x00FF;
                    rgbQuad_rgbGreen = (pel >> 8)  & 0x00FF;
                    rgbQuad_rgbRed   = (pel >> 16) & 0x00FF;
                    out.writeByte(rgbQuad_rgbBlue); // lowest byte in the color
                    out.writeByte(rgbQuad_rgbGreen);
                    out.writeByte(rgbQuad_rgbRed);  // highest byte in the color
                    //out.writeInt(swapInt(16777215-pel));
                }
                for (int j = 0; j < iDeadBytes; j++)
                {
                    out.writeByte(0); // Now write out the "dead bytes" to pad to a 4 byte boundary
                }
            }

            out.close();
            fstream.close();
        } //try
        catch (Exception e)
        {
            System.err.println("File output error" + e);
        }
    }
    
    public static void main(String[] args)
    {
        BitmapProcessor lard = new BitmapProcessor("24bittest.bmp");
        ImageProcessor marge = new ImageProcessor();
        
        //System.out.println(lard.getImageType());
        
        int[][] farb = lard.getImage();
        //farb = marge.shiftImage(farb);
        
        for(int i = 0; i < farb.length; i++)
        {
            for(int j = 0; j < farb[0].length; j++)
            {
                if(farb[i][j] > 0)
                    System.out.print("1 ");
                else
                    System.out.print("0 ");
                //System.out.print(farb[i][j] + " ");
            }
            System.out.println();
        }
        
        for(int i = 0; i < 2; i ++)
        {
            for(int j = 0; j < 2; j++)
            {
                System.out.print(marge.cornerArray(farb, 1, 1)[i][j] + " ");
            }
            System.out.println();
        }
        
        farb = marge.scaleImage(farb, marge.cornerArray(farb, 1, 1), 32, 32);
        
        for(int i = 0; i < farb.length; i++)
        {
            for(int j = 0; j < farb[0].length; j++)
            {
                if(farb[i][j] > 0)
                    System.out.print("1 ");
                else
                    System.out.print("0 ");
            }
            System.out.println();
        }
        
        
        lard.saveAsBMP(farb, "24bitout.bmp");
    }  
} // public class BitmapProcessor

