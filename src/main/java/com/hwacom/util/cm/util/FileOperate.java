/**
 * 
 */
package com.hwacom.util.cm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Douglas Feng
 *
 */
public class FileOperate {

	private static final Logger log = LoggerFactory.getLogger(FileOperate.class);

	public FileOperate() {
	}

	/**
	 * 新建目錄
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 */
	public static void newFolder(String folderPath) {
		try {
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.mkdir();
			}
		} catch (Exception e) {
			log.error("新建目錄操作出錯" + "\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 新建文件
	 * 
	 * @param filePathAndName
	 *            String 文件路徑及名稱 如c:/fqf.txt
	 * @param fileContent
	 *            String 文件內容
	 * @return boolean
	 */
	public static void newFile(String filePathAndName, String fileContent) {

		try {
			String filePath = filePathAndName.substring(0, filePathAndName.lastIndexOf("/") + 1);
			File file = new File(filePath);
			if(!file.exists()) {
				file.mkdirs();
			}
			File myFilePath = new File(filePathAndName);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath);
			PrintWriter myFile = new PrintWriter(resultFile);
			myFile.println(fileContent);
			resultFile.close();

		} catch (Exception e) {
			log.error("新建目錄操作出錯" + "\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 刪除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路徑及名稱 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			java.io.File myDelFile = new java.io.File(filePath);
			myDelFile.delete();
		} catch (Exception e) {
			log.error("Delete File Error:\n{}", e.getMessage());
			e.printStackTrace();
		} finally {
			
		}
	}

	/**
	 * 刪除文件夾
	 * 
	 * @param filePathAndName
	 *            String 文件夾路徑及名稱 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 刪除完裡面所有內容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 刪除空文件夾

		} catch (Exception e) {
			log.error("刪除文件夾操作出錯" + "\n" + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 刪除文件夾裡面的所有文件
	 * 
	 * @param path
	 *            String 文件夾路徑 如 c:/fqf
	 */
	public static void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先刪除文件夾裡面的文件
				delFolder(path + "/" + tempList[i]);// 再刪除空文件夾
			}
		}
	}

	/**
	 * 複製單個文件
	 * 
	 * @param oldPath
	 *            String 原文件路徑 如：c:/fqf.txt
	 * @param newPath
	 *            String 複製後路徑 如：f:/fqf.txt
	 * @return boolean
	 * @throws IOException 
	 */
	public static void copyFile(String oldPath, String newPath) throws IOException {
		log.debug("oldpath=[{}], newpath=[{}]", oldPath, newPath);
		int bytesum = 0;
		int byteread = 0;
		File file = new File(newPath.substring(0, newPath.lastIndexOf("/") + 1));
		if(!file.exists()) {
			file.mkdirs();
		}
		File oldfile = new File(oldPath);
		if (oldfile.exists()) { // 文件存在時
			InputStream inStream = new FileInputStream(oldPath); // 讀入原文件
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1444];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; // 字節數 文件大小
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
		}

	}

	/**
	 * 複製整個文件夾內容
	 * 
	 * @param oldPath
	 *            String 原文件路徑 如：c:/fqf
	 * @param newPath
	 *            String 複製後路徑 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夾不存在 則建立新文件夾
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夾
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			log.error("複製整個文件夾內容操作出錯" + "\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 移動文件到指定目錄
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 * @throws IOException 
	 */
	public static void moveFile(String oldPath, String newPath) throws IOException {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * 創建多級目錄操作 方法名：CreateFolder 參 數： String FolderPath //要創建的目錄 返回值：boolean類型 功
	 * 能：根據用戶指定的多級目錄進行創建 備 註：
	 */
	public static boolean newMultiFolder(String FolderPath) {
		File file1 = new File(FolderPath);
		// 檢查參數
		if (FolderPath == null || FolderPath.length() == 0) {
			return false;
		}
		if (FolderPath.indexOf("/") == -1 && FolderPath.indexOf("\\") == -1) {
			return false;
		}
		if (file1.exists()) {
			return true;
		}
		// 首次處理傳過來的字符串
		String str_temp = "";
		for (int i = 0; i < FolderPath.length(); i++) {
			if (i < FolderPath.length()) {
				if (FolderPath.substring(i, i + 1).equals("\\")) {
					str_temp += "/";
				} else {
					str_temp += FolderPath.substring(i, i + 1);
				}
			}
		}
		// 通過"/"，那字符串拆分
		String Str_P[] = str_temp.split("/");
		String Str_Create = "";
		for (int i = 0; i < Str_P.length; i++) {
			Str_Create += Str_P[i] + "/";
			File file = new File(Str_Create);
			if (!file.exists()) {
				if (!file.mkdir()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 移動文件到指定目錄
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	public static void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}
}
