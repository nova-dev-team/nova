/**
 * 
 */
package nova.Interface;

import java.util.List;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 * 
 */
/**
 * @author frankvictor mailto:frankvictor@qq.com
 * 
 */
public interface IVCControl {
	
	/**
	 * name：虚拟集群的名字，不要带空格和减号，最好是英文+数字
	 * size：虚拟集群的大小
	 * machines：各个虚拟机的定义。每个虚拟机的定义如下： 
	 * vdisk_fname=硬盘镜像文件名                                      
	 * machine_name=虚拟机名字                                                         
	 * cpu_count=CPU个数                                                                                       
	 * mem_size=内存大小（单位MB）                             
	 * soft_list=（留空）                                                                      
	 * 然后注意每个虚拟机定义结束都要添加一个空行，包括最后一个虚拟机的定义后面也要加一个空行。但是也不要加多了，只要一个空行 
	 * 一个例子：
	 * name:                                         
	 * cluster1                                      
	 *                                               
	 * size:                                         
	 * 2                                             
	 *                                               
	 * machines: （最开始不要加空行） 
	 * vdisk_fname=winxp.img                         
	 * machine_name=node1                            
	 * cpu_count=1                                   
	 * mem_size=256                                  
	 * soft_list=                                    
	 *                                               
	 * vdisk_fname=small.img                         
	 * machine_name=node2                            
	 * cpu_count=1                                   
	 * mem_size=512                                  
	 * soft_list=                                    
	 *                                               
	 * vdisk_fname=winxp.img                         
	 * machine_name=node3                            
	 * cpu_count=2                                   
	 * mem_size=256                                  
	 * soft_list=                                    
	 *                                               
	 * 注意上面还留空了一行
	 * @return
	 */
	public boolean createVM(String name, List<Machine> machines);
	
	/**
	 * list all clusters' name
	 */
	public List<String> listVclusters();
	
	public List<String> listVmachines(String clustername);
}
