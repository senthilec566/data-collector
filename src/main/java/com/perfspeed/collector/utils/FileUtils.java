package com.perfspeed.collector.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class FileUtils {
	/**
	 * Make sure explicitly close the InputStream
	 * 
	 * use the API with try-with-resources
	 * 
	 * try(InputStream is = FileUtils.getInputStream(pathToFile)){
	 * 		//any user operation on InputStream
	 * }
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static InputStream getInputStream(String filePath) throws IOException {
		return Files.newInputStream(Paths.get(filePath), StandardOpenOption.READ);
	}
}
