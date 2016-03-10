/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package tds.student.diagnostic.services.impl;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tds.student.diagnostic.domain.LocalSystem;
import tds.student.diagnostic.domain.Rating;
import tds.student.diagnostic.domain.Volume;
import tds.student.diagnostic.services.DiagnosticSystemService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiagnosticSystemServiceImpl implements DiagnosticSystemService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticSystemServiceImpl.class);

    @Value("${diagnostic.volume.minimumPercentFree:10}")
    private Integer minimumPercentFree;

    public LocalSystem getSystem() {

        LocalSystem localSystem = new LocalSystem(Rating.IDEAL);

        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();

        localSystem.setOperatingSystemName(bean.getName());
        localSystem.setArchitecture(bean.getArch());
        localSystem.setAvailableProcessors(bean.getAvailableProcessors());

        localSystem.setJavaProcessCpuLoad(bean.getProcessCpuLoad());
        localSystem.setSystemCpuLoad(bean.getSystemCpuLoad());
        localSystem.setSystemLoadAverage(bean.getSystemLoadAverage());

        localSystem.setTotalPhysicalMemory(bean.getTotalPhysicalMemorySize());
        localSystem.setFreePhysicalMemory(bean.getFreePhysicalMemorySize());
        localSystem.setFreeSwapSpace(bean.getFreeSwapSpaceSize());

        localSystem.setJvmTotalMemory(Runtime.getRuntime().totalMemory());
        localSystem.setJvmFreeMemory(Runtime.getRuntime().freeMemory());
        localSystem.setJvmMaxMemory(Runtime.getRuntime().maxMemory());

        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        List<Volume> volumes = new ArrayList<>();

        for (File root : roots) {
            double doubleFree = root.getFreeSpace();
            double doubleTotal = root.getTotalSpace();
            double percentFree = doubleFree / doubleTotal;

            Volume volume = new Volume(
                    root.getAbsolutePath(),
                    root.getFreeSpace(),
                    root.getTotalSpace(),
                    root.getUsableSpace(),
                    percentFree);

            if (percentFree * 100 < minimumPercentFree) {
                volume.setRating(Rating.WARNING);
                volume.setWarning("Current free space is below the minimum percent free of " + minimumPercentFree + "%");
            }
            volumes.add(volume);
        }
        localSystem.setVolumes(volumes);

        return localSystem;
    }

}