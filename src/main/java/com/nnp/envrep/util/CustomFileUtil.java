package com.nnp.envrep.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.nnp.envrep.exception.EnvReplEngineEx;

import lombok.extern.slf4j.Slf4j;

/**
 * @author AC (AC)
 *
 */
@Slf4j
public class CustomFileUtil {

	private static final int BUFFER_SIZE = 4096;

    public void makeFolder(String baseFolderPath, String folderName) throws IOException {
        File rootFolder = new File(baseFolderPath);
        File[] folderList = rootFolder.listFiles();
        if (folderList == null || folderList.length == 0) {
            createFolder(baseFolderPath, folderName); return;
        }
        for (File subFolder : folderList) {
            if (subFolder.isDirectory() && !subFolder.getName().equalsIgnoreCase(folderName)) {
                createFolder(baseFolderPath, folderName); return;
            }
        }
    }
    private void createFolder(String baseFolderPath, String folderName) throws IOException {
        Path folderPath = Path.of(baseFolderPath, folderName);
        FileUtils.forceMkdir(folderPath.toFile());
    }
	public void writeFile(InputStream is, File f) throws EnvReplEngineEx {
		try {
			FileUtils.touch(f);
			FileOutputStream outputStream = new FileOutputStream(f);
			IOUtils.copy(is, outputStream);
			is.close();
			outputStream.close();
		} catch (IOException e) {
			log.error("exception in CustomFileUtil-> writeFile()::", e);
			throw new EnvReplEngineEx(e.getMessage(),e);
		}

	}

	public void copyDirectory(String source, String dest) throws EnvReplEngineEx {
		try {
			
			File srcFile = new File(source);
			File destFile = new File(dest);

			FileUtils.copyDirectory(srcFile, destFile);
		} catch (IOException e) {
			log.error("exception in CustomFileUtil-> copyDirectory()::", e);
			throw new EnvReplEngineEx(e.getLocalizedMessage(),e);
		}

	}

	

	public void deleteFile(String path) {

		try {
			Thread.sleep(1000);
			File f = new File(path);
			if(f.exists()) {
				FileUtils.forceDelete(new File(path));
				log.info("CustomFileUtil-> deleteFile() :: file deleted successfull - "+path);
			}else {
				log.info("CustomFileUtil-> deleteFile() :: no file created - "+path);
			}
			
		} catch (IOException e) {
			log.debug("exception in CustomFileUtil-> deleteFile()::", e);
			log.error("exception in CustomFileUtil-> deleteFile()::"+e.getMessage());
			throw new EnvReplEngineEx(e.getLocalizedMessage(), e);

		} catch(InterruptedException e) {
			log.error("exception in CustomFileUtil-> deleteFile()::", e);
			Thread.currentThread().interrupt();
		}

	}

	

	public boolean renameFile(String pomLocation, String fromFileName, String toFileName) {
		File fromFile = new File(pomLocation+fromFileName);
		File toFile = new File(pomLocation+toFileName);
		return fromFile.renameTo(toFile);
	}

}
