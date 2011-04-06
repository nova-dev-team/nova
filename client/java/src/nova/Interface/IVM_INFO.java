package nova.Interface;
/**
 * 提供虚拟机的信息，如内存、CPU、网络信息等，还可以包括创建时间、大小等等，其内容会因为实现方式的不同而变化，也可根据需要添加。本接口提供信息的方式类似与字典，每种信息都用其特定的关键字来标识，目前的关键字有：<br/>
 * vm.unique_id 虚拟机的全局唯一标识<br/>
 * vm.state 虚拟机目前的运行状态，包括运行、挂起、关机等<br/>
 * memory.size 虚拟机的内存大小<br/>
 * cpu.count 虚拟机分配到的CPU数量<br/>
 * net.ip 虚拟机的IP地址<br/>
 * migration.state 虚拟机的迁移状态，包括没有迁移、不停机阶段、停机阶段等<br/>
 * migration.process 迁移的进度<br/>
 * migration.target 迁移的目标
 */
public interface IVM_INFO
{
    /**
     * 根据关键字获取相应的虚拟机信息，如getInfo("memory.size")就能够得到内存大小
     * 另外返回的对象一定要提供合适的toString()方法
     *
     * @param key 用来索引信息的关键字
     * @return 返回包含信息的对象
     */
    public Object getInfo(String key);

    /**
     * 查询某个关键字是否存在
     * @param key 用来索引信息的关键字
     */
    public boolean hasInfo(String key);

    public String toString();
}
