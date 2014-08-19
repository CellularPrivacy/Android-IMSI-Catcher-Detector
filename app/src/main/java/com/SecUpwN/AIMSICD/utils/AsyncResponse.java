package com.SecUpwN.AIMSICD.utils;

import java.util.List;

public interface AsyncResponse {
    void processFinish(float[] output);
    void processFinish(List<Cell> cells);
}
