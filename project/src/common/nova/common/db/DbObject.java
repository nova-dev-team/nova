package nova.common.db;

public abstract class DbObject {

	public static final long INVALID_ID = -1L;

	protected long id = INVALID_ID;

	public final long getId() {
		return this.id;
	}

	public final void setId(long id) {
		this.id = id;
	}
}
