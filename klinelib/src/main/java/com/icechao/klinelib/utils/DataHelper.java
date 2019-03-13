package com.icechao.klinelib.utils;

import com.icechao.klinelib.entity.KLineEntity;

import java.util.List;

/*************************************************************************
 * Description   :
 *
 * @PackageName  : com.icechao.klinelib.utils
 * @FileName     : DataHelper.java
 * @Author       : chao
 * @Date         : 2019/1/8
 * @Email        : icechliu@gmail.com
 * @version      : V1
 *************************************************************************/
public class DataHelper {


    /**
     * 计算  重构:计算方法,过多循环~  把循环尽量放到一个循环种加快计算速度
     *
     * @param dataList
     */
    static void calculate(List<KLineEntity> dataList, float bollP, int bollN,
                          float priceMaOne, float priceMaTwo, float priceMaThree,
                          int s, int l, int m,
                          float maOne, float maTwo, float maThree,
                          int rsiDay,
                          int kdjDay,
                          int one, int two, int three) {
        float ma5 = 0;
        float ma10 = 0;
        float ma20 = 0;
        float ma30 = 0;

        float preEma12 = 0;
        float preEma26 = 0;

        float preDea = 0;


        float volumeMaOne = 0;
        float volumeMaTwo = 0;
        float volumeMaThree = 0;


        Float rsi;
        float rsiABSEma = 0;
        float rsiMaxEma = 0;


        float k = 0;
        float d = 0;

        Float r;


        for (int i = 0; i < dataList.size(); i++) {


            KLineEntity point = dataList.get(i);
            final float closePrice = point.getClosePrice();
            //ma计算
            ma5 += closePrice;
            ma10 += closePrice;
            ma20 += closePrice;
            ma30 += closePrice;
            if (i == priceMaOne - 1) {
                point.maOne = ma5 / priceMaOne;
            } else if (i >= priceMaOne) {
                ma5 -= dataList.get((int) (i - priceMaOne)).getClosePrice();
                point.maOne = ma5 / priceMaOne;
            } else {
                point.maOne = 0f;
            }
            if (i == priceMaTwo - 1) {
                point.maTwo = ma10 / priceMaTwo;
            } else if (i >= priceMaTwo) {
                ma10 -= dataList.get((int) (i - priceMaTwo)).getClosePrice();
                point.maTwo = ma10 / priceMaTwo;
            } else {
                point.maTwo = 0f;
            }
            if (i == priceMaThree - 1) {
                point.maThree = ma20 / priceMaThree;
            } else if (i >= priceMaThree) {
                ma20 -= dataList.get((int) (i - priceMaThree)).getClosePrice();
                point.maThree = ma20 / priceMaThree;
            } else {
                point.maThree = 0f;
            }
            if (i == bollP - 1) {
                point.bollMa = ma30 / bollP;
            } else if (i >= bollP) {
                ma30 -= dataList.get((int) (i - bollP)).getClosePrice();
                point.bollMa = ma30 / bollP;
            } else {
                point.bollMa = 0f;
            }


            if (dataList.size() >= m + l - 2) {
                if (i < l - 1) {
                    dataList.get(i).dif = (0);
                }

                if (i >= s - 1) {
                    float ema12 = calculateEMA(dataList, s, i, preEma12);
                    preEma12 = ema12;
                    if (i >= l - 1) {
                        float ema26 = calculateEMA(dataList, l, i, preEma26);
                        preEma26 = ema26;
                        point.dif = (ema12 - ema26);
                    } else {
                        point.dif = Float.MIN_VALUE;
                    }
                } else {
                    point.dif = Float.MIN_VALUE;
                }


                if (i >= m + l - 2) {
                    boolean isFirst = i == m + l - 2;
                    float dea = calculateDEA(dataList, l, m, i, preDea, isFirst);
                    preDea = dea;
                    point.dea = (dea);
                } else {
                    point.dea = Float.MIN_VALUE;
                }


                if (i >= m + l - 2) {
                    point.macd = point.getDif() - point.getDea();
                } else {
                    point.macd = 0;
                }

            } else {
                point.macd = 0;
            }

            //boll计算
            if (i < bollP - 1) {
                point.mb = 0f;
                point.up = 0f;
                point.dn = 0f;
            } else {
                int n = (int) bollP;
                float md = 0;
                for (int j = i - n + 1; j <= i; j++) {
                    float c = dataList.get(j).getClosePrice();
                    float mm = point.getBollMa();
                    float value = c - mm;
                    md += value * value;
                }
                md = md / (n - 1);
                md = (float) Math.sqrt(md);
                point.mb = point.getBollMa();
                point.up = point.mb + bollN * md;
                point.dn = point.mb - bollN * md;
            }

            //vol ma计算
            volumeMaOne += point.getVolume();
            volumeMaTwo += point.getVolume();
            volumeMaThree += point.getVolume();

            if (i == maOne - 1) {
                point.MA5Volume = (volumeMaOne / maOne);
            } else if (i > maOne - 1) {
                volumeMaOne -= dataList.get((int) (i - maOne)).getVolume();
                point.MA5Volume = volumeMaOne / maOne;
            } else {
                point.MA5Volume = 0f;
            }

            if (i == maTwo - 1) {
                point.MA10Volume = (volumeMaTwo / maTwo);
            } else if (i > maTwo - 1) {
                volumeMaTwo -= dataList.get((int) (i - maTwo)).getVolume();
                point.MA10Volume = volumeMaTwo / maTwo;
            } else {
                point.MA10Volume = 0f;
            }

            if (i == maThree - 1) {
                point.MAVolume = (volumeMaThree / maThree);
            } else if (i > maThree - 1) {
                volumeMaThree -= dataList.get((int) (i - maThree)).getVolume();
                point.MAVolume = volumeMaThree / maThree;
            } else {
                point.MAVolume = 0f;
            }


            if (i == 0) {
                rsi = 0f;
                rsiABSEma = 0;
                rsiMaxEma = 0;
            } else {
                float Rmax = Math.max(0, closePrice - dataList.get(i - 1).getClosePrice());
                float RAbs = Math.abs(closePrice - dataList.get(i - 1).getClosePrice());

                rsiMaxEma = (Rmax + (rsiDay - 1) * rsiMaxEma) / rsiDay;
                rsiABSEma = (RAbs + (rsiDay - 1) * rsiABSEma) / rsiDay;
                rsi = (rsiMaxEma / rsiABSEma) * 100;
            }
            if (i < rsiDay - 1) {
                rsi = 0f;
            }
            if (rsi.isNaN())
                rsi = 0f;
            point.rsi = rsi;

            int startIndex = i - kdjDay - 1;
            if (startIndex < 0) {
                startIndex = 0;
            }
            float maxRsi = Float.MIN_VALUE;
            float minRsi = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                maxRsi = Math.max(maxRsi, dataList.get(index).getHighPrice());
                minRsi = Math.min(minRsi, dataList.get(index).getLowPrice());
            }
            Float rsv = 100f * (closePrice - minRsi) / (maxRsi - minRsi);
            if (rsv.isNaN()) {
                rsv = 0f;
            }
            if (i == 0) {
                k = 50;
                d = 50;
            } else {
                k = (rsv + 2f * k) / 3f;
                d = (k + 2f * d) / 3f;
            }
            if (i < kdjDay - 1) {
                point.k = 0f;
                point.d = 0f;
                point.j = 0f;
            } else if (i == kdjDay - 1 || i == kdjDay) {
                point.k = k;
                point.d = 0f;
                point.j = 0f;
            } else {
                point.k = k;
                point.d = d;
                point.j = 3f * k - 2 * d;
            }


            //wr
            int startIndexOne = i - one;
            int startIndexTwo = i - two;
            int startIndexThree = i - three;
            if (startIndexOne < 0) {
                startIndexOne = 0;
            }
            if (startIndexTwo < 0) {
                startIndexTwo = 0;
            }
            if (startIndexThree < 0) {
                startIndexThree = 0;
            }
            float maxWr = Float.MIN_VALUE;
            float minWr = Float.MAX_VALUE;
            for (int index = startIndexOne; index <= i; index++) {
                maxWr = Math.max(maxWr, dataList.get(index).getHighPrice());
                minWr = Math.min(minWr, dataList.get(index).getLowPrice());
            }

            if (i < one - 1) {
                point.rOne = Float.NaN;
            } else {
                r = -100 * (maxWr - dataList.get(i).getClosePrice()) / (maxWr - minWr);
                if (r.isNaN()) {
                    point.rOne = 0f;
                } else {
                    point.rOne = r;
                }
            }
            float maxTwo = Float.MIN_VALUE;
            float minTwo = Float.MAX_VALUE;
            for (int index = startIndexTwo; index <= i; index++) {
                maxTwo = Math.max(maxTwo, dataList.get(index).getHighPrice());
                minTwo = Math.min(minTwo, dataList.get(index).getLowPrice());
            }
            if (i < two - 1) {
                point.rTwo = Float.NaN;
            } else {
                r = -100 * (maxTwo - dataList.get(i).getClosePrice()) / (maxTwo - minTwo);
                if (r.isNaN()) {
                    point.rTwo = 0f;
                } else {
                    point.rTwo = r;
                }
            }
            float maxThree = Float.MIN_VALUE;
            float minTree = Float.MAX_VALUE;
            for (int index = startIndexThree; index <= i; index++) {
                maxThree = Math.max(maxThree, dataList.get(index).getHighPrice());
                minTree = Math.min(minTree, dataList.get(index).getLowPrice());
            }
            if (i < three - 1) {
                point.rThree = Float.NaN;
            } else {
                r = -100 * (maxThree - dataList.get(i).getClosePrice()) / (maxThree - minTree);
                if (r.isNaN()) {
                    point.rThree = 0f;
                } else {
                    point.rThree = r;
                }
            }
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param dataList
     */
    public static void calculate(List<KLineEntity> dataList) {
        calculate(dataList, 20, 2,
                5, 10, 30,
                12, 26, 9,
                5, 10, 30,
                14,
                14,
                14, 0, 0);
    }


    /**
     * 计算ema指标
     *
     * @param n      s或l
     * @param index  index
     * @param preEma 上一个ema
     * @return ema
     */
    private static float calculateEMA(List<KLineEntity> list, int n, int index, float preEma) {
        float y = 0;
        try {
            if (index + 1 < n) {
                return y;
            } else if (index + 1 == n) {
                for (int i = 0; i < n; i++) {
                    y += list.get(i).Close;
                }
                return y / n;
            } else {
                return (float) ((preEma * (n - 1) + list.get(index).Close * 2) / (n + 1));
            }
        } catch (Exception e) {
            return y;
        }
    }

    /**
     * 计算DEA
     *
     * @param list    列表
     * @param l       L26
     * @param m       M9
     * @param index   index
     * @param preDea  上一个dea
     * @param isFirst 是否是第一个
     * @return dea
     */
    private static float calculateDEA(List<KLineEntity> list, int l, int m, int index,
                                      float preDea,
                                      boolean isFirst) {
        float y = 0;
        try {
            if (isFirst) {
                for (int i = l - 1; i <= m + l - 2; i++) {
                    y += list.get(i).getDif();
                }
                return y / m;
            } else {
                return (float) ((preDea * (m - 1) + list.get(index).getDif() * 2) / (m + 1));
            }
        } catch (Exception e) {
            return y;
        }
    }
}
