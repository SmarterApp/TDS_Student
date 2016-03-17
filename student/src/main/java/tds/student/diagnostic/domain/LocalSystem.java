package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalSystem extends AbstractStatus {

    String operatingSystemName;
    String architecture;
    Integer availableProcessors;

    // 1 is 100%
    Double javaProcessCpuLoad;
    Double systemCpuLoad;
    Double systemLoadAverage;

    // Good
    Long totalPhysicalMemory;
    Long freePhysicalMemory;
    Long freeSwapSpace;

    Long jvmTotalMemory;
    Long jvmFreeMemory;
    Long jvmMaxMemory;

    List<Volume> volumes;

    public LocalSystem(Rating rating) {
        super(rating);
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(Integer availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public Long getFreePhysicalMemory() {
        return freePhysicalMemory;
    }

    public void setFreePhysicalMemory(Long freePhysicalMemory) {
        this.freePhysicalMemory = freePhysicalMemory;
    }

    public Long getFreeSwapSpace() {
        return freeSwapSpace;
    }

    public void setFreeSwapSpace(Long freeSwapSpace) {
        this.freeSwapSpace = freeSwapSpace;
    }

    public Double getJavaProcessCpuLoad() {
        return javaProcessCpuLoad;
    }

    public void setJavaProcessCpuLoad(Double javaProcessCpuLoad) {
        this.javaProcessCpuLoad = javaProcessCpuLoad;
    }

    public Long getJvmFreeMemory() {
        return jvmFreeMemory;
    }

    public void setJvmFreeMemory(Long jvmFreeMemory) {
        this.jvmFreeMemory = jvmFreeMemory;
    }

    public Long getJvmMaxMemory() {
        return jvmMaxMemory;
    }

    public void setJvmMaxMemory(Long jvmMaxMemory) {
        this.jvmMaxMemory = jvmMaxMemory;
    }

    public Long getJvmTotalMemory() {
        return jvmTotalMemory;
    }

    public void setJvmTotalMemory(Long jvmTotalMemory) {
        this.jvmTotalMemory = jvmTotalMemory;
    }

    public String getOperatingSystemName() {
        return operatingSystemName;
    }

    public void setOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    public Double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(Double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public Double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(Double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public Long getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(Long totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
        super.updateRating(volumes);
    }
}
