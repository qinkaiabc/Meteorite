package com.firelotus.meteorite.ui.content;

import android.content.Context;

import com.firelotus.meteorite.BuildConfig;
import com.firelotus.meteorite.ui.bean.EveryDayBean;
import com.firelotus.meteorite.ui.bean.GankBean;
import com.firelotus.meteoritelibrary.tools.MLog;
import com.firelotus.meteoritelibrary.tools.MNovateResponse;
import com.firelotus.meteoritelibrary.tools.NetworkInterceptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tamic.novate.BaseSubscriber;
import com.tamic.novate.Novate;
import com.tamic.novate.Throwable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.TreeMap;

import okhttp3.ResponseBody;

/**
 * Created by firelotus on 2017/10/15.
 */

public class SubPresenter implements SubContract.Presenter {
    private SubContract.View view;
    private Context context;
    private Novate novate;

    public SubPresenter(Context context, SubContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
        this.context = context;
        novate = new Novate.Builder(context)
                .baseUrl(BuildConfig.BASE_URL)
                .addInterceptor(new NetworkInterceptor(context))
                .build();
    }
    @Override
    public void getContent(String type, int pageIndex, int pageSize) {
        view.showProgress();
        TreeMap<String, Object> parameters = new TreeMap<>();
        //福利 | Android | iOS | 休息视频 | 拓展资源 | 前端 | all
        novate.get("data/"+type+"/"+pageSize+"/"+pageIndex,parameters,new BaseSubscriber<ResponseBody>(context){
            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String jstr = new String(responseBody.bytes());
                    MLog.d(jstr);
                    Type type = new TypeToken<MNovateResponse<ArrayList<GankBean>>>() {
                    }.getType();
                    MNovateResponse<ArrayList<GankBean>> response = new Gson().fromJson(jstr, type);
                    MLog.d(response.toString());
                    view.onContentSuccess(response.getResults());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
        view.dismissProgress();
    }

    @Override
    public void getEveryDay(String year, String month, String day) {
        view.showProgress();
        TreeMap<String, Object> parameters = new TreeMap<>();
        novate.get("day/"+year+"/"+month+"/"+day,parameters,new BaseSubscriber<ResponseBody>(context){
            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String jstr = new String(responseBody.bytes());
                    MLog.d(jstr);
                    JSONObject jsonObject = new JSONObject(jstr);
                    JSONArray category = jsonObject.getJSONArray("category");
                    JSONObject results = jsonObject.getJSONObject("results");
                    EveryDayBean everyDayBean = new EveryDayBean();
                    Type type = new TypeToken<ArrayList<GankBean>>() {}.getType();
                    for(int i = 0; i < category.length(); i++){
                        if("Android".equals(category.get(i))){
                            everyDayBean.setAndroid((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("Android")), type));
                        }else if("iOS".equals(category.get(i))){
                            everyDayBean.setiOS((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("iOS")), type));
                        }else if("前端".equals(category.get(i))){
                            everyDayBean.setFront((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("前端")), type));
                        }else if("App".equals(category.get(i))){
                            everyDayBean.setApp((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("App")), type));
                        }else if("休息视频".equals(category.get(i))){
                            everyDayBean.setRestMovie((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("休息视频")), type));
                        }else if("拓展资源".equals(category.get(i))){
                            everyDayBean.setResource((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("拓展资源")), type));
                        }else if("瞎推荐".equals(category.get(i))){
                            everyDayBean.setRecommend((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("瞎推荐")), type));
                        }else if("福利".equals(category.get(i))){
                            everyDayBean.setWelfare((ArrayList<GankBean>)new Gson().fromJson(String.valueOf(results.getJSONArray("福利")), type));
                        }
                    }
                    MLog.d(everyDayBean.toString());
                    view.onEveryDaySuccess(everyDayBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
        view.dismissProgress();
    }
}