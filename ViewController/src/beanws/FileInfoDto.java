package beanws;


public class FileInfoDto {
    private String fileName;
    private String fileType;
    private String fileSize;


    public FileInfoDto(String fileName, String fileType, String fileSize) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSize() {
        return fileSize;
    }

    public FileInfoDto() {
        super();
    }
}
