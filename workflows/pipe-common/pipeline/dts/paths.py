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



class Path:

    def __init__(self, source_path, destination_path, rules):
        self.source_path = source_path
        self.destination_path = destination_path
        self.rules = rules

    def __str__(self):
        return str(self.__dict__)


class LocalToS3(Path):
    def __init__(self, local_path, remote_path, rules):
        Path.__init__(self, local_path, remote_path, rules)


class S3ToLocal(Path):
    def __init__(self, remote_path, local_path, rules):
        Path.__init__(self, remote_path, local_path, rules)