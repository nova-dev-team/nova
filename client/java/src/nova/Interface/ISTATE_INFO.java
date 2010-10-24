package nova.Interface;
/**
 * 提供状态的信息，如状态的创建时间等，其内容会因为实现方式的不同而变化，也可根据需要添加。本接口提供信息的方式类似与字典，每种信息都用其特定的关键字来标识，目前的关键字有：<br/>
 * state.create_time 该装态的创建时间
 */
public interface ISTATE_INFO
{
    /**
     * 根据关键字获取相应的状态信息，如getInfo("state.create_time")就能够得到状态的创建时间
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
}
