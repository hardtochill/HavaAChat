package cn.havaachat.utils;

import cn.havaachat.constants.FileConstants;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * 文件路径工具类
 */
public class FilePathUtils {
    /**
     * 生成存储头像文件的文件夹路径
     * @param baseFolderPath
     * @return
     */
    public static String generateAvatarFileFolderPath(String baseFolderPath){
        StringBuilder stringBuilder = new StringBuilder(baseFolderPath);
        paddingFilePathSuffix(baseFolderPath,stringBuilder);
        return stringBuilder.append(FileConstants.AVATAR_FOLDER_NAME).toString();
    }

    /**
     * 生成存储版本文件的文件夹路径
     * @param baseFolderPath
     * @return
     */
    public static String generateAppUpdateFileFolderPath(String baseFolderPath){
        StringBuilder stringBuilder = new StringBuilder(baseFolderPath);
        paddingFilePathSuffix(baseFolderPath,stringBuilder);
        return stringBuilder.append(FileConstants.APP_UPDATE_FOLDER_NAME).toString();
    }

    /**
     * 生成存储上传文件的文件夹路径
     * 存储上传文件的文件夹路径：baseFolder/yyyyMM
     * @param baseFolderPath
     * @param localDate
     * @return
     */
    public static String generateUploadFileFolderPath(String baseFolderPath,LocalDate localDate){
        StringBuilder stringBuilder = new StringBuilder(baseFolderPath);
        paddingFilePathSuffix(baseFolderPath,stringBuilder);
        // 取日期做文件夹名
        String folderName = localDate.format(DateTimeFormatter.ofPattern(FileConstants.DATE_PATTERN));
        return stringBuilder.append(folderName).toString();
    }
    /**
     * 生成头像文件的文件路径
     * 头像文件存储：baseFolder/AVATAR_FOLDER_NAME/id.png
     * @param folderPath
     * @param id
     * @return
     */
    public static String generateAvatarFilePath(String folderPath,String id){
        StringBuilder stringBuilder = new StringBuilder(folderPath);
        paddingFilePathSuffix(folderPath,stringBuilder);
        return stringBuilder.append(id).append(FileConstants.IMAGE_SUFFIX).toString();
    }
    /**
     * 生成缩略头像文件的文件路径
     * 头像文件存储：baseFolder/AVATAR_FOLDER_NAME/id_cover.png
     * @param folderPath
     * @param id
     * @return
     */
    public static String generateCoverAvatarFilePath(String folderPath,String id){
        StringBuilder stringBuilder = new StringBuilder(folderPath);
        paddingFilePathSuffix(folderPath,stringBuilder);
        return stringBuilder.append(id).append(FileConstants.COVER_IMAGE_SUFFIX).toString();
    }

    /**
     * 生成版本文件的文件路径
     * 版本文件存储：baseFolderPath/APP_UPDATE_FOLDER_NAME/id.exe
     * @param folderPath
     * @param id
     * @return
     */
    public static String generateAppUpdateFilePath(String folderPath,Integer id){
        StringBuilder stringBuilder = new StringBuilder(folderPath);
        paddingFilePathSuffix(folderPath,stringBuilder);
        return stringBuilder.append(id).append(FileConstants.APP_NAME_SUFFIX).toString();
    }
    /**
     * 生成存储上传文件的文件路径
     * 上传文件存储：baseFolderPath/yyyyMM/messageId_fileName + fileSuffix
     * @param folderPath
     * @param messageId
     * @param originFileName
     * @param fileSuffix
     * @return
     */
    public static String generateUploadFilePath(String folderPath,Long messageId,String originFileName,String fileSuffix){
        StringBuilder stringBuilder = new StringBuilder(folderPath);
        paddingFilePathSuffix(folderPath,stringBuilder);
        return stringBuilder.append(messageId).append(FileConstants.NAME_SEPARATOR).append(originFileName).append(fileSuffix).toString();
    }
    /**
     * 生成存储上传文件的缩略文件路径
     * 上传缩略文件存储：baseFolderPath/yyyyMM/messageId_fileName_cover.png
     * @param folderPath
     * @param messageId
     * @param originFileName
     * @return
     */
    public static String generateCoverUploadFilePath(String folderPath,Long messageId,String originFileName){
        StringBuilder stringBuilder = new StringBuilder(folderPath);
        paddingFilePathSuffix(folderPath,stringBuilder);
        return stringBuilder.append(messageId).append(FileConstants.NAME_SEPARATOR).append(originFileName).append(FileConstants.COVER_IMAGE_SUFFIX).toString();
    }
    /**
     * 填充文件分隔符
     * @param path
     */
    public static void paddingFilePathSuffix(String path,StringBuilder stringBuilder){
        if (!path.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
    }
}
