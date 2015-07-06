package com.hiuzhong.baselib.util;

import java.util.Random;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class RandomUtil {
    char[] chars = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d'
            ,'e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t'
            ,'u','v','w','x','y','z'};
    char[] charsAll = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d'
            ,'e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t'
            ,'u','v','w','x','y','z','A','B','C','D'
            ,'E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T'
            ,'U','V','W','X','Y','Z'
    };

    public String randomPurChar(int length){
        StringBuilder re = new StringBuilder(length);
        Random r = new Random();
        for (int i = 0; i <length ; i++) {
            re.append(charsAll[r.nextInt(charsAll.length)]);
        }
        return re.toString();
    }

}