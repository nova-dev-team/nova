package nova.common.db;

import java.util.HashSet;
import java.util.Set;

public class DbSpec {

	public static final String ID_COLUMN_NAME = "id";

	Set<String> indexFields = new HashSet<String>();

	public DbSpec() {
		this.addIndex(DbSpec.ID_COLUMN_NAME);
	}

	public void addIndex(String fieldName) {
		indexFields.add(fieldName);
	}

	public Set<String> getAllIndex() {
		return this.indexFields;
	}
}
