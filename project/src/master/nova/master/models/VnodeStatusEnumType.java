package nova.master.models;

import nova.common.db.EnumUserType;

public class VnodeStatusEnumType extends EnumUserType<Vnode.Status> {
	public VnodeStatusEnumType() {
		super(Vnode.Status.class);
		// TODO Auto-generated constructor stub
	}
}
