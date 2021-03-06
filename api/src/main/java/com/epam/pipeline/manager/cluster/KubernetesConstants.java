/*
 * Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.pipeline.manager.cluster;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Mariia_Zueva on 5/2/2017.
 */
public final class KubernetesConstants {

    protected static final String POD_SUCCEEDED_PHASE = "Succeeded";
    protected static final String POD_FAILED_PHASE = "Failed";
    protected static final String NODE_LOST = "NodeLost";
    protected static final String POD_UNSCHEDULABLE = "Unschedulable";
    protected static final String RUN_ID_LABEL = "runid";
    protected static final String AWS_REGION_LABEL = "aws_region";
    protected static final String POD_RUNNING_PHASE = "Running";
    public static final String POD_WORKER_NODE_LABEL = "cluster_id";
    public static final String PAUSED_NODE_LABEL = "Paused";

    protected static final String SYSTEM_NAMESPACE = "kube-system";
    protected static final String POD_NODE_SELECTOR = "spec.nodeName";

    // node condition types
    protected static final String OUT_OF_DISK = "OutOfDisk";
    protected static final String READY = "Ready";
    protected static final String MEMORY_PRESSURE = "MemoryPressure";
    protected static final String DISK_PRESSURE = "DiskPressure";
    protected static final String NETWORK_UNAVAILABLE = "NetworkUnavailable";
    protected static final String CONFIG_OK = "ConfigOK";
    // node condition statuses
    protected static final String TRUE = "True";
    protected static final String FALSE = "False";
    protected static final String UNKNOWN = "Unknown";

    protected static final Set<String> NODE_CONDITION_TYPES =
            Stream.of(OUT_OF_DISK, READY, MEMORY_PRESSURE, DISK_PRESSURE, NETWORK_UNAVAILABLE, CONFIG_OK)
                    .collect(Collectors.toSet());

    protected static final Set<String> NODE_OUT_OF_ORDER_REASONS = new HashSet<>();

    static {
        NODE_OUT_OF_ORDER_REASONS.add("KubeletOutOfDisk");
        NODE_OUT_OF_ORDER_REASONS.add("NodeStatusUnknown");
    }

    private KubernetesConstants() {
        //no op
    }
}
