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

import React from 'react';
import {computed, observable} from 'mobx';
import {Provider, observer} from 'mobx-react';
import PropTypes from 'prop-types';
import {Alert, Checkbox, message, Modal, Row} from 'antd';
import CommitRunForm from '../logs/forms/CommitRunForm';
import {PipelineRunCommitCheck} from '../../../models/pipelines/PipelineRunCommitCheck';
import PipelineRunCommit from '../../../models/pipelines/PipelineRunCommit';
import StopPipeline from '../../../models/pipelines/StopPipeline';
import moment from 'moment';

export function canStopRun (run) {
  // Checks only run state, not user permissions
  const {status, commitStatus} = run;
  return status.toLowerCase() === 'running' && (commitStatus || '').toLowerCase() !== 'committing';
}

export function canCommitRun (run) {
  // Checks only run state, not user permissions
  const {podIP} = run;
  return canStopRun(run) &&
    podIP && !(run.nodeCount > 0) &&
    !(run.parentRunId && run.parentRunId > 0);
}

export function stopRun (parent, callback) {
  if (!parent) {
    console.warn('"stopRun" function should be called with parent component passed to arguments:');
    console.warn('"stopRun(parent)"');
    console.warn('Parent component should be marked with @runPipelineActions');
    throw new Error('"stopRun" function should be called with parent component passed to arguments:');
  }
  const {localization, dockerRegistries} = parent.props;
  return function (run) {
    return stopRunFn(run, callback, {localization, dockerRegistries});
  };
}

async function stopPipeline (run) {
  const hide = message.loading('Terminating run...', 0);
  const request = new StopPipeline(run.id);
  await request.send(
    {
      endDate: moment.utc().format('YYYY-MM-DD HH:mm:ss.SSS'),
      status: 'STOPPED'
    }
  );
  hide();
  return request.error;
}

async function commitRunAndStop (run, payload) {
  const {newImageName, registryToCommitId} = payload;
  const request = new PipelineRunCommit(run.id);
  const hide = message.loading('Committing...', -1);
  await request.send({
    deleteFiles: false,
    newImageName,
    registryToCommitId,
    stopPipeline: true
  });
  hide();
  return request.error;
}

function stopRunFn (run, callback, stores) {
  let content;
  const canCommitRunResult = canCommitRun(run);
  const onOkClicked = async (close, resolve) => {
    let validationResult = true;
    if (content) {
      validationResult = await content.validate();
    }
    if (validationResult) {
      let error;
      if (validationResult.persistState && canCommitRunResult) {
        error = await commitRunAndStop(run, validationResult.values);
      } else {
        error = await stopPipeline(run);
      }
      if (error) {
        message.error(error, 5);
      }
      close();
      callback && callback();
      resolve(!error);
    }
  };
  return new Promise((resolve) => {
    Modal.confirm({
      title: `Stop ${run.podId}?`,
      width: '50%',
      okText: 'STOP',
      okType: 'danger',
      content: (
        <Provider {...stores}>
          <StopRunConfirmation
            ref={(el) => {
              content = el;
            }}
            runId={run.id}
            canCommitRun={canCommitRunResult}
            dockerImage={run.dockerImage} />
        </Provider>
      ),
      onOk (close) {
        onOkClicked(close, resolve);
      },
      onCancel () {
        resolve();
      }
    });
  });
}

@observer
class StopRunConfirmation extends React.Component {

  static propTypes = {
    runId: PropTypes.number,
    canCommitRun: PropTypes.bool,
    dockerImage: PropTypes.string
  };

  state = {
    persistState: false
  };

  _commitRunForm;

  @observable
  _commitCheck = null;

  fetchCommitCheck = async () => {
    this._commitCheck = new PipelineRunCommitCheck(this.props.runId);
    await this._commitCheck.fetch();
  };

  @computed
  get commitCheck () {
    if (!this._commitCheck || !this._commitCheck.loaded) {
      return true;
    }

    return !!this._commitCheck.value;
  }

  onChange = (e) => {
    this.setState({
      persistState: e.target.checked
    }, () => {
      if (e.target.checked) {
        this.fetchCommitCheck();
      }
    });
  };

  validate = async () => {
    if (this.state.persistState) {
      if (this._commitRunForm) {
        const result = await this._commitRunForm.validate();
        if (result) {
          return {
            persistState: true,
            values: result
          };
        }
      }
      return null;
    }
    return {
      persistState: false
    };
  };

  onInitializeForm = (component) => {
    this._commitRunForm = component;
  };

  render () {
    return (
      <div>
        <Row type="flex" style={{marginBottom: 5}}>
          <Alert
            type="info"
            showIcon
            message="Once a run is stopped - all local data will be deleted (that is not stored within shared data storages)" />
        </Row>
        {
          this.props.canCommitRun &&
          <Row type="flex" style={{marginBottom: 5, fontWeight: 'bold'}}>
            Do you want to persist current docker image state?
          </Row>
        }
        {
          this.props.canCommitRun &&
          <Row type="flex" style={{marginBottom: 5}}>
            <Checkbox
              checked={this.state.persistState}
              onChange={this.onChange}>
              Persist current docker image state
            </Checkbox>
          </Row>
        }
        {
          this.state.persistState && this.props.canCommitRun &&
          <CommitRunForm
            onInitialized={this.onInitializeForm}
            visible={this.state.persistState}
            stopPipeline
            commitCheck={this.commitCheck}
            pending={this._commitCheck && this._commitCheck.pending}
            displayStopPipelineSelector={false}
            displayDeleteRuntimeFilesSelector={false}
            defaultDockerImage={this.props.dockerImage} />
        }
      </div>
    );
  }
}
