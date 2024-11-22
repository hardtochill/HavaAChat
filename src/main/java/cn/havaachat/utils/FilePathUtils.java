package cn.havaachat.utils;

import cn.havaachat.constants.FileConstants;

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
        if(!baseFolderPath.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
        return stringBuilder.append(FileConstants.AVATAR_FOLDER_NAME).toString();
    }

    /**
     * 生成存储版本文件的文件夹路径
     * @param baseFolderPath
     * @return
     */
    public static String generateAppUpdateFileFolderPath(String baseFolderPath){
        StringBuilder stringBuilder = new StringBuilder(baseFolderPath);
        if(!baseFolderPath.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
        return stringBuilder.append(FileConstants.APP_UPDATE_FOLDER_NAME).toString();
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
        if(!folderPath.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
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
        if(!folderPath.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
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
        if(!folderPath.endsWith(FileConstants.PATH_SEPARATOR)){
            stringBuilder.append(FileConstants.PATH_SEPARATOR);
        }
        return stringBuilder.append(id).append(FileConstants.APP_NAME_SUFFIX).toString();
    }
}
