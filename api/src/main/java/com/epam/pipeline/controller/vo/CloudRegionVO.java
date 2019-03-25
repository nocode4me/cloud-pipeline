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

package com.epam.pipeline.controller.vo;

import com.epam.pipeline.entity.region.AzurePolicy;
import com.epam.pipeline.entity.region.CloudProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudRegionVO {
    private Long id;
    @JsonProperty(value = "regionId")
    private String regionCode;
    private String name;
    @JsonProperty(value = "default")
    private boolean isDefault;
    private CloudProvider provider;

    private String corsRules;

    //AWS Fields
    private String policy;
    private String kmsKeyId;
    private String kmsKeyArn;
    private String profile;
    private String sshKeyName;
    private String tempCredentialsRole;
    private Integer backupDuration;
    private boolean versioningEnabled;

    //Azure Fields
    private String storageAccount;
    private String storageAccountKey;
    private String resourceGroup;
    private AzurePolicy azurePolicy;
    private String subscription;
    private String authFile;
    private String sshPublicKeyPath;
    private String meterRegionName;
    private String azureApiUrl;
    private String priceOfferId;

    //GCP
    private String project;
}
