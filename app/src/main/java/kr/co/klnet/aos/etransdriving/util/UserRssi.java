package kr.co.klnet.aos.etransdriving.util;


import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.MinewBeacon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserRssi implements Comparator<MinewBeacon> {

    private List<MinewBeacon> mMinewBeacons = new ArrayList<>();

    @Override
    public int compare(MinewBeacon minewBeacon, MinewBeacon t1) {
        float floatValue1 = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();
        float floatValue2 = t1.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getFloatValue();


        if(floatValue1<floatValue2){
            return 1;
        }else if(floatValue1==floatValue2){
            return 0;
        }else {
            return -1;
        }
    }

    public void setItems(List<MinewBeacon> newItems) {
//        validateItems(newItems);


        int startPosition = 0;
        int preSize = 0;
        if (this.mMinewBeacons != null) {
            preSize = this.mMinewBeacons.size();

        }
        if (preSize > 0) {
            this.mMinewBeacons.clear();
           // notifyItemRangeRemoved(startPosition, preSize);
        }
        this.mMinewBeacons.addAll(newItems);
        //notifyItemRangeChanged(startPosition, newItems.size());
    }


}
