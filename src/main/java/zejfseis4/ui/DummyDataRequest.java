package zejfseis4.ui;

import zejfseis4.data.DataManager;
import zejfseis4.data.DataRequest;

public class DummyDataRequest extends DataRequest {

    public DummyDataRequest(DataManager dataManager, String name, long durationMS) {
        super(dataManager, name, durationMS);
    }

    @Override
    public int getSampleTime() {
        return 1000 / 40;
    }

    @Override
    public double getFilteredValue(long time) {
        return 0;
    }

    @Override
    public long getLogId(long t) {
        return 1;
    }
}
