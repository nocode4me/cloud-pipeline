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
import PropTypes from 'prop-types';
import {inject, observer} from 'mobx-react';
import {computed} from 'mobx';
import {
  ContextualPreferenceLoad,
  ContextualPreferenceUpdate,
  ContextualPreferenceDelete,
  names
} from '../../../../models/utils/ContextualPreference';
import {
  Button,
  Input,
  message,
  Row,
  Select
} from 'antd';
import LoadingView from '../../../special/LoadingView';

const valueNames = {
  allowedInstanceTypes: 'allowedInstanceTypes',
  allowedToolInstanceTypes: 'allowedToolInstanceTypes',
  allowedPriceTypes: 'allowedPriceTypes'
};

@inject((stores, props) => {
  const loadPreference = (field) => {
    if (props.resourceId && props.level) {
      return {
        [field]: new ContextualPreferenceLoad(props.level, names[field], props.resourceId)
      };
    }
    return {};
  };
  return {
    ...loadPreference(valueNames.allowedInstanceTypes),
    ...loadPreference(valueNames.allowedToolInstanceTypes),
    ...loadPreference(valueNames.allowedPriceTypes)
  };
})
@observer
export default class InstanceTypesManagementForm extends React.Component {

  static propTypes = {
    level: PropTypes.oneOf(['USER', 'TOOL', 'ROLE']),
    resourceId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
  };

  state = {
    operationInProgress: false
  };

  @computed
  get pending () {
    return this.valuePending(valueNames.allowedPriceTypes) ||
      this.valuePending(valueNames.allowedInstanceTypes) ||
      this.valuePending(valueNames.allowedToolInstanceTypes);
  }

  valuePending = (field) => {
    return this.props[field] && this.props[field].pending;
  };

  onValueChanged = (field) => (e) => {
    const state = this.state;
    state[field] = e.target.value;
    this.setState(state);
  };

  getModified () {
    return this.valueModified(valueNames.allowedInstanceTypes) ||
      this.valueModified(valueNames.allowedToolInstanceTypes) ||
      this.valueModified(valueNames.allowedPriceTypes);
  }

  valueModified = (field) => {
    if (this.state[field] !== undefined) {
      return this.getCurrentValue(field) !== this.getInitialValue(field);
    }
    return false;
  };

  getCurrentValue = (field) => {
    if (this.state[field] !== undefined) {
      return this.state[field];
    }
    return '';
  };

  getInitialValue = (field) => {
    if (this.props[field] && this.props[field].loaded) {
      return this.props[field].value.value;
    }
    return '';
  };

  getValue = (field) => {
    if (this.state[field] !== undefined) {
      return this.state[field];
    }
    if (this.props[field] && this.props[field].loaded) {
      return this.props[field].value.value;
    }
    return '';
  };

  getPriceTypesValue = () => {
    return (this.getValue(valueNames.allowedPriceTypes) || '').split(',').filter(v => !!v);
  };

  onPriceTypeChanged = (e) => {
    const value = e.join(',');
    if (value !== this.state[valueNames.allowedPriceTypes]) {
      this.setState({
        [valueNames.allowedPriceTypes]: value
      });
    }
  };

  valueInputDecorator = (field) =>
    <Input
      style={{flex: 1}}
      value={this.getValue(field)}
      onChange={this.onValueChanged(field)} />;

  operationWrapper = (fn) => (...opts) => {
    this.setState({
      operationInProgress: true
    }, async () => {
      await fn(...opts);
      this.setState({
        operationInProgress: false
      });
    });
  };

  applyValue = async (field) => {
    if (this.state[field] !== undefined) {
      const request = this.state[field]
        ? new ContextualPreferenceUpdate()
        : new ContextualPreferenceDelete(names[field], this.props.level, this.props.resourceId);
      this.state[field]
        ? await request.send({
          name: names[field],
          value: this.state[field] || undefined,
          type: 'STRING',
          resource: {
            level: this.props.level,
            resourceId: this.props.resourceId
          }
        })
        : await request.fetch();
      if (request.error) {
        return request.error;
      }
    }
    return null;
  };

  reloadValue = async (field) => {
    if (this.props[field]) {
      await this.props[field].fetch();
    }
  };

  onApplyClicked = async () => {
    const hide = message.loading('Updating launch options...', 0);
    const results = [];
    results.push(await this.applyValue(valueNames.allowedInstanceTypes));
    results.push(await this.applyValue(valueNames.allowedToolInstanceTypes));
    results.push(await this.applyValue(valueNames.allowedPriceTypes));
    const errors = results.filter(r => !!r);
    if (errors.length) {
      hide();
      message.error(errors.join('\n'), 5);
    } else {
      await this.reloadValue(valueNames.allowedInstanceTypes);
      await this.reloadValue(valueNames.allowedToolInstanceTypes);
      await this.reloadValue(valueNames.allowedPriceTypes);
      this.setState({
        [valueNames.allowedInstanceTypes]: undefined,
        [valueNames.allowedToolInstanceTypes]: undefined,
        [valueNames.allowedPriceTypes]: undefined
      }, hide);
    }
  };

  render () {
    if (!this.props.resourceId || !this.props.level) {
      return null;
    }
    if (this.pending || this.state.operationInProgress) {
      return <LoadingView />;
    }
    return (
      <Row type="flex" style={{flex: 1, overflow: 'auto'}}>
        <div style={{padding: 2, width: '100%'}}>
          {
            this.props.level !== 'TOOL' &&
            <Row type="flex" style={{marginTop: 5}}>
              <b>Allowed instance types mask</b>
            </Row>
          }
          {
            this.props.level !== 'TOOL' &&
            <Row type="flex">
              {this.valueInputDecorator(valueNames.allowedInstanceTypes)}
            </Row>
          }
          <Row type="flex" style={{marginTop: 5}}>
            <b>Allowed tool instance types mask</b>
          </Row>
          <Row type="flex">
            {this.valueInputDecorator(valueNames.allowedToolInstanceTypes)}
          </Row>
          <Row type="flex" style={{marginTop: 5}}>
            <b>Allowed price types</b>
          </Row>
          <Row type="flex">
            <Select
              mode="multiple"
              style={{flex: 1}}
              value={this.getPriceTypesValue()}
              onChange={this.onPriceTypeChanged}>
              <Select.Option
                key="on_demand"
                value="on_demand">
                On demand
              </Select.Option>
              <Select.Option
                key="spot"
                value="spot">
                Spot
              </Select.Option>
            </Select>
          </Row>
          <Row type="flex" justify="end" style={{marginTop: 10}}>
            <Button
              type="primary"
              onClick={this.operationWrapper(this.onApplyClicked)}
              disabled={!this.getModified() || this.state.operationInProgress}>
              APPLY
            </Button>
          </Row>
        </div>
      </Row>
    );
  }
}
