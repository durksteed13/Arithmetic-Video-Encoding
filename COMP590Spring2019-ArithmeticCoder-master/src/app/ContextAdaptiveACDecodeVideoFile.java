package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class ContextAdaptiveACDecodeVideoFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/contextvideo.dat";
		String output_file_name = "data/test13.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[511];
		for (int i=-255; i<256; i++) {
			symbols[i+255] = i;
		}

		// Create 511 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[511];
		
		for (int i=0; i<511; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		
		Integer[] originals = new Integer[num_symbols];

		for (int i=0; i<num_symbols; i++) {
			int sym = decoder.decode(model, bit_source);
			if(i < 4096) {
				originals[i] = sym;
			}
			else {
				originals[i] = originals[i-4096] - sym;
			}
			// Update model used
			model.addToCount(indexOf(sym, symbols));
			
			// Set up next model to use.
			model = models[indexOf(sym, symbols)];
		}
		
		for(int i = 0; i < num_symbols; i++) {
			fos.write(originals[i]);
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
	
	public static int indexOf(int x, Integer[] symbols) {
		for(int i = 0; i < symbols.length; i++) {
			if (symbols[i] == x) {
				return i;
			}
		}
		return -256;
	}
}
