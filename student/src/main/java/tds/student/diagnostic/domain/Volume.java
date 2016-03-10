package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Volume extends AbstractStatus {


    private String absolutePath;
    private Long totalSpace;
    private Long freeSpace;
    private Long usableSpace;
    private Double percentFree;

    public Volume(String absolutePath, Long freeSpace, Long totalSpace, Long usableSpace , Double percentFree) {
        super(Rating.IDEAL);
        this.absolutePath = absolutePath;
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;
        this.usableSpace = usableSpace;
        this.percentFree = percentFree;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(Long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public Long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(Long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public Double getPercentFree() {
        return percentFree;
    }

    public void setPercentFree(Double percentFree) {
        this.percentFree = percentFree;
    }
}
