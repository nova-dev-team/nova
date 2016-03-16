/**
 * exception triggered when there is not a single virtual
 * machine on the hot spot physical machine. 
 * we assume that a "hot spot" is resulted purely from the payload
 * within some virtual machines on it. 
 */
package nova.master.daemons;

/**
 * @author Tianyu Chen
 *
 */
public class NoVnodeOnHotspotException extends RuntimeException {

    private static final long serialVersionUID = -5894715719439775644L;

    public NoVnodeOnHotspotException() {
        super("No vnode on hotspot! ");
    }
}
