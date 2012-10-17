package nova.common.util;

public interface Cancellable {

    void cancel();

    boolean isCancelled();

}
