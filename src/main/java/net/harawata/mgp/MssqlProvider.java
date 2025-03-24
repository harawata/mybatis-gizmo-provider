/*-
 * MIT License
 *
 * Copyright (c) 2025 Iwao AVE!
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.harawata.mgp;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;

public class MssqlProvider extends GizmoProvider {
  protected static final String DQ = "\"";
  protected static final String SRCTABLE = "srctbl";
  protected static final String DESTTABLE = "desttbl";

  @Override
  protected CharSequence escape(CharSequence name) {
    return new StringBuilder().append(DQ).append(name).append(DQ);
  }

  @Override
  public StringBuilder upsert(Object bean, ProviderContext context) {
    String mapperMethodName = context.getMapperMethod().getName();
    String[] keyColumns = mapperMethodName.split("On(?=[A-Z])|And(?=[A-Z])");
    if(keyColumns.length == 1) {
      throw new IllegalArgumentException("Upsert requires key column name(s) after 'On' e.g. 'upsertSomeBeanOnId(Bean bean)'");
    }

    CharSequence src = escape(SRCTABLE);
    CharSequence dest = escape(DESTTABLE);

    Class<? extends Object> beanClass = bean.getClass();
    List<Field> fields = getInsertableFields(beanClass);
    StringBuilder sql = new StringBuilder();

    sql.append("merge into ");
    catalogOrSchema(beanClass).ifPresent(x -> sql.append(escape(x)).append('.'));
    sql.append(escape(getTableName(beanClass))).append(" with (holdlock) as ").append(dest)
        .append(" using (select ");

    StringBuilder matched = new StringBuilder(") when matched then update set ");
    StringBuilder notMatched = new StringBuilder(" when not matched then insert (");
    StringBuilder values = new StringBuilder(") values (");


    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) {
        sql.append(", ");
        matched.append(", ");
        notMatched.append(", ");
        values.append(", ");
      }
      CharSequence col = escape(getColumnName(fields.get(i)));
      sql.append("#{").append(fields.get(i).getName()).append("} as ").append(col);
      matched.append(col).append(" = ").append(src).append(".").append(col);
      notMatched.append(col);
      values.append(src).append(".").append(col);
    }
    sql.append(") as ").append(src).append(" on (");
    for (int i = 1; i < keyColumns.length; i++) {
      if (i > 1) {
        sql.append(", ");
      }
      CharSequence keyColumn = escape(decapitalize(keyColumns[i]));
      sql.append(dest).append(".").append(keyColumn).append(" = ").append(src).append(".").append(keyColumn);
    }
    sql.append(matched).append(notMatched).append(values).append(");");
    return sql;
  }
}
