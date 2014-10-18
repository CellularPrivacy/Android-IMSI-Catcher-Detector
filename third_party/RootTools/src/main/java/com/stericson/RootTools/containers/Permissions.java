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

public class Permissions {
    String type;
    String user;
    String group;
    String other;
    String symlink;
    int permissions;

    public String getSymlink() {
        return this.symlink;
    }

    public String getType() {
        return type;
    }

    public int getPermissions() {
        return this.permissions;
    }

    public String getUserPermissions() {
        return this.user;
    }

    public String getGroupPermissions() {
        return this.group;
    }

    public String getOtherPermissions() {
        return this.other;
    }

    public void setSymlink(String symlink) {
        this.symlink = symlink;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public void setUserPermissions(String user) {
        this.user = user;
    }

    public void setGroupPermissions(String group) {
        this.group = group;
    }

    public void setOtherPermissions(String other) {
        this.other = other;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }


}
