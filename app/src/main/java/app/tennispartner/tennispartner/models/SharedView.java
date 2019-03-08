package app.tennispartner.tenispartner.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedView extends ViewModel {
    private final MutableLiveData<Integer> selected = new MutableLiveData<>();

    public void setRadius(Integer radius) {
        selected.setValue(radius);
    }

    public LiveData<Integer> getRadius() {
        return selected;
    }
}
