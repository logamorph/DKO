package org.kered.dko;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.kered.dko.Field.PK;
import org.kered.dko.TemporaryTableFactory.DummyTableWithName;


/**
 * This is the base class of all classes generated by this API.
 * Some of these methods are public only by necessity.  Please use only
 * {@code insert()}, {@code update()}, {@code save()} and {@code dirty()}.
 * I consider all others fair game for changing in later versions of the API.
 *
 * @author Derek Anderson
 */
public abstract class Table {

	/**
	 * @return The database schema
	 * @deprecated please use the static _SCHEMA_NAME class attribute (on the generated classes)
	 */
	@Deprecated
	protected String SCHEMA_NAME() {
		return Util.getSchemaName(this.getClass());
	}

	/**
	 * @return The database schema
	 * @deprecated please use the static _TABLE_NAME class attribute (on the generated classes)
	 */
	protected String TABLE_NAME() {
		throw new RuntimeException("fix me...");
		//return Util.getTABLE_NAME(this.getClass());
	}

	/**
	 * @return A list of the fields defined for this class.
	 * @deprecated please use the static _FIELDS class attribute (on the generated classes)
	 */
	@Deprecated
	public List<Field<?>> FIELDS() {
		return Util.getFields(this.getClass());
	}

	/**
	 * Please do not use.
	 * @return
	 */
	protected abstract Field.FK[] FKS();

	/**
	 * Please do not use.
	 */
	protected BitSet __NOSCO_FETCHED_VALUES = null;

	/**
	 * Please do not use.
	 */
	protected BitSet __NOSCO_UPDATED_VALUES = null;

	/**
	 * Please do not use.
	 */
	protected DataSource __NOSCO_ORIGINAL_DATA_SOURCE = null;

	@SuppressWarnings("rawtypes")
	UsageMonitor __NOSCO_USAGE_MONITOR = null;

	/**
	 * Returns true if the object has been modified
	 * @return true if the object has been modified
	 */
	public boolean dirty() {
		return __NOSCO_UPDATED_VALUES != null && !__NOSCO_UPDATED_VALUES.isEmpty();
	}

	/**
	 * Return the value of this instance that corresponds to the given field.
	 * Throws an IllegalArgumentException if the field isn't part of this instance.
	 * @param field
	 * @return
	 */
	public abstract <S> S get(Field<S> field);

	/**
	 * @return A list of the fields populated in this instance.
	 */
	public abstract List<Field<?>> fields();

	/**
	 * Sets the value of this instance that corresponds to the given field.
	 * @param field
	 * @param value
	 * @return
	 */
	public abstract <S> Table set(Field<S> field, S value);

	/**
	 * Creates and executes an insert statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean insert() throws SQLException;

	/**
	 * Creates and executes an update statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean update() throws SQLException;

	/**
	 * Creates and executes a delete statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean delete() throws SQLException;

	/**
	 * Creates and executes an insert or update statement for this object
	 * based on if the object came from the database or not.
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean save() throws SQLException;

	/**
	 * Returns if this object exists in the database. &nbsp;
	 * Executes SQL looking for the PK values, or all columns if no PK.
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean exists() throws SQLException;

	/**
	 * Creates and executes an insert statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean insert(DataSource ds) throws SQLException;

	/**
	 * Creates and executes an update statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean update(DataSource ds) throws SQLException;

	/**
	 * Creates and executes a delete statement for this object
	 * (irregardless of if it's already in the database)
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean delete(DataSource ds) throws SQLException;

	/**
	 * Creates and executes an insert or update statement for this object
	 * based on if the object came from the database or not.
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean save(DataSource ds) throws SQLException;

	/**
	 * Returns if this object exists in the database. &nbsp;
	 * Executes SQL looking for the PK values, or all columns if no PK.
	 * @return success
	 * @throws SQLException
	 */
	public abstract boolean exists(DataSource ds) throws SQLException;

	static Map<Table,java.lang.reflect.Field> _pkCache = new HashMap<Table, java.lang.reflect.Field>();
	static Field.PK GET_TABLE_PK(final Table table) {
		if (!_pkCache.containsKey(table)) {
			java.lang.reflect.Field field = null;
			try {
				field = table.getClass().getDeclaredField("PK");
			} catch (final SecurityException e) {
				e.printStackTrace();
			} catch (final NoSuchFieldException e) {
				e.printStackTrace();
			}
			_pkCache.put(table, field);
		}
		try {
			return (PK) _pkCache.get(table).get(table);
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This is used for conditional statements where the fields are ambiguous. (ie: a self-join) &nbsp;
	 * All generated subclasses of {@code Table} will contain a static {@code MyTable.as(String s)} method. &nbsp;
	 * It will return an instance of this class. &nbsp; You will likely never have to create one manually. &nbsp;
	 * (the constructor is public only because the generated classes are in a different package scope)
	 * <p>
	 * Example:
	 * <pre>  {@code MyTable.ALL.cross(MyTable.as("t2")).where(MyTable.ID.from("t2").eq(MyTable.PARENT_ID))}</pre>
	 * @author Derek Anderson
	 * @param <S>
	 */
	public static class __Alias<S extends Table> {

		final Class<S> table;
		final String schema_name;
		final String table_name;
		final String alias;
		public final Query<S> ALL;
		final DummyTableWithName dummyTable;

		public __Alias(final Class<S> table, final String alias) {
			this.table = table;
			this.schema_name = Util.getSchemaName(table);
			this.table_name = Util.getTableName(table);
			this.alias = alias;
			Query<S> all = null;
			try {
				final S instance = table.newInstance();
				all = new DBQuery<S>(this);
				final java.lang.reflect.Field f = table.getDeclaredField("ALL");
				f.setAccessible(true);
				@SuppressWarnings("unchecked")
				final
				DBQuery<S> q = (DBQuery<S>) f.get(null);
				all = all.use(q.ds);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			this.ALL = all;
			this.dummyTable = null;
		}

		public __Alias(final DummyTableWithName tmp, final String aliasName) {
			this.table = tmp.cls;
			this.schema_name = null;
			this.table_name = null;
			this.alias = aliasName;
			this.ALL = null;
			this.dummyTable = tmp;
		}

	}

	/**
	 * Represents a primary key of a row. &nbsp; Supports compound primary keys. &nbsp;
	 * Each attribute is accessible by calling {@code get(field)}. &nbsp; Note that this
	 * different than {@code Field.PK} which represents the primary key of a table (ie: the
	 * set of columns that make up the key).
	 * <p>
	 * Use with enums. &nbsp; Nosco generated enums of table values are always
	 * {@code __PrimaryKey}s. &nbsp; They can be used in comparisons to foreign keys. &nbsp;
	 * For instance, assume tables A and B, with a FK from A to B (and A has been enumed):
	 *
	 * <pre>  {@code B.ALL.where(A.FK_B.eq(A.PKS.INSTANCE_ONE))}</pre>
	 *
	 * {@code A.PKS.INSTANCE_ONE} is an instance of this class. &nbsp; {@code A.FK_B} is an
	 * instance of {@code Field.FK}. &nbsp; {@code B.ALL} is an instance of {@code Query<B>}.
	 *
	 * @author Derek Anderson
	 *
	 * @param <S>
	 */
	public static interface __PrimaryKey<S extends Table> {
		<R> R get(Field<R> field);
		List<Field<?>> FIELDS();
	}

	/**
	 * Represents a simple (ie: non-compound) primary key of a row. &nbsp;
	 * Attribute is accessible by calling {@code get(field)} or by calling {@code value()}.
	 *
	 * @author Derek Anderson
	 *
	 * @param <S>
	 * @param <V>
	 */
	public static interface __SimplePrimaryKey<S extends Table,V> extends __PrimaryKey<S> {
		public V value();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    final Field.PK<?> pk = Util.getPK(this);
	    final List<Field<?>> fields = pk == null ? Util.getFields(this.getClass()) : pk.GET_FIELDS();
	    for (final Field<?> f : fields) {
	    	final Object o = this.get(f);
		    result = prime * result + ((o == null) ? 0 : o.hashCode());
	    }
	    return result;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (!(other instanceof Table)) return false;
	    final Field.PK<?> pk = Util.getPK(this);
	    final List<Field<?>> fields = pk == null ? Util.getFields(this.getClass()) : pk.GET_FIELDS();
	    for (final Field<?> f : fields) {
	    	final Object o1 = this.get(f);
	    	final Object o2 = ((Table)other).get(f);
	    	if (!((o1 == null) ? (o2 == null) : o1.equals(o2))) return false;
	    }
	    return true;
	}

	/**
	 * Internal function - please don't use. &nbsp; Subject to change.
	 * @param o
	 * @return
	 */
	protected abstract java.lang.Object __NOSCO_PRIVATE_mapType(java.lang.Object o);

	/**
	 * {@code toStringDetailed()} will print out all the values of all the fields fetched.
	 * @return
	 */
	public String toStringDetailed() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(this.getClass().getSimpleName());
		for (Field<?> field : fields()) {
			sb.append(" ").append(field.NAME).append(":").append(this.get(field));
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * The {@code toStringSimple()} methods generated by nosco print out PK values plus any "name" column it finds. &nbsp;
	 * By default the genned {@code toString()} calls this.
	 * @return
	 */
	public String toStringSimple() {
		return toString();
	}

	/**
	 * Internal function - please don't use. &nbsp; Subject to change.
	 * @param context
	 * @param conn
	 */
	protected void __NOSCO_PRIVATE_preExecute(final SqlContext context, final Connection conn) throws SQLException {}

	/**
	 * Internal function - please don't use. &nbsp; Subject to change.
	 * @param conn
	 */
	protected void __NOSCO_PRIVATE_postExecute(final SqlContext context, final Connection conn) throws SQLException {}

	/**
	 * Internal function - please don't use. &nbsp; Subject to change.
	 * @param conn
	 */
	protected void __NOSCO_PRIVATE_accessedFkCallback(final Table table, final Field.FK<? extends Table> fk) {
		if (__NOSCO_USAGE_MONITOR!=null) __NOSCO_USAGE_MONITOR.accessedFkCallback(table, fk);
	}

	/**
	 * Internal function - please don't use. &nbsp; Subject to change.
	 * @param conn
	 */
	protected void __NOSCO_PRIVATE_accessedColumnCallback(final Table table, final Field<?> field) {
		if (__NOSCO_USAGE_MONITOR!=null) __NOSCO_USAGE_MONITOR.__NOSCO_PRIVATE_accessedColumnCallback(table, field);
	}

	String TABLE_NAME(final SqlContext sqlContext) {
		return Util.getTableName(this.getClass());
	}

}
