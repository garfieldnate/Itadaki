package examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;

import org.itadaki.jisx0213.EUCJISX0213Charset;
import org.itadaki.jisx0213.EUCJISX0213CharsetDecoder;


/**
 * Demonstration decoder. Limited by heap size
 */
public class Decoder {

	/**
	 * Decodes the file given as the first argument from EUC-JISX0213 to UTF-8
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException {

		FileInputStream stream = new FileInputStream (new File (args[0]));
		FileChannel channel = stream.getChannel();
		MappedByteBuffer buffer = channel.map (FileChannel.MapMode.READ_ONLY, 0, channel.size());

		CharsetDecoder decoder = new EUCJISX0213CharsetDecoder (new EUCJISX0213Charset());
		CharBuffer output = decoder.decode (buffer);

		System.out.print (output);

	}

}
