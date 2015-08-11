package com.hiuzhong.yuxun.helper;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * Created by gongsheng on 2015/8/11.
 */
public class BaiduGpsConvertHelper {

    public static LatLng convertGpsToBaidu(LatLng source){
        CoordinateConverter  converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(source);
        return  converter.convert();
    }
}
