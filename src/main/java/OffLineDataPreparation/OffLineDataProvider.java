package OffLineDataPreparation;

import Utils.Const;

import java.util.Map;

/**
 * Created by dbg on 15/05/2017.
 */
public class OffLineDataProvider {

    private String vhdrFile;
    private String vmrkFile;
    private String eegFile;
    private int FZIndex;
    private int CZIndex;
    private int PZIndex;
    private Map<String, Integer> files;




    private void setFileNames(String filename) {
        int index = filename.lastIndexOf(".");
        String baseName = filename.substring(0, index);
        this.vhdrFile = baseName + Const.VHDR_EXTENSION;
        this.vmrkFile = baseName + Const.VMRK_EXTENSION;
        this.eegFile = baseName + Const.EEG_EXTENSION;
    }


    private float[] toFloatArray(double[] arr) {
        if (arr == null) {
            return null;
        }
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float) arr[i];
        }
        return ret;
    }

}
