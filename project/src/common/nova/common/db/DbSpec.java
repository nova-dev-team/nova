package nova.common.db;

import java.util.HashSet;
import java.util.Set;

public class DbSpec {

	Set<String> indexFields = new HashSet<String>();

	public DbSpec() {
		this.addIndex("id");
	}

	public void addIndex(String fieldName) {
		indexFields.add(fieldName);
	}

	public Set<String> getAllIndex() {
		return this.indexFields;
	}
}
