package nova.common.db;

import java.util.HashSet;
import java.util.Set;

public class DbSpec {

	Set<String> indexCols = new HashSet<String>();

	public DbSpec() {
		this.addIndex("id");
	}

	public void addIndex(String colName) {
		indexCols.add(colName);
	}

	public Set<String> getAllIndex() {
		return this.indexCols;
	}
}
