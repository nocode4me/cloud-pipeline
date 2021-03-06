# Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from pipeline import Logger, TaskStatus, PipelineAPI, StatusEntry
import argparse
import os
import time


class Task:
    def __init__(self):
        self.task_name = 'Task'

    def fail_task(self, message):
        error_text = '{} task failed: {}.'.format(self.task_name, message)
        Logger.fail(error_text, task_name=self.task_name)
        raise RuntimeError(error_text)


class Node:
    def __init__(self, run):
        self.run_id = run['id']
        self.name = run['podId']
        if 'podIP' in run:
            self.ip = run['podIP']
        else:
            self.ip = None


class WaitForNode(Task):
    def __init__(self):
        Task.__init__(self)
        self.task_name = 'WaitForNode'
        self.pipe_api = PipelineAPI(os.environ['API'], 'logs')

    def await_node_start(self, parameters, task_name, run_id):
        try:
            Logger.info('Waiting for node with parameters = {}, task: {}'.format(','.join(parameters), task_name),
                        task_name=self.task_name)
            # approximately 10 minutes
            attempts = 60
            master = self.get_node_info(parameters, task_name, run_id)
            while not master and attempts > 0:
                master = self.get_node_info(parameters, task_name, run_id)
                attempts -= 1
                Logger.info('Waiting for node ...', task_name=self.task_name)
                time.sleep(10)
            if not master:
                raise RuntimeError('Failed to attach to master node')

            Logger.success('Attached to node (run id {})'.format(master.name), task_name=self.task_name)
            return master
        except Exception as e:
            self.fail_task(e.message)

    def get_node_info(self, parameters, task_name, run_id):
        params = self.parse_parameters(parameters)
        runs = self.pipe_api.search_runs(params, status='RUNNING', run_id=run_id)
        if len(runs) == 0:
            params.append(('parent-id', str(run_id)))
            runs = self.pipe_api.search_runs(params, status='RUNNING')
        for run in runs:
            if self.check_run(run, params):
                node = Node(run)
                task_logs = self.pipe_api.load_task(node.run_id, task_name)
                if not task_logs:
                    return None
                task_status = task_logs[-1]['status']
                if task_status == 'SUCCESS':
                    return node
                elif task_status != 'RUNNING':
                    raise RuntimeError(
                        'Node failed to start as it cannot attach to a node (run id {})'.format(node.run_id))
        return None

    def parse_parameters(self, parameters):
        result = []
        for param in parameters:
            if '=' not in param:
                raise RuntimeError("Illegal parameter format. Key=Value is expected.")
            result.append(param.split("=", 1))
        return result

    def check_run(self, run, params):
        run_params = {}
        for run_param in run['pipelineRunParameters']:
            value = run_param['value'] if 'value' in run_param else None
            run_params[run_param['name']] = value
        for param in params:
            if param[0] not in run_params or run_params[param[0]] != param[1]:
                return False
        return True

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--parameter', type=str, required=True, nargs='*')
    parser.add_argument('--task-name', required=True)
    parser.add_argument('--run-id', required=True, type=int)
    args = parser.parse_args()
    status = StatusEntry(TaskStatus.SUCCESS)
    try:
        node = WaitForNode().await_node_start(args.parameter, args.task_name, args.run_id)
        print(node.name + " " + node.ip)
        exit(0)
    except Exception as e:
        Logger.warn(e.message)
        status = StatusEntry(TaskStatus.FAILURE)
    if status.status == TaskStatus.FAILURE:
        raise RuntimeError('Failed to setup cluster')


if __name__ == '__main__':
    main()
