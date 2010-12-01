package nova.Interface;
    
/**
 * 虚拟机的远程控制控制器，每个实例对应一个虚拟机，可以有不同的实现方式，但至少需提供以下接口
 */
public interface IVMRemoteControl
{
    /**
     * 执行虚拟机中的指定程序
     * @param path 虚拟机中程序的路径
     * @return 返回值为0，执行成功，返回小于0，则执行失败，返回值为错误代码
     */
    public int execRemoteProcedure(String path);

    /**
     * 关闭虚拟机中的指定程序
     * @param name 要关闭的进程的进程名
     * @return 返回值为0，执行成功，返回小于0，则执行失败，返回值为错误代码
     */
    public int terminateRemoteProcedure(String name);

    /**
     * 在虚拟机中创建文件
     * @param path 要创建的文件的路径
     * @return 返回值为0，执行成功，返回小于0，则执行失败，返回值为错误代码
     */
    public int createRemoteFile(String path);

    /**
     * 在虚拟机中删除文件
     * @param path 要删除的文件的路径
     * @return 返回值为0，执行成功，返回小于0，则执行失败，返回值为错误代码
     */
    public int removeRemoteFile(String path);

}
