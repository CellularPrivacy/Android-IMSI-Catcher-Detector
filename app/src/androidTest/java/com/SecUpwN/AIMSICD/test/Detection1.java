/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.test;

import android.test.AndroidTestCase;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.utils.Cell;

/**
 * Test detection 1: Changing LAC
 */
public class Detection1 extends AndroidTestCase {
    AIMSICDDbAdapter dbHelper;
    public final static int CELL_ID = 123456;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dbHelper = new AIMSICDDbAdapter(getContext());
        
    }

    @Override
    protected void tearDown() throws Exception {
        
        super.tearDown();
    }

    public void testChangingLAC() {
        // First delete any records for test cell
        dbHelper.deleteCell(CELL_ID);
/*
        // add a test cell into the db
        Cell cell = new Cell();
        cell.setCID(CELL_ID);
        cell.setLat(21.00);
        cell.setLon(21.00);
        cell.setLAC(123);

        long recId = dbHelper.insertCell(cell);
        assertTrue(recId != 0l);
        assertTrue(dbHelper.checkLAC(cell));

        // change it's LAC and test
        cell = new Cell();
        cell.setCID(CELL_ID);
        cell.setLAC(234);
        assertFalse(dbHelper.checkLAC(cell));
*/
    }

}
