
require 'logger'
require 'rubygems'
require 'sqlite3'

module Tools
  LOGGER = Logger.new STDOUT

  DB_RETRY = 5

  def self._dbexec(db, sql)
    db.execute(sql)
  end

  def self.dbexec(dburi, n = DB_RETRY)
    tries = 0
    begin
      db = SQLite3::Database.new(dburi)
      tries += 1
      yield db
    rescue SQLite3::BusyException, SQLite3::LockedException => e
      db.close
      if tries < n
        LOGGER.error "tools capture a exception #{e}, retry#{2**tries}"
        sleep 2**tries
        retry
      end
    end
  ensure
    db.close
  end

  def self.sqlite3_insert(db, table, args)
    sql = "insert into "
    sql += table + "(" + args.keys.join(', ') + ") "
    values = args.values.map { |v| "\"" + v.to_s + "\""}
    sql += "values(" + values.join(', ') + ") "

    LOGGER.debug "sqlite3_insert >> " + sql

    _dbexec db, sql
  end

  def self.sqlite3_update_by_id(db, table, args, id)
    sql = "update " + table
    sql += " set "
    s = args.map { |k, v|
      k.to_s + " = " + "\"" + v.to_s + "\""
    }
    sql += s.join(', ')
    sql += " where id = " + id

    LOGGER.debug "sqlite3_update >> " + sql

    _dbexec db, sql
  end

  def self.sqlite3_update(db, table, args, where)
    sql = "update " + table
    sql += " set "
    s = args.map { |k, v|
      k.to_s + " = " + "\"" + v.to_s + "\""
    }
    sql += s.join(', ')

    sql += " where "
    w = where.map { |k, v|
      k.to_s + " = " + "\"" + v.to_s + "\""
    }
    sql += w.join(', ')

    LOGGER.debug "sqlite3_update >> " + sql

    _dbexec db, sql
  end

  def self.sqlite3_select(db, table, cols, where)
    sql = "select " + cols.join(', ')
    sql += " from " + table
    sql += " where "
    c = []
    where.each { |k, v|
      c << k.to_s + " = " + "\"" + v.to_s + "\""
    }
    sql += " " + c.join(' and ')

    LOGGER.debug "sqlite3_select >> " + sql

    _dbexec db, sql
  end

  def self.sqlite3_delete(db, table, where)
    sql = "delete from " + table
    sql += " where "
    c = []
    where.each { |k, v|
      c << k.to_s + " = " + "\"" + v.to_s + "\""
    }
    sql += " " + c.join(' and ')

    LOGGER.debug "sqlite3_delete >> " + sql

    _dbexec db, sql
  end

end


