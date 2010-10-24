package nova.Interface;

import java.net.InetAddress;

/**
 * 迁移操作的回调接口
 */
public interface IVMMigrationCallback
{
    /**
     * 在线迁移启动时被调用
     * @param machine 被迁移机器的虚拟机句柄
     * @param srcAddr 迁移源地址 
     * @param desAddr 迁移目的地址
     */
    void onMigrationBegin(VM_HANDLE machine,InetAddress srcAddr, InetAddress desAddr);

    /**
     * 在线迁移结束时被调用
     * @param machine 被迁移机器的虚拟机句柄
     * @param srcAddr 迁移源地址 
     * @param desAddr 迁移目的地址
     */
    void onMigrationDone(VM_HANDLE machine, InetAddress srcAddr, InetAddress desAddr);

    /**
     * 在线迁移出错时被调用
     * @param machine 被迁移机器的虚拟机句柄
     * @param srcAddr 迁移源地址 
     * @param desAddr 迁移目的地址
     */
    void OnError(VM_HANDLE machine, InetAddress srcAddr, InetAddress desAddr);
}
