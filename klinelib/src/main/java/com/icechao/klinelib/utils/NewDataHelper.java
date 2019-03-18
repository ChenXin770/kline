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
public class NewDataHelper {


    public static final int INDEX_OPEN = 0;
    public static final int INDEX_CLOSE = 1;
    public static final int INDEX_HIGH = 2;
    public static final int INDEX_LOW = 3;
    public static final int INDEX_VOL = 4;
    public static final int INDEX_MA_1 = 5;
    public static final int INDEX_MA_2 = 6;
    public static final int INDEX_MA_3 = 7;
    public static final int INDEX_BOLL_UP = 8;
    public static final int INDEX_BOLL_MB = 9;
    public static final int INDEX_BOLL_DN = 10;
    public static final int INDEX_KDJ_K = 11;
    public static final int INDEX_KDJ_D = 12;
    public static final int INDEX_KDJ_J = 13;
    public static final int INDEX_WR_1 = 14;
    public static final int INDEX_WR_2 = 15;
    public static final int INDEX_WR_3 = 16;
    public static final int INDEX_VOL_MA_1 = 17;
    public static final int INDEX_VOL_MA_2 = 18;
    public static final int INDEX_RSI = 19;
    public static final int INDEX_MACD_DEA = 20;
    public static final int INDEX_MACD_DIF = 21;
    public static final int INDEX_MACD = 22;


    /**
     * 计算  重构:计算方法,过多循环~  把循环尽量放到一个循环种加快计算速度
     *
     * @param dataList
     */
    static void calculate(List<KLineEntity> dataList, float bollP, int bollN,
                          float priceMaOne, float priceMaTwo, float priceMaThree,
                          int s, int l, int m,
                          float maOne, float maTwo, float maThree,
                          int kdjDay,
                          int wr1, int wr2, int wr3) {
        float maSum1 = 0;
        float maSum2 = 0;
        float maSum3 = 0;


        float volumeMaOne = 0;
        float volumeMaTwo = 0;
        float volumeMaThree = 0;


        float preEma12 = 0;
        float preEma26 = 0;

        float preDea = 0;


        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            KLineEntity point = dataList.get(i);
            float closePrice = point.getClosePrice();
            //ma计算
            maSum1 += closePrice;
            maSum2 += closePrice;
            maSum3 += closePrice;

            if (i == priceMaOne - 1) {
                point.maOne = maSum1 / priceMaOne;
            } else if (i >= priceMaOne) {
                maSum1 -= dataList.get((int) (i - priceMaOne)).getClosePrice();
                point.maOne = maSum1 / priceMaOne;
            } else {
                point.maOne = Float.MIN_VALUE;
            }
            if (i == priceMaTwo - 1) {
                point.maTwo = maSum2 / priceMaTwo;
            } else if (i >= priceMaTwo) {
                maSum2 -= dataList.get((int) (i - priceMaTwo)).getClosePrice();
                point.maTwo = maSum2 / priceMaTwo;
            } else {
                point.maTwo = Float.MIN_VALUE;
            }
            if (i == priceMaThree - 1) {
                point.maThree = maSum3 / priceMaThree;
            } else if (i >= priceMaThree) {
                maSum3 -= dataList.get((int) (i - priceMaThree)).getClosePrice();
                point.maThree = maSum3 / priceMaThree;
            } else {
                point.maThree = Float.MIN_VALUE;
            }

            //macd
            if (s > 0 && l > 0 && m > 0) {
                if (size >= m + l - 2) {
                    if (i < l - 1) {
                        point.dif = 0;
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
            }


            //boll计算
            if (i >= bollN - 1) {
                float boll = calculateBoll(dataList, i, bollN);
                float highBoll = boll + bollP * STD(dataList, i, bollN);
                float lowBoll = boll - bollP * STD(dataList, i, bollN);
                point.up = highBoll;
                point.mb = boll;
                point.dn = lowBoll;
            } else {
                point.up = Float.MIN_VALUE;
                point.mb = Float.MIN_VALUE;
                point.dn = Float.MIN_VALUE;
            }

            //vol ma计算
            volumeMaOne += point.getVolume();
            volumeMaTwo += point.getVolume();
            volumeMaThree += point.getVolume();
            float ma;
            if (i == maOne - 1) {
                ma = (volumeMaOne / maOne);
            } else if (i > maOne - 1) {
                volumeMaOne -= dataList.get((int) (i - maOne)).getVolume();
                ma = volumeMaOne / maOne;
            } else {
                ma = Float.MIN_VALUE;
            }
            point.ma5Volume = ma;

            if (i == maTwo - 1) {
                ma = (volumeMaTwo / maTwo);
            } else if (i > maTwo - 1) {
                volumeMaTwo -= dataList.get((int) (i - maTwo)).getVolume();
                ma = volumeMaTwo / maTwo;
            } else {
                ma = Float.MIN_VALUE;
            }
            point.ma10Volume = ma;

            if (i == maThree - 1) {
                ma = (volumeMaThree / maThree);
            } else if (i > maThree - 1) {
                volumeMaThree -= dataList.get((int) (i - maThree)).getVolume();
                ma = volumeMaThree / maThree;
            } else {
                ma = Float.MIN_VALUE;
            }
            point.MAVolume = ma;

            //kdj
            calcKdj(dataList, kdjDay, i, point, closePrice);
            //计算3个 wr指标
            point.wrOne = getValueWR(dataList, wr1, i);
            point.wrTwo = getValueWR(dataList, wr2, i);
            point.wrThree = getValueWR(dataList, wr3, i);


        }
    }

    private static float getValueWR(List<KLineEntity> dataList, int wr1, int i) {
        float valueWR;
        if (wr1 != 0 && i >= wr1) {
            valueWR = -calcWr(dataList, i, wr1);
        } else {
            valueWR = Float.MIN_VALUE;
        }
        return valueWR;
    }

    private static void calcKdj(List<KLineEntity> dataList, int kdjDay, int i, KLineEntity point, float closePrice) {
        float k;
        float d;
        if (i < kdjDay - 1 || 0 == i) {
            point.k = Float.MIN_VALUE;
            point.d = Float.MIN_VALUE;
            point.j = Float.MIN_VALUE;
        } else {
            int startIndex = i - kdjDay + 1;
            float maxRsi = Float.MIN_VALUE;
            float minRsi = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                maxRsi = Math.max(maxRsi, dataList.get(index).getHighPrice());
                minRsi = Math.min(minRsi, dataList.get(index).getLowPrice());
            }
            float rsv;
            try {
                rsv = 100f * (closePrice - minRsi) / (maxRsi - minRsi);
            } catch (Exception e) {
                rsv = 0f;
            }
            KLineEntity kLineEntity = dataList.get(i - 1);
            float k1 = kLineEntity.getK();
            k = 2f / 3f * (k1 == Float.MIN_VALUE ? 50 : k1) + 1f / 3f * rsv;
            float d1 = kLineEntity.getD();
            d = 2f / 3f * (d1 == Float.MIN_VALUE ? 50 : d1) + 1f / 3f * k;
            point.k = k;
            point.d = d;
            point.j = 3f * k - 2 * d;
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param dataList
     */
    public static void calculate(List<KLineEntity> dataList) {
        calculate(dataList, 2, 20,
                5, 10, 30,
                12, 26, 9,
                5, 10, 30,
                14,
                14, 0, 0);
        calculateRSI(dataList, 14);
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
                    y += list.get(i).close;
                }
                return y / n;
            } else {
                return (preEma * (n - 1) + list.get(index).close * 2) / (n + 1);
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
                return ((preDea * (m - 1) + list.get(index).getDif() * 2) / (m + 1));
            }
        } catch (Exception e) {
            return y;
        }
    }


    public static void calculateRSI(List<KLineEntity> klineInfos,
                                    int n) {
        if (klineInfos.size() > n) {
            double firstValue = klineInfos.get(n - 1).rOne;
            if (firstValue != 0 && firstValue != Float.MIN_VALUE) {
                calculateRSIChange(klineInfos, n, findStartIndex(klineInfos),
                        klineInfos.size());
            } else {
                calculateRSIChange(klineInfos, n, 0, klineInfos.size());
            }
        }

    }


    private static void calculateRSIChange(List<KLineEntity> klineInfos,
                                           int n, int start, int end) {
        double upPriceRma = 0;
        double downPriceRma = 0;
        for (int i = start; i < end; i++) {
            double rsi = 0;
            if (i == n) {
                double upPrice = 0;
                double downPrice = 0;
                for (int k = 1; k <= n; k++) {
                    KLineEntity kLineEntity = klineInfos.get(k);
                    double close = kLineEntity.close;
                    double lastClose = klineInfos.get(k - 1).close;
                    upPrice += Math.max(close - lastClose, 0);
                    downPrice += Math.max(lastClose - close, 0);
                }
                upPriceRma = upPrice / n;
                downPriceRma = downPrice / n;
                rsi = calculateRSI(upPriceRma, downPriceRma);
            } else if (i > n) {
                double close = klineInfos.get(i).close;
                double lastClose = klineInfos.get(i - 1).close;

                double upPrice = Math.max(close - lastClose, 0);
                double downPrice = Math.max(lastClose - close, 0);

                upPriceRma = (upPrice + (n - 1) * upPriceRma) / n;
                downPriceRma = (downPrice + (n - 1) * downPriceRma) / n;
                rsi = calculateRSI(upPriceRma, downPriceRma);
            } else {
                rsi = Float.MIN_VALUE;
            }

            klineInfos.get(i).rOne = (float) rsi;
        }
    }

    private static double calculateRSI(double upPriceRma, double downPriceRma) {
        if (downPriceRma == 0) {
            return 100;
        } else {
            if (upPriceRma == 0) {
                return 0;
            } else {
                return 100 - (100 / (1 + upPriceRma / downPriceRma));
            }
        }
    }

    private static int findStartIndex(List<KLineEntity> klineInfos) {
        int startIndex = 0;
        for (int i = klineInfos.size() - 1; i > 0; i--) {
            double maValue = klineInfos.get(i).rOne;
            if (maValue != 0) {
                startIndex = i + 1;
                break;
            }
        }
        return startIndex;
    }


    public static float calcWr(List<KLineEntity> dataDiction, int nIndex, int n) {
        float result = 0;
        float lowInNLowsValue = getLowestOfArray(dataDiction, nIndex, n);   //N日内最低价的最低值
        float highInHighsValue = getHighestOfArray(dataDiction, nIndex, n);   //N日内最低价的最低值
        float valueSpan = highInHighsValue - lowInNLowsValue;
        if (valueSpan > 0) {
            KLineEntity kLineData = dataDiction.get(nIndex);
            result = 100 * (highInHighsValue - kLineData.close) / valueSpan;
        } else
            result = 0;

        return result;
    }


    public static float getLowestOfArray(List<KLineEntity> valuesArray, int fromIndex, int nCount) {
        float result = Float.MAX_VALUE;
        int endIndex = fromIndex - (nCount - 1);
        if (fromIndex >= endIndex) {
            for (int itemIndex = fromIndex + 1; itemIndex > endIndex; itemIndex--) {
                KLineEntity klineData = valuesArray.get(itemIndex - 1);
                float lowPrice = klineData.low;
                result = result <= lowPrice ? result : lowPrice;
            }
        }

        return result;
    }


    public static float getHighestOfArray(List<KLineEntity> valuesArray,
                                          int fromIndex, int nCount) {
        float result = Float.MIN_VALUE;
        int endIndex = fromIndex - (nCount - 1);
        if (fromIndex >= endIndex) {
            for (int itemIndex = fromIndex + 1; itemIndex > endIndex; itemIndex--) {
                KLineEntity klineData = valuesArray.get(itemIndex - 1);
                float highPrice = klineData.high;
                result = result >= highPrice ? result : highPrice;
            }
        }
        return result;
    }

    public static void calculateBoLL(List<KLineEntity> klineInfos, int n, int p) {
        if (n == 0 || p == 0) {
            return;
        }
        if (klineInfos.size() < n) {
            return;
        }

        for (int i = n - 1; i < klineInfos.size(); i++) {
            float boll = calculateBoll(klineInfos, i, n);
            float highBoll = boll + p * STD(klineInfos, i, n);
            float lowBoll = boll - p * STD(klineInfos, i, n);
            klineInfos.get(i).up = (highBoll);
            klineInfos.get(i).mb = (boll);
            klineInfos.get(i).dn = (lowBoll);
        }
    }


    /**
     * 布林线指标计算
     */
    private static float calculateBoll(List<KLineEntity> payloads, int index, int maN) {
        float sum = 0;
        for (int i = index; i >= index - maN + 1; i--) {
            sum = (sum + payloads.get(i).close);
        }
        return sum / maN;

    }

    private static float STD(List<KLineEntity> payloads, int index, int maN) {

        float sum = 0f;
        float std = 0f;
        for (int i = index; i >= index - maN + 1; i--) {
            sum += payloads.get(i).close;
        }
        float avg = sum / maN;
        //float avg = payloads.get(index).bollMa;
        //不能提前计算,会出现0.001的误差
        for (int i = index; i >= index - maN + 1; i--) {
            std += (payloads.get(i).close - avg) * (payloads.get(i).close - avg);
        }
        return (float) Math.sqrt(std / maN);
    }


}
