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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.stericson.RootTools.Constants;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

public class Remounter {

    //-------------
    //# Remounter #
    //-------------

    /**
     * This will take a path, which can contain the file name as well,
     * and attempt to remount the underlying partition.
     * <p/>
     * For example, passing in the following string:
     * "/system/bin/some/directory/that/really/would/never/exist"
     * will result in /system ultimately being remounted.
     * However, keep in mind that the longer the path you supply, the more work this has to do,
     * and the slower it will run.
     *
     * @param file      file path
     * @param mountType mount type: pass in RO (Read only) or RW (Read Write)
     * @return a <code>boolean</code> which indicates whether or not the partition
     *         has been remounted as specified.
     */

    public boolean remount(String file, String mountType) {

        //if the path has a trailing slash get rid of it.
        if (file.endsWith("/") && !file.equals("/")) {
            file = file.substring(0, file.lastIndexOf("/"));
        }
        //Make sure that what we are trying to remount is in the mount list.
        boolean foundMount = false;

        while (!foundMount) {
            try {
                for (Mount mount : RootTools.getMounts()) {
                    RootTools.log(mount.getMountPoint().toString());

                    if (file.equals(mount.getMountPoint().toString())) {
                        foundMount = true;
                        break;
                    }
                }
            } catch (Exception e) {
                if (RootTools.debugMode) {
                    e.printStackTrace();
                }
                return false;
            }
            if (!foundMount) {
                try {
                    file = (new File(file).getParent());
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        Mount mountPoint = findMountPointRecursive(file);

        if (mountPoint != null) {

            RootTools.log(Constants.TAG, "Remounting " + mountPoint.getMountPoint().getAbsolutePath() + " as " + mountType.toLowerCase());
            final boolean isMountMode = mountPoint.getFlags().contains(mountType.toLowerCase());

            if (!isMountMode) {
                //grab an instance of the internal class
                try {
                    CommandCapture command = new CommandCapture(0,
                            true,
                            "busybox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(),
                            "toolbox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(),
                            "mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(),
                            "/system/bin/toolbox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath()
                    );
                    Shell.startRootShell().add(command);
                    commandWait(command);

                } catch (Exception e) {}

                mountPoint = findMountPointRecursive(file);
            }

            if (mountPoint != null) {
                RootTools.log(Constants.TAG, mountPoint.getFlags() + " AND " + mountType.toLowerCase());
                if (mountPoint.getFlags().contains(mountType.toLowerCase())) {
                    RootTools.log(mountPoint.getFlags().toString());
                    return true;
                } else {
                    RootTools.log(mountPoint.getFlags().toString());
                    return false;
                }
            }
            else {
                RootTools.log("mount is null, file was: " + file + " mountType was: " + mountType);
            }
        }
        else {
            RootTools.log("mount is null, file was: " + file + " mountType was: " + mountType);
        }

        return false;
    }

    private Mount findMountPointRecursive(String file) {
        try {
            ArrayList<Mount> mounts = RootTools.getMounts();

            for (File path = new File(file); path != null; ) {
                for (Mount mount : mounts) {
                    if (mount.getMountPoint().equals(path)) {
                        return mount;
                    }
                }
            }

            return null;

        } catch (IOException e) {
            if (RootTools.debugMode) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (RootTools.debugMode) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void commandWait(Command cmd) {
        synchronized (cmd) {
            try {
                if (!cmd.isFinished()) {
                    cmd.wait(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
