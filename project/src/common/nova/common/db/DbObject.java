package nova.common.db;

public abstract class DbObject {

	protected long id = 1L;

	public final long getId() {
		return this.id;
	}

	public final void setId(long id) {
		this.id = id;
	}
}
