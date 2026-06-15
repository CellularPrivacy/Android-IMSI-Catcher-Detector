package com.secupwn.aimsicd.utils;

import com.secupwn.aimsicd.adapters.CardItemData;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CellTest {

    @Test
    public void setCdmaCellIdentityStoresSidNidAndBidSeparatelyFromMnc() {
        Cell cell = new Cell();

        cell.setCdmaCellIdentity(42, 250, 12345);

        assertTrue(cell.isCdma());
        assertEquals(42, cell.getSid());
        assertEquals(250, cell.getLocationAreaCode());
        assertEquals(12345, cell.getCellId());
        assertEquals(Integer.MAX_VALUE, cell.getMobileNetworkCode());
    }

    @Test
    public void cardItemDataUsesCdmaLabels() {
        Cell cell = new Cell();
        cell.setCdmaCellIdentity(42, 250, 12345);

        CardItemData data = new CardItemData(cell, "1");

        assertEquals("BID: 12345  (0x3039)", data.getCellId());
        assertEquals("NID: 250", data.getLac());
        assertEquals("SID: 42", data.getMnc());
    }
}
