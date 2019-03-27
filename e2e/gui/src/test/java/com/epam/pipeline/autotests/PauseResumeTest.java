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
package com.epam.pipeline.autotests;

import com.epam.pipeline.autotests.ao.ToolPageAO;
import com.epam.pipeline.autotests.ao.ToolTab;
import com.epam.pipeline.autotests.mixins.Tools;
import com.epam.pipeline.autotests.utils.C;
import com.epam.pipeline.autotests.utils.TestCase;
import com.epam.pipeline.autotests.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Supplier;

import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.epam.pipeline.autotests.ao.LogAO.InstanceParameters.getParameterValueLink;
import static com.epam.pipeline.autotests.ao.LogAO.InstanceParameters.parameterWithName;
import static com.epam.pipeline.autotests.ao.NodePage.labelWithType;
import static com.epam.pipeline.autotests.ao.NodePage.mainInfo;
import static com.epam.pipeline.autotests.utils.Conditions.textMatches;

public class PauseResumeTest extends AbstractSeveralPipelineRunningTest implements Tools {

    private final String tool = C.TESTING_TOOL_NAME;
    private final String registry = C.DEFAULT_REGISTRY;
    private final String group = C.DEFAULT_GROUP;
    private final String testFileName = "test.txt";
    private final String testFileContent = "test";
    private final String ipField = "IP";

    private String endpoint;

    @BeforeClass
    @AfterClass(alwaysRun = true)
    public void fallBackToDefaultToolSettings() {
        open(C.ROOT_ADDRESS);
        fallbackToToolDefaultState(registry, group, tool);
    }

    @AfterMethod(alwaysRun = true)
    public void refresh() {
        open(C.ROOT_ADDRESS);
    }

    @Test
    @TestCase({"EPMCMBIBPC-2309"})
    public void pauseAndResumeValidation() {
        final String priceType = "On-demand";
        tools()
                .perform(registry, group, tool, ToolTab::runWithCustomSettings)
                .setPriceType(priceType)
                .launchTool(this, Utils.nameWithoutGroup(tool))
                .log(getLastRunId(), log ->
                        log.waitForSshLink()
                                .inAnotherTab(logTab -> logTab
                                        .ssh(shell -> shell.execute(
                                                String.format("echo '%s' > %s", testFileContent, testFileName)))
                                )
                                .waitForPauseButton()
                                .pause(getToolName())
                                .assertPausingFinishedSuccessfully()
                                .instanceParameters(parameters -> {
                                    final String nodeIp = $(parameterWithName(ipField)).text().split(" \\(")[0];
                                    final String ipHyperlink = getParameterValueLink(ipField);
                                    parameters.inAnotherTab(nodeTab ->
                                            checkNodePage(() -> nodeTab.messageShouldAppear(
                                                    String.format("The node '%s' was not found or was removed",  nodeIp)
                                            ), ipHyperlink)
                                    );
                                })
                                .resume(getToolName())
                                .assertResumingFinishedSuccessfully()
                                .waitForSshLink()
                                .inAnotherTab(logTab -> logTab
                                        .ssh(shell -> shell
                                                .execute(String.format("cat %s", testFileName))
                                                .assertOutputContains(testFileContent))
                                )
                                .instanceParameters(parameters -> {
                                    final String nodeIp = $(parameterWithName(ipField)).text().split(" \\(")[0];
                                    final String expectedTitle = String.format("^Node: %s*", nodeIp);
                                    final String ipHyperlink = getParameterValueLink(ipField);
                                    parameters.inAnotherTab(nodeTab ->
                                            checkNodePage(() ->
                                                    nodeTab
                                                        .ensure(mainInfo(), have(textMatches(expectedTitle)))
                                                        .ensure(labelWithType("RUNID"), visible)
                                                        .ensure(labelWithType("PIPELINE-INFO"), visible), ipHyperlink)
                                    );
                                })
                );
    }

    @Test
    @TestCase({"EPMCMBIBPC-2626"})
    public void pauseAndResumeEndpointValidation() {
        final String priceType = "On-demand";
        endpoint = tools()
                .perform(registry, group, tool, ToolTab::runWithCustomSettings)
                .setPriceType(priceType)
                .launchTool(this, Utils.nameWithoutGroup(tool))
                .show(getLastRunId())
                .clickEndpoint()
                .getEndpoint();
        open(C.ROOT_ADDRESS);
        runsMenu()
                .log(getLastRunId(), log -> log
                        .waitForPauseButton()
                        .pause(getToolName())
                        .assertPausingFinishedSuccessfully()
                        .inAnotherTab(nodeTab ->
                                checkNodePage(() ->
                                        new ToolPageAO(endpoint)
                                                .assertPageTitleIs("404 Not Found"),
                                        endpoint)
                        )
                        .resume(getToolName())
                        .assertResumingFinishedSuccessfully()
                        .inAnotherTab(nodeTab ->
                                checkNodePage(() -> new ToolPageAO(endpoint).validateEndpointPage(), endpoint)
                        )
                );
    }

    private void checkNodePage(final Supplier<?> nodePage, final String ipHyperlink) {
        open(ipHyperlink);
        nodePage.get();
    }

    private String getToolName() {
        final String[] toolAndGroup = tool.split("/");
        return toolAndGroup[toolAndGroup.length - 1];
    }
}