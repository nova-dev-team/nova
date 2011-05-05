package nova.common.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class EnumUserType<E extends Enum<E>> implements UserType {
	Logger log = Logger.getLogger(EnumUserType.class);
	private Class<E> clazz = null;
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	protected EnumUserType(Class<E> c) {
		this.clazz = c;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	public Class<E> returnedClass() {
		return clazz;
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y)
			return true;
		if (null == x || null == y)
			return true;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException {
		E result = null;
		if (!resultSet.wasNull()) {
			String name = resultSet.getString(names[0]).toUpperCase();
			result = Enum.valueOf(clazz, name);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public void nullSafeSet(PreparedStatement prepareStatement, Object value,
			int index) throws HibernateException, SQLException {
		if (null == value) {
			prepareStatement.setNull(index, Types.VARCHAR);
		} else {
			prepareStatement.setString(index, ((Enum) value).name());
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}
}