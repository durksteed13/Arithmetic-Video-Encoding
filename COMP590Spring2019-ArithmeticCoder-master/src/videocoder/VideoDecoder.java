package videocoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import app.FreqCountIntegerSymbolModel;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

public class VideoDecoder {
	
	public static void main(String args[]) throws InsufficientBitsLeftException, IOException {
		
		String file = "data/out.dat";
		InputStream iStream = new FileInputStream(file);
		InputStreamBitSource iStreamSource = new InputStreamBitSource(iStream);
		
		String ofile = "data/compressed.dat";
		OutputStream oStream = new FileOutputStream(file);
		OutputStreamBitSink oStreamSource = new OutputStreamBitSink(oStream);
		
		int framelength = 4096;
		
		File fileb = new File(file);
		long numBytes = fileb.length();
		System.out.println(numBytes);
		
		ArrayList<Integer> symbollist = new ArrayList<Integer>();
		
		Integer[] pixels = new Integer[(int) numBytes];
		int[] freqs = new int[256];
		
		int comparison = 0;
		for(int i = 0; i < numBytes; i++) {
			int next = iStreamSource.next(8);
			freqs[next] = freqs[next] + 1;
			if(i % framelength == 0) {
				System.out.println(i);
				comparison = next;
				pixels[i] = next;
			
				if(!symbollist.contains(i)) {
					symbollist.add(next);
				}
			}
			else {
				pixels[i] = next - comparison;
				
				if(!symbollist.contains(i)) {
					symbollist.add(next-comparison);
				}
			}
		}
		
		Integer[] pixelsymbols = new Integer[]
		
		FreqCountIntegerSymbolModel f = new FreqCountIntegerSymbolModel(pixels, freqs);
		
		
		
		
//		for(int i = 0; i < 256; i++) {
//			
//			System.out.println(i + "--> " + freqs[i]);
//		}
//		
//		for(int i = 0; i < 1000; i++) {
//			System.out.println(pixels[i]);
//		}
//		
//		System.out.println("done");
		
	}

}
