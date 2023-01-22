package bjfu.it.yhz.odometer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class OdometerViewModel extends AndroidViewModel {
    private MutableLiveData<String> longitude; // 经度
    private MutableLiveData<String> latitude; // 纬度
    private MutableLiveData<String> time;  // 采样时间

    private OdometerService odometerService;


    // 进行页面刷新任务
    //

    public OdometerViewModel(@NonNull Application application) {
        super(application);

    }
}
