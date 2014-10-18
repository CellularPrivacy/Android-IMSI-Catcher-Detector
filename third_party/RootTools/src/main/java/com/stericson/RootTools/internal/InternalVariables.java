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

package com.stericson.RootTools.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.containers.Symlink;

public class InternalVariables {

    // ----------------------
    // # Internal Variables #
    // ----------------------


    protected static boolean accessGiven = false;
    protected static boolean nativeToolsReady = false;
    protected static boolean found = false;
    protected static boolean processRunning = false;

    protected static String[] space;
    protected static String getSpaceFor;
    protected static String busyboxVersion;
    protected static String pid_list = "";
    protected static Set<String> path;
    protected static ArrayList<Mount> mounts;
    protected static ArrayList<Symlink> symlinks;
    protected static List<String> results;
    protected static String inode = "";
    protected static Permissions permissions;

    // regex to get pid out of ps line, example:
    // root 2611 0.0 0.0 19408 2104 pts/2 S 13:41 0:00 bash
    protected static final String PS_REGEX = "^\\S+\\s+([0-9]+).*$";
    protected static Pattern psPattern;

    static {
        psPattern = Pattern.compile(PS_REGEX);
    }
}
