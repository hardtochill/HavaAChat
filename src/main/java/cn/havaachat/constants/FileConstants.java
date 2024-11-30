package cn.havaachat.constants;

/**
 * 文件常量类
 */
public class FileConstants {
    // 路径分隔符
    public static final String PATH_SEPARATOR = "/";
    // 文件名的单词之间分隔符
    public static final String NAME_SEPARATOR = "_";
    // 头像文件所在文件夹名称
    public static final String AVATAR_FOLDER_NAME = "avatar";
    // 图像文件后缀
    public static final String IMAGE_SUFFIX = ".png";
    // 缩略图像文件后缀
    public static final String COVER_IMAGE_SUFFIX = "_cover.png";
    // 版本文件目录
    public static final String APP_UPDATE_FOLDER_NAME= "app";
    // 版本文件后缀
    public static final String APP_NAME_SUFFIX = ".exe";
    // 版本文件前缀
    public static final String APP_NAME_PREFIX = "HaveAChat.";
    // 图片文件后缀
    public static final String[] IMAGE_SUFFIX_ARRAY = {".jpeg",".jpg",".png",".gif",".bmp",".webp"};
    // 视频文件后缀
    public static final String[] VIDEO_SUFFIX_ARRAY= {".mp4",".avi",".rmvb",".mkv",".mov"};
    // 将B转成MB
    public static final Long FILE_SIZE_MB = 1024*1024L;
    // 存储聊天文件的文件夹中的时间格式
    public static final String DATE_PATTERN = "yyyyMM";
}
