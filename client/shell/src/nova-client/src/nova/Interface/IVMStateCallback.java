package nova.Interface;
/**
 * 状态操作的回调接口
 */
public interface IVMStateCallback
{
    /**
     * 当状态被保存时调用
     * @param state 状态句柄
     */
    void onStateSaved(STATE_HANDLE state);

    /**
     * 当状态被恢复时调用
     * @param state 状态句柄
     */
    void onStateLoaded(STATE_HANDLE state);
}
