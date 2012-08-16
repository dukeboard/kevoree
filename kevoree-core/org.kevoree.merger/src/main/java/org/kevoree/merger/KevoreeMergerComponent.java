/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.merger;

import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.merger.MergerErrorListener;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ffouquet
 */
public class KevoreeMergerComponent implements org.kevoree.api.service.core.merger.MergerService {

    private RootMerger rootMerger = new RootMerger();

    @Override
    public ContainerRoot merge(ContainerRoot actualModel, ContainerRoot targetModel) {
        rootMerger.merge(actualModel, targetModel);
        return actualModel;
    }

    private List<MergerErrorListener> listeners = new ArrayList<MergerErrorListener>();

    @Override
    public void registerMergerListener(MergerErrorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterMergerListener(MergerErrorListener listener) {
        listeners.remove(listener);
    }

}
