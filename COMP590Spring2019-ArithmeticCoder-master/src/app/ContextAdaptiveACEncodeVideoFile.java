package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticEncoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

public class ContextAdaptiveACEncodeVideoFile {

	public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
		String input_file_name = "data/out.dat";
		String output_file_name = "data/contextvideo.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
				
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

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);
		
		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		InputStreamBitSource bit_source = new InputStreamBitSource(fis);
		
		int[] processed = new int[num_symbols];
		int[] original = new int[num_symbols];
		for(int i = 0; i < num_symbols; i++) {
			int next_symbol = bit_source.next(8);
			original[i] = next_symbol;
			if(i < 4096) {
				processed[i] = next_symbol;
			}
			else {
				processed[i] = original[i - 4096] - next_symbol;
			}
		}
				
		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		
		for(int i = 0; i < num_symbols; i++) {
			encoder.encode(processed[i], model, bit_sink);
			model.addToCount(indexOf(processed[i], symbols));
			model = models[indexOf(processed[i], symbols)];
		}
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
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
