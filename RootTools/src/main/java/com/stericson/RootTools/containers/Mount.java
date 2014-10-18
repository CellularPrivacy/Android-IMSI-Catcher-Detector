/* 
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *  
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *  
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 * 
 * The terms of each license can be found in the root directory of this project's repository as well as at:
 * 
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.RootTools.containers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Mount {
    final File mDevice;
    final File mMountPoint;
    final String mType;
    final Set<String> mFlags;

    public Mount(File device, File path, String type, String flagsStr) {
        mDevice = device;
        mMountPoint = path;
        mType = type;
        mFlags = new LinkedHashSet<String>(Arrays.asList(flagsStr.split(",")));
    }

    public File getDevice() {
        return mDevice;
    }

    public File getMountPoint() {
        return mMountPoint;
    }

    public String getType() {
        return mType;
    }

    public Set<String> getFlags() {
        return mFlags;
    }

    @Override
    public String toString() {
        return String.format("%s on %s type %s %s", mDevice, mMountPoint, mType, mFlags);
    }
}
