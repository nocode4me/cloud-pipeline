/*
 * Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.pipeline.manager.region;

import com.epam.pipeline.controller.vo.CloudRegionVO;
import com.epam.pipeline.entity.region.AbstractCloudRegion;
import com.epam.pipeline.entity.region.AbstractCloudRegionCredentials;
import com.epam.pipeline.entity.region.AwsRegion;
import com.epam.pipeline.entity.region.CloudProvider;
import com.epam.pipeline.entity.region.GCPRegion;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GCPCloudRegionManagerTest extends AbstractCloudRegionManagerTest {

    private static final String GCP_PROJECT = "Project";
    private static final String GCP_PROJECT_CHANGED = "Bio";
    private static final String SSH_PUB_PATH = "/ssh.pub";

    @Override
    AbstractCloudRegion commonRegion() {
        final GCPRegion region = new GCPRegion();
        region.setId(ID);
        region.setName(REGION_NAME);
        region.setRegionCode(validRegionId());
        region.setOwner("owner");
        region.setCreatedDate(new Date());
        region.setProject(GCP_PROJECT);
        region.setAuthFile(SSH_PUB_PATH);
        return region;
    }

    @Override
    CloudRegionVO.CloudRegionVOBuilder createRegionBuilder() {
        return updateRegionBuilder()
                .regionCode(validRegionId())
                .sshPublicKeyPath(SSH_PUB_PATH)
                .project(GCP_PROJECT);
    }

    @Override
    CloudRegionVO.CloudRegionVOBuilder updateRegionBuilder() {
        return CloudRegionVO.builder()
                .name(REGION_NAME)
                .project(GCP_PROJECT_CHANGED)
                .sshPublicKeyPath(SSH_PUB_PATH)
                .provider(CloudProvider.GCP);
    }

    @Override
    AbstractCloudRegionCredentials credentials() {
        return null;
    }

    @Override
    String validRegionId() {
        return "us-central";
    }

    @Override
    void assertRegionEquals(final AbstractCloudRegion expectedRegion, final AbstractCloudRegion actualRegion) {
        assertThat(actualRegion, instanceOf(GCPRegion.class));
        final GCPRegion expectedGcpRegion = (GCPRegion) expectedRegion;
        final GCPRegion actualGcpRegion = (GCPRegion) actualRegion;
        assertThat(expectedGcpRegion.getRegionCode(), is(actualGcpRegion.getRegionCode()));
        assertThat(expectedGcpRegion.getName(), is(actualGcpRegion.getName()));
        assertThat(expectedGcpRegion.getAuthFile(), is(actualGcpRegion.getAuthFile()));
        assertThat(expectedGcpRegion.getProject(), is(actualGcpRegion.getProject()));
        assertThat(expectedGcpRegion.getSshPublicKeyPath(), is(actualGcpRegion.getSshPublicKeyPath()));
    }

    @Override
    List<CloudRegionHelper> helpers() {
        return Collections.singletonList(new GCPRegionHelper(messageHelper));
    }

    @Override
    CloudProvider defaultProvider() {
        return CloudProvider.GCP;
    }
}
