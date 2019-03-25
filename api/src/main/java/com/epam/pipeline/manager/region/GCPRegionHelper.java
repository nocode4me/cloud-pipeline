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

import com.epam.pipeline.common.MessageConstants;
import com.epam.pipeline.common.MessageHelper;
import com.epam.pipeline.entity.region.AbstractCloudRegionCredentials;
import com.epam.pipeline.entity.region.CloudProvider;
import com.epam.pipeline.entity.region.GCPRegion;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class GCPRegionHelper implements CloudRegionHelper<GCPRegion, AbstractCloudRegionCredentials> {

    private final MessageHelper messageHelper;

    @Override
    public void validateRegion(final GCPRegion region, final AbstractCloudRegionCredentials credentials) {
        Assert.state(StringUtils.isNotBlank(region.getRegionCode()),
                messageHelper.getMessage(MessageConstants.ERROR_REGION_REGIONID_MISSING));
        Assert.state(StringUtils.isNotBlank(region.getProject()),
                messageHelper.getMessage(MessageConstants.ERROR_GCP_PROJECT_REQUIRED));
        Assert.state(StringUtils.isNotBlank(region.getSshPublicKeyPath()),
                messageHelper.getMessage(MessageConstants.ERROR_GCP_SSH_KEY_REQUIRED));
    }

    @Override
    public List<String> loadAvailableRegions() {
        //TODO:implement
        return Collections.emptyList();
    }

    @Override
    public GCPRegion mergeRegions(final GCPRegion originalRegion, final GCPRegion updatedRegion) {
        originalRegion.setName(updatedRegion.getName());
        originalRegion.setDefault(updatedRegion.isDefault());
        originalRegion.setSshPublicKeyPath(updatedRegion.getSshPublicKeyPath());
        originalRegion.setAuthFile(updatedRegion.getAuthFile());
        originalRegion.setProject(updatedRegion.getProject());
        return originalRegion;
    }

    @Override
    public CloudProvider getProvider() {
        return CloudProvider.GCP;
    }
}
