package nova.Interface;
/**
 * 开机、关机类操作的回调接口
 */
public interface IVMPowerCallback
{
    /**
     * 当操作成功完成时被调用
     * @param machine 关闭的虚拟机的句柄
     */
    public void onOperationDone(VM_HANDLE machine);

    /**
     * 操作发生错误时被调用
     * @param machine 发生操作错误的虚拟机的句柄
     * @param errno 错误代码
     */	
    public void onError(VM_HANDLE machine, int errno);

}
