package nova.Interface;

import java.net.InetAddress;
import java.util.List;

/**
 * 虚拟机控制器需要实现的接口。
 * 将来可以根据不同的虚拟机实现，定义XenControl、KVMControl等类
 * 下面的主要接口采用异步方式通知执行结果
 */
public interface IVMMControl
{
    /**
     * 获取指定物理机上所有已注册的虚拟机句柄
     *
     * @param addr 指定物理机的网络地址
     * @return 返回虚拟机操作句柄数组
     */
    public VM_HANDLE[] getVirtualMachineList(InetAddress addr);

    /**
     * 获取虚拟机信息
     * 
     * @param machine 虚拟机句柄
     * @return 虚拟机的内存、CPU、网络信息
     */
    public IVM_INFO getVirtualMachineInfo(VM_HANDLE machine);

    /**
     * 运行虚拟机(在虚拟机关机时，相当于按下开机按钮；在虚拟机暂停或挂起时，恢复虚拟机运行）
     * 不指定运行物理机
     * @param machine  虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    public void startVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback);

    /**
     * 运行虚拟机(在虚拟机关机时，相当于按下开机按钮；在虚拟机暂停或挂起时，恢复虚拟机运行）
     * 指定运行物理机
     * @param machine  虚拟机句柄
     * @param sched_to 运行物理机的IP地址
     * @param callback 执行结果的回调接口
     */
    public void startVirtualMachine(VM_HANDLE machine, String sched_to, IVMPowerCallback callback);
    
    /** 
     * 关闭虚拟机（相当于强制切断电源）
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    //这个好像没有
    public void powerdownVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback); 
    
    /**
     * 关闭虚拟机（相当于按下关机按钮）
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    public void shutdownVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback);

    /**
     * 重启虚拟机（相当于按下reset按钮）
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    public void resetVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback);

    /**
     * 暂停虚拟机（类比于操作系统的待机）
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    //这个好像没有
    public void pauseVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback);

    /** 
     * 挂起虚拟机(类比于操作系统的休眠）
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    public void suspendVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback);

    /**
     * 将虚拟机在线迁移到指定物理节点上
     * @param machine 虚拟机句柄
     * @param addr 目标物理节点的地址
     * @param callback 执行结果的回调接口
     */
    public void migrateVirtualMachine(VM_HANDLE machine, InetAddress addr, IVMMigrationCallback callback);

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 获取当前虚拟机下的状态
     * @param machine 虚拟机句柄
     * @return 返回状态句柄数组
     */
    public STATE_HANDLE[] getVirtualMachineStateList(VM_HANDLE machine);

    /** 
     * 获取状态信息
     * @param machine 虚拟机句柄
     * @param state 虚拟机状态句柄
     * @return 虚拟机状态信息，如创建时间等
     */
    public ISTATE_INFO getVirtualMachineStateInfo(VM_HANDLE machine, STATE_HANDLE state);

    /**
     * 保存当前虚拟机的状态
     * @param machine 虚拟机句柄
     * @param callback 执行结果的回调接口
     */
    public void saveVirtualMachineState(VM_HANDLE machine, IVMStateCallback callback);

    /**
     * 将虚拟机回滚到保存的状态上
     * @param state 虚拟机状态句柄
     * @param callback 执行结果的回调接口
     */
    public void loadVirtualMachineState(STATE_HANDLE state, IVMStateCallback callback);

	
}
